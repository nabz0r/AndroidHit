package com.androidhit.lumadrift.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Offline, original procedural sound design. Own from one Activity and release on destruction.
 * All public operations are safe before preparation, after pause, or after release.
 * No media files, network connection, or Android permissions are required.
 */
public final class AudioEngine {
    public enum Sound { TAP, SELECT, MOVE, BLOCKED, GATE, UNDO, HINT, COIN, PURCHASE, WIN, LOSE }

    private final Object lock = new Object();
    private final AtomicBoolean released = new AtomicBoolean();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Luma-original-audio");
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(true);
        return t;
    });
    private final EnumMap<Sound, Integer> samples = new EnumMap<>(Sound.class);
    private final Set<Integer> loaded = new HashSet<>();
    private final SoundPool pool;
    private final AudioManager manager;
    private final AudioFocusRequest focusRequest;
    private final AudioManager.OnAudioFocusChangeListener focusListener;
    private AudioTrack music;
    private boolean running;
    private boolean sfxEnabled = true;
    private boolean musicEnabled = true;
    private boolean focusGranted;
    private boolean focusSuppressed;
    private boolean ducked;
    private float sfxVolume = 0.72f;
    private float musicVolume = 0.24f;

    public AudioEngine(Context context) {
        Context app = context.getApplicationContext();
        manager = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes effects = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        pool = new SoundPool.Builder().setMaxStreams(7).setAudioAttributes(effects).build();
        pool.setOnLoadCompleteListener((p, sampleId, status) -> {
            synchronized (lock) {
                if (status == 0 && !released.get()) loaded.add(sampleId);
            }
        });
        focusListener = change -> {
            synchronized (lock) {
                if (released.get()) return;
                if (change == AudioManager.AUDIOFOCUS_GAIN) {
                    focusSuppressed = false;
                    ducked = false;
                    focusGranted = true;
                } else if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    ducked = true;
                } else {
                    focusSuppressed = true;
                    if (change == AudioManager.AUDIOFOCUS_LOSS) focusGranted = false;
                }
                updateMusicLocked();
            }
        };
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setOnAudioFocusChangeListener(focusListener).build();
        File directory = new File(app.getCacheDir(), "luma_audio_v1");
        worker.execute(() -> prepare(directory));
    }

    private void prepare(File directory) {
        AudioTrack candidate = null;
        try {
            if (!directory.exists() && !directory.mkdirs()) return;
            for (Sound sound : Sound.values()) {
                if (released.get() || Thread.currentThread().isInterrupted()) return;
                File file = new File(directory, sound.name().toLowerCase(java.util.Locale.ROOT) + ".wav");
                if (!file.isFile() || file.length() < 44) writeWave(file, Synth.effect(sound.name()));
                synchronized (lock) {
                    if (released.get()) return;
                    samples.put(sound, pool.load(file.getAbsolutePath(), 1));
                }
            }
            if (released.get()) return;
            short[] pcm = Synth.ambientLoop();
            candidate = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(Synth.RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(pcm.length * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC).build();
            if (candidate.getState() != AudioTrack.STATE_INITIALIZED) return;
            int written = candidate.write(pcm, 0, pcm.length);
            if (written != pcm.length || candidate.setLoopPoints(0, pcm.length, -1) != AudioTrack.SUCCESS) return;
            synchronized (lock) {
                if (!released.get()) {
                    music = candidate;
                    candidate = null;
                    updateMusicLocked();
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // A muted device or unavailable audio output must never prevent playing the game.
        } finally {
            if (candidate != null) {
                try { candidate.release(); } catch (RuntimeException ignored) { }
            }
        }
    }

    public void play(Sound sound) {
        if (sound == null || released.get()) return;
        synchronized (lock) {
            if (released.get() || !running || !sfxEnabled || focusSuppressed) return;
            Integer id = samples.get(sound);
            if (id == null || !loaded.contains(id)) return;
            float volume = sfxVolume * (ducked ? 0.25f : 1f);
            try { pool.play(id, volume, volume, sound == Sound.WIN ? 2 : 1, 0, 1f); }
            catch (RuntimeException ignored) { }
        }
    }

    public void setSfxEnabled(boolean enabled) {
        synchronized (lock) {
            sfxEnabled = enabled;
            if (!enabled && !released.get()) pool.autoPause();
            if (enabled && running && !focusGranted) requestFocusLocked();
        }
    }

    public void setMusicEnabled(boolean enabled) {
        synchronized (lock) {
            musicEnabled = enabled;
            if (running && enabled && !focusGranted) requestFocusLocked();
            updateMusicLocked();
        }
    }

    public void setSfxVolume(float volume) {
        synchronized (lock) { sfxVolume = clampVolume(volume); }
    }

    public void setMusicVolume(float volume) {
        synchronized (lock) {
            musicVolume = clampVolume(volume);
            updateMusicLocked();
        }
    }

    public boolean isSfxEnabled() { synchronized (lock) { return sfxEnabled; } }
    public boolean isMusicEnabled() { synchronized (lock) { return musicEnabled; } }

    /** Call in Activity.onResume. Music resumes at its previous position. */
    public void resume() {
        synchronized (lock) {
            if (released.get()) return;
            running = true;
            focusSuppressed = false;
            requestFocusLocked();
            updateMusicLocked();
        }
    }

    /** Call in Activity.onPause. Stops all sound immediately and relinquishes audio focus. */
    public void pause() {
        synchronized (lock) {
            if (released.get()) return;
            running = false;
            try { pool.autoPause(); } catch (RuntimeException ignored) { }
            updateMusicLocked();
            abandonFocusLocked();
        }
    }

    /** Idempotent. Call in Activity.onDestroy. */
    public void release() {
        synchronized (lock) {
            if (!released.compareAndSet(false, true)) return;
            running = false;
            abandonFocusLocked();
            if (music != null) {
                try { music.pause(); music.release(); } catch (RuntimeException ignored) { }
                music = null;
            }
            try { pool.release(); } catch (RuntimeException ignored) { }
            samples.clear();
            loaded.clear();
        }
        worker.shutdownNow();
    }

    private void requestFocusLocked() {
        if (released.get() || manager == null || focusGranted || (!musicEnabled && !sfxEnabled)) return;
        try {
            int result = manager.requestAudioFocus(focusRequest);
            focusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            focusSuppressed = !focusGranted;
        } catch (RuntimeException ignored) { focusSuppressed = false; }
    }

    private void abandonFocusLocked() {
        if (manager != null) {
            try {
                manager.abandonAudioFocusRequest(focusRequest);
            } catch (RuntimeException ignored) { }
        }
        focusGranted = false;
        ducked = false;
    }

    private void updateMusicLocked() {
        if (music == null || released.get()) return;
        try {
            music.setVolume(musicVolume * (ducked ? 0.2f : 1f));
            boolean shouldPlay = running && musicEnabled && !focusSuppressed;
            if (shouldPlay && music.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) music.play();
            else if (!shouldPlay && music.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) music.pause();
        } catch (RuntimeException ignored) { }
    }

    private static float clampVolume(float value) {
        return Float.isNaN(value) ? 0f : Math.max(0f, Math.min(1f, value));
    }

    private static void writeWave(File file, short[] pcm) throws IOException {
        byte[] wav = new byte[44 + pcm.length * 2];
        ascii(wav, 0, "RIFF"); le32(wav, 4, wav.length - 8); ascii(wav, 8, "WAVEfmt ");
        le32(wav, 16, 16); le16(wav, 20, 1); le16(wav, 22, 1);
        le32(wav, 24, Synth.RATE); le32(wav, 28, Synth.RATE * 2);
        le16(wav, 32, 2); le16(wav, 34, 16); ascii(wav, 36, "data");
        le32(wav, 40, pcm.length * 2);
        for (int i = 0; i < pcm.length; i++) le16(wav, 44 + i * 2, pcm[i]);
        File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
        try (FileOutputStream stream = new FileOutputStream(temporary)) { stream.write(wav); }
        if (!temporary.renameTo(file)) {
            temporary.delete();
            throw new IOException("Cannot cache original sound");
        }
    }

    private static void ascii(byte[] output, int offset, String value) {
        for (int i = 0; i < value.length(); i++) output[offset + i] = (byte) value.charAt(i);
    }
    private static void le16(byte[] output, int offset, int value) {
        output[offset] = (byte) value; output[offset + 1] = (byte) (value >>> 8);
    }
    private static void le32(byte[] output, int offset, int value) {
        le16(output, offset, value); le16(output, offset + 2, value >>> 16);
    }
}
