package com.androidhit.lumadrift.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.Handler;
import java.io.File;
import java.nio.file.Files;
import java.util.function.BooleanSupplier;

/** Deterministic JVM model of Android audio state/focus; this is not an emulator test. */
public final class AudioEngineTest {
    private static int checks;
    public static void main(String[] args) throws Exception {
        AudioManager manager = new AudioManager();
        AudioTrack.lastCreated = null;
        AudioEngine audio = engine(manager, false);
        audio.resume();
        AudioTrack track = awaitTrack();
        await(() -> track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING, "Ambient starts after preparation");
        check(track.writes == 1 && track.looped, "STATE_NO_STATIC_DATA is filled before loop/play");
        check(track.volume == 0f, "Playback starts silently for the fade");
        Handler.advanceBy(450);
        check(track.volume > .1f && track.volume < .22f, "The entrance fades progressively");
        Handler.advanceBy(500);
        check(close(track.volume, AudioEngine.DEFAULT_MUSIC_VOLUME), "The default level is reached");
        check(Handler.pending() == 0, "No permanent gain polling when settled");
        SoundPool effects = SoundPool.lastCreated;
        audio.play(AudioEngine.Sound.MOVE);
        check(effects.played == 1, "Effects remain available over the ambient music");

        audio.setMusicEnabled(false);
        Handler.advanceBy(80);
        check(track.volume > 0 && track.volume < AudioEngine.DEFAULT_MUSIC_VOLUME, "Music toggle fades out");
        audio.setMusicEnabled(true);
        Handler.advanceBy(500);
        check(close(track.volume, AudioEngine.DEFAULT_MUSIC_VOLUME), "A quick toggle reverses the current fade");
        audio.setMusicEnabled(false);
        Handler.advanceBy(200);
        check(track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED && track.volume == 0, "Disabled music becomes silent and paused");
        audio.setSfxEnabled(true);
        Handler.advanceBy(1000);
        check(track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED, "The SFX switch does not enable music");
        audio.setMusicEnabled(true);
        Handler.advanceBy(1000);

        manager.dispatch(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
        Handler.advanceBy(200);
        check(close(track.volume, AudioEngine.DEFAULT_MUSIC_VOLUME * .2f), "Another app can duck the music");
        manager.dispatch(AudioManager.AUDIOFOCUS_GAIN);
        Handler.advanceBy(500);
        check(close(track.volume, AudioEngine.DEFAULT_MUSIC_VOLUME), "Focus gain restores the level smoothly");
        manager.dispatch(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
        check(track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED && track.volume == 0, "Transient focus loss stops output immediately");
        audio.play(AudioEngine.Sound.MOVE);
        check(effects.played == 1, "Effects respect focus loss too");
        manager.dispatch(AudioManager.AUDIOFOCUS_GAIN);
        Handler.advanceBy(1000);
        check(track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING, "Focus gain resumes the same track");

        audio.pause();
        check(!manager.held && track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED, "Background pauses and relinquishes focus");
        check(track.volume == 0 && Handler.pending() == 0, "Background leaves no delayed audible fade");
        manager.dispatch(AudioManager.AUDIOFOCUS_GAIN);
        Handler.advanceBy(1000);
        check(track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED, "A late callback cannot restart background music");
        audio.resume();
        Handler.advanceBy(1000);
        check(AudioTrack.lastCreated == track && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING,
                "Resume reuses the track rather than creating a second player");
        audio.setMusicVolume(Float.NaN);
        Handler.advanceBy(500);
        check(track.volume == 0, "Invalid volume is silenced");
        audio.release();
        audio.release();
        audio.resume();
        audio.setMusicEnabled(true);
        audio.setSfxEnabled(true);
        Handler.advanceBy(1500);
        check(track.released && effects.released && !manager.held, "Release is idempotent and frees audio objects");
        check(Handler.pending() == 0 && track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING, "Release leaves no runnable/player leak");

        AudioManager denied = new AudioManager();
        denied.result = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        AudioTrack.lastCreated = null;
        AudioEngine deniedAudio = engine(denied, false);
        deniedAudio.resume();
        AudioTrack deniedTrack = awaitTrack();
        Handler.advanceBy(1200);
        check(deniedTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING, "A denied focus request cannot start music");
        deniedAudio.play(AudioEngine.Sound.MOVE);
        check(SoundPool.lastCreated.played == 0, "A denied focus request cannot start effects");
        denied.result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        deniedAudio.resume();
        Handler.advanceBy(1000);
        check(deniedTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING, "A later foreground resume can recover focus");
        deniedAudio.release();

        AudioTrack.lastCreated = null;
        AudioEngine cacheFailure = engine(new AudioManager(), true);
        cacheFailure.resume();
        AudioTrack fallback = awaitTrack();
        await(() -> fallback.getPlayState() == AudioTrack.PLAYSTATE_PLAYING, "Music survives an unavailable effects cache");
        cacheFailure.release();

        AudioTrack.lastCreated = null;
        AudioEngine muted = engine(new AudioManager(), false);
        muted.setMusicEnabled(false);
        muted.resume();
        AudioTrack mutedTrack = awaitTrack();
        Handler.advanceBy(1500);
        check(mutedTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING, "A saved disabled preference also applies before preparation");
        muted.release();
        check(Handler.pending() == 0, "Every fixture leaves no pending gain work");
        AudioTrack.lastCreated = null;
        AudioEngine pausedBeforeReady = engine(new AudioManager(), false);
        pausedBeforeReady.resume();
        pausedBeforeReady.pause();
        AudioTrack waiting = awaitTrack();
        Handler.advanceBy(1500);
        check(waiting.getPlayState() != AudioTrack.PLAYSTATE_PLAYING,
                "Finishing preparation after backgrounding cannot start music");
        pausedBeforeReady.release();
        check(Handler.pending() == 0, "Preparation/background race leaves no pending fade");
        System.out.println(checks + " audio lifecycle/focus regression checks passed (JVM Android model)");
    }

    private static AudioEngine engine(AudioManager manager, boolean unavailableCache) throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"), "luma-audio-model");
        root.mkdirs();
        File cache = unavailableCache ? Files.createTempFile(root.toPath(), "blocked-cache", ".file").toFile()
                : Files.createTempDirectory(root.toPath(), "cache").toFile();
        return new AudioEngine(new Context(cache, manager));
    }
    private static AudioTrack awaitTrack() throws Exception {
        await(() -> AudioTrack.lastCreated != null && AudioTrack.lastCreated.writes == 1,
                "Background preparation writes the static PCM");
        // The worker assigns the prepared track immediately after the write and loop check.
        Thread.sleep(20);
        return AudioTrack.lastCreated;
    }
    private static void await(BooleanSupplier ready, String message) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (!ready.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(10);
        check(ready.getAsBoolean(), message);
    }
    private static boolean close(float value, float target) { return Math.abs(value - target) < .0001f; }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
}
