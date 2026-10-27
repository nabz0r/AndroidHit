package com.androidhit.lumadrift.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

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
    public static final int BIOME_COUNT = 10;
    private static final String TAG = "LumaAudio";
    static final float DEFAULT_MUSIC_VOLUME = 0.27f;

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
    private final Handler fades = new Handler(Looper.getMainLooper());
    private final Runnable fadeTick = this::advanceFade;
    private AudioTrack music;
    private int requestedBiome;
    private int loadedBiome = -1;
    private long biomeGeneration;
    /** One coalescing worker, including the constructor's initial preparation. */
    private boolean musicWorkerQueued = true;
    private boolean running;
    private boolean sfxEnabled = true;
    private boolean musicEnabled = true;
    private boolean focusGranted;
    private boolean focusSuppressed;
    private boolean ducked;
    private float sfxVolume = 0.72f;
    private float musicVolume = DEFAULT_MUSIC_VOLUME;
    private float appliedMusicGain;
    private float fadeFrom;
    private float fadeTo;
    private long fadeStarted;
    private long fadeDuration;
    private boolean fading;
    private boolean pauseAfterFade;

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
                if (released.get() || !running) return;
                if (change == AudioManager.AUDIOFOCUS_GAIN) {
                    focusSuppressed = false;
                    ducked = false;
                    focusGranted = true;
                } else if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    ducked = true;
                } else {
                    focusSuppressed = true;
                    if (change == AudioManager.AUDIOFOCUS_LOSS) focusGranted = false;
                    try { pool.autoPause(); } catch (RuntimeException ignored) { }
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
        try {
            if (!directory.exists() && !directory.mkdirs()) throw new IOException("Cannot create sound cache");
            for (Sound sound : Sound.values()) {
                if (released.get() || Thread.currentThread().isInterrupted()) return;
                File file = new File(directory, sound.name().toLowerCase(java.util.Locale.ROOT) + ".wav");
                if (!file.isFile() || file.length() < 44) writeWave(file, Synth.effect(sound.name()));
                synchronized (lock) {
                    if (released.get()) return;
                    samples.put(sound, pool.load(file.getAbsolutePath(), 1));
                }
            }
        } catch (IOException | RuntimeException failed) {
            // Music does not depend on the effects cache being writable.
            Log.w(TAG, "Could not prepare every sound effect", failed);
        }
        prepareRequestedMusic();
    }

    /**
     * One job consumes the newest request. PCM is never cached for all ten
     * worlds: only the current output and one in-flight replacement exist.
     * Generation guards cover late DSP/write completion and Activity destruction.
     */
    private void prepareRequestedMusic() {
        while (true) {
            final int target;
            final long generation;
            synchronized (lock) {
                if (released.get() || Thread.currentThread().isInterrupted() || loadedBiome == requestedBiome) {
                    musicWorkerQueued = false;
                    return;
                }
                target = requestedBiome;
                generation = biomeGeneration;
            }
            AudioTrack candidate = null;
            try {
                short[] pcm = Synth.ambientLoop(target);
                synchronized (lock) {
                    if (released.get() || Thread.currentThread().isInterrupted()) return;
                    if (generation != biomeGeneration) continue;
                }
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
                // MODE_STATIC starts in STATE_NO_STATIC_DATA: fill before readiness/loop checks.
                if (candidate.getState() == AudioTrack.STATE_UNINITIALIZED)
                    throw new IllegalStateException("Music output could not initialize");
                int written = candidate.write(pcm, 0, pcm.length);
                if (written != pcm.length || candidate.getState() != AudioTrack.STATE_INITIALIZED
                        || candidate.setLoopPoints(0, pcm.length, -1) != AudioTrack.SUCCESS)
                    throw new IllegalStateException("Music PCM or loop could not load: " + written);
                candidate.setVolume(0f);
                if (!awaitPreviousFade(generation)) continue;
                synchronized (lock) {
                    if (released.get() || generation != biomeGeneration) continue;
                    fades.removeCallbacks(fadeTick);
                    fading = false;
                    pauseAfterFade = false;
                    appliedMusicGain = 0f;
                    releaseTrack(music);
                    music = candidate;
                    candidate = null;
                    loadedBiome = target;
                    Log.i(TAG, "Original ambient ready: biome=" + target + ", " + pcm.length
                            + " mono frames at " + Synth.RATE + " Hz");
                    updateMusicLocked();
                }
            } catch (RuntimeException failed) {
                synchronized (lock) {
                    if (released.get()) return;
                    if (generation == biomeGeneration) {
                        musicWorkerQueued = false;
                        Log.w(TAG, "Could not prepare ambient music", failed);
                        return;
                    }
                }
            } finally {
                releaseTrack(candidate);
            }
        }
    }

    /**
     * Select the original atmosphere for one 100-level world, 0..9. Repeated
     * requests for the same world do not regenerate PCM or queue extra jobs.
     * The previous track fades briefly while the worker prepares its successor.
     */
    public void setBiome(int biome) {
        if (biome < 0 || biome >= BIOME_COUNT) throw new IllegalArgumentException("Biome must be 0..9");
        synchronized (lock) {
            if (released.get() || requestedBiome == biome) return;
            requestedBiome = biome;
            biomeGeneration++;
            updateMusicLocked();
            if (!musicWorkerQueued) {
                musicWorkerQueued = true;
                worker.execute(this::prepareRequestedMusic);
            }
        }
    }

    public int getBiome() { synchronized (lock) { return requestedBiome; } }

    /** Wait only on the background worker; the UI remains free to fade or cancel. */
    private boolean awaitPreviousFade(long generation) {
        long deadline = System.nanoTime() + 280_000_000L;
        while (true) {
            synchronized (lock) {
                if (released.get() || generation != biomeGeneration || Thread.currentThread().isInterrupted()) return false;
                if (music == null || music.getPlayState() != AudioTrack.PLAYSTATE_PLAYING || appliedMusicGain <= .0001f)
                    return true;
                if (System.nanoTime() >= deadline) {
                    // A stalled main looper must not retain an obsolete player indefinitely.
                    stopMusicLocked();
                    return true;
                }
            }
            try { Thread.sleep(10); }
            catch (InterruptedException cancelled) { Thread.currentThread().interrupt(); return false; }
        }
    }

    public void play(Sound sound) {
        if (sound == null || released.get()) return;
        synchronized (lock) {
            if (released.get() || !running || !sfxEnabled || !focusGranted || focusSuppressed) return;
            Integer id = samples.get(sound);
            if (id == null || !loaded.contains(id)) return;
            float volume = sfxVolume * (ducked ? 0.25f : 1f);
            try { pool.play(id, volume, volume, sound == Sound.WIN ? 2 : 1, 0, 1f); }
            catch (RuntimeException ignored) { }
        }
    }

    public void setSfxEnabled(boolean enabled) {
        synchronized (lock) {
            if (released.get()) return;
            sfxEnabled = enabled;
            if (!enabled && !released.get()) pool.autoPause();
            if (enabled && running && !focusGranted) requestFocusLocked();
            if (!enabled && !musicEnabled) abandonFocusLocked();
            updateMusicLocked();
        }
    }

    public void setMusicEnabled(boolean enabled) {
        synchronized (lock) {
            if (released.get()) return;
            musicEnabled = enabled;
            if (running && enabled && !focusGranted) requestFocusLocked();
            if (!enabled && !sfxEnabled) abandonFocusLocked();
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
            fades.removeCallbacks(fadeTick);
            fading = false;
            biomeGeneration++;
            abandonFocusLocked();
            if (music != null) {
                releaseTrack(music);
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
            if (!focusGranted) Log.i(TAG, "Audio focus unavailable; audio remains paused");
        } catch (RuntimeException failed) {
            focusSuppressed = true;
            Log.w(TAG, "Could not request audio focus", failed);
        }
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
            if (!running || !focusGranted || focusSuppressed) {
                stopMusicLocked();
                return;
            }
            if (loadedBiome != requestedBiome) {
                if (music.getPlayState() == AudioTrack.PLAYSTATE_PLAYING && !pauseAfterFade)
                    fadeMusicLocked(0f, 180, true);
                return;
            }
            if (!musicEnabled) {
                if (music.getPlayState() == AudioTrack.PLAYSTATE_PLAYING && !pauseAfterFade) {
                    fadeMusicLocked(0f, 180, true);
                }
                return;
            }
            float target = musicVolume * (ducked ? 0.2f : 1f);
            if (music.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                appliedMusicGain = 0f;
                music.setVolume(0f);
                music.play();
                Log.i(TAG, "Ambient playing; fade to " + target);
                fadeMusicLocked(target, 900, false);
            } else if (Math.abs(target - fadeTo) > 0.0001f || pauseAfterFade) {
                fadeMusicLocked(target, ducked ? 180 : 450, false);
            }
        } catch (RuntimeException failed) { Log.w(TAG, "Could not update ambient playback", failed); }
    }

    private void stopMusicLocked() {
        fades.removeCallbacks(fadeTick);
        fading = false;
        pauseAfterFade = false;
        appliedMusicGain = 0f;
        music.setVolume(0f);
        if (music.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            music.pause();
            Log.i(TAG, "Ambient paused");
        }
    }

    private void fadeMusicLocked(float target, long duration, boolean pauseWhenSilent) {
        fades.removeCallbacks(fadeTick);
        fadeFrom = appliedMusicGain;
        fadeTo = target;
        fadeStarted = SystemClock.uptimeMillis();
        fadeDuration = duration;
        pauseAfterFade = pauseWhenSilent;
        fading = true;
        fades.post(fadeTick);
    }

    private void advanceFade() {
        synchronized (lock) {
            if (released.get() || music == null || !fading) return;
            float progress = Math.min(1f, (SystemClock.uptimeMillis() - fadeStarted) / (float) fadeDuration);
            float eased = progress * progress * (3f - 2f * progress);
            appliedMusicGain = fadeFrom + (fadeTo - fadeFrom) * eased;
            try {
                music.setVolume(appliedMusicGain);
                if (progress < 1f) fades.postDelayed(fadeTick, 20);
                else {
                    fading = false;
                    if (pauseAfterFade) stopMusicLocked();
                }
            } catch (RuntimeException failed) {
                fading = false;
                Log.w(TAG, "Could not fade ambient playback", failed);
            }
        }
    }

    private static float clampVolume(float value) {
        return Float.isNaN(value) ? 0f : Math.max(0f, Math.min(1f, value));
    }

    private static void releaseTrack(AudioTrack track) {
        if (track == null) return;
        try { track.pause(); } catch (RuntimeException ignored) { }
        try { track.release(); } catch (RuntimeException ignored) { }
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
