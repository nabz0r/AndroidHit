package com.androidhit.lumadrift.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** Controllable late native writes and lifecycle races in the JVM Android model. */
public final class AudioBiomeEngineTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        AudioTrack.resetTestState();
        AudioManager manager = new AudioManager();
        AudioEngine audio = new AudioEngine(new Context(Files.createTempDirectory("luma-biome-audio").toFile(), manager));
        audio.resume();
        AudioTrack initial = awaitPlaying(0);
        Handler.advanceBy(1000);
        check(initial.volume > .26f, "Original theme is audible before switching");
        for (int biome = 1; biome < 10; biome++) {
            AudioTrack old = AudioTrack.lastCreated;
            audio.setBiome(biome);
            check(audio.getBiome() == biome, "Requested biome is visible synchronously");
            Handler.advanceBy(80);
            check(old.volume < .27f, "Old music fades during preparation");
            Handler.advanceBy(160);
            AudioTrack fresh = awaitPlaying(biome);
            await(() -> old.released, "Replaced native track is released");
            Handler.advanceBy(1000);
            check(fresh != old && fresh.volume > .26f, "Replacement fades in at the saved gain");
            check(AudioTrack.liveCount == 1 && AudioTrack.maximumLiveCount <= 2, "One current track, at most one in-flight candidate");
            int count = AudioTrack.createdCount;
            for (int repeat = 0; repeat < 100; repeat++) audio.setBiome(biome);
            Thread.sleep(15);
            check(AudioTrack.createdCount == count, "Repeated same-world calls never create another player");
        }

        AudioTrack current = AudioTrack.lastCreated;
        holdNextWrite();
        audio.setBiome(2);
        AudioTrack stale = awaitBlocked();
        for (int i = 0; i < 1000; i++) audio.setBiome(i % 10);
        audio.setBiome(6);
        Handler.advanceBy(250);
        int before = AudioTrack.createdCount;
        releaseWrite();
        AudioTrack sixth = awaitPlaying(6);
        check(stale.released && stale.plays == 0, "A late obsolete write is released without ever playing");
        check(current.released && AudioTrack.createdCount == before + 1, "A thousand intermediate requests coalesce into the last theme");
        check(AudioTrack.liveCount == 1 && AudioTrack.maximumLiveCount <= 2, "Rapid map scrolling does not retain ten loops");
        Handler.advanceBy(1000);

        holdNextWrite();
        audio.setBiome(7);
        AudioTrack cancelled = awaitBlocked();
        audio.setBiome(6);
        releaseWrite();
        await(() -> cancelled.released, "Returning to the current world discards its obsolete replacement");
        Handler.advanceBy(1000);
        check(AudioTrack.liveCount == 1 && !sixth.released && sixth.getPlayState() == AudioTrack.PLAYSTATE_PLAYING,
                "Returning to current world reuses the existing track");

        holdNextWrite();
        audio.setBiome(8);
        AudioTrack background = awaitBlocked();
        audio.pause();
        releaseWrite();
        await(() -> sixth.released && background.looped, "A background replacement may prepare but releases its predecessor");
        manager.dispatch(AudioManager.AUDIOFOCUS_GAIN);
        Handler.advanceBy(1500);
        check(background.plays == 0 && !manager.held && Handler.pending() == 0, "Late completion/focus gain cannot start background music");
        audio.resume();
        await(() -> background.getPlayState() == AudioTrack.PLAYSTATE_PLAYING, "Foreground resume starts the prepared requested theme");
        Handler.advanceBy(1000);

        holdNextWrite();
        audio.setBiome(9);
        AudioTrack muted = awaitBlocked();
        audio.setMusicEnabled(false);
        Handler.advanceBy(250);
        releaseWrite();
        await(() -> background.released && muted.looped, "Muted replacement finishes without leaking predecessor");
        audio.setSfxEnabled(true);
        Handler.advanceBy(1000);
        check(muted.plays == 0, "New worlds respect saved music-off and independent effects preference");
        audio.setMusicEnabled(true);
        await(() -> muted.getPlayState() == AudioTrack.PLAYSTATE_PLAYING, "Music toggle resumes requested world");
        Handler.advanceBy(1000);

        AudioTrack.failNextWrite.set(true);
        audio.setBiome(3);
        await(() -> AudioTrack.lastCreated != muted && AudioTrack.lastCreated.released, "Failed short PCM write releases its candidate");
        Handler.advanceBy(250);
        check(AudioTrack.liveCount == 1, "Failed output keeps no leaked candidate");
        audio.setBiome(4);
        AudioTrack recovered = awaitPlaying(4);
        check(muted.released, "A later request recovers cleanly after output preparation failure");
        Handler.advanceBy(1000);
        for (int invalid : new int[]{-1,10}) {
            try { audio.setBiome(invalid); throw new AssertionError("Invalid biome accepted"); }
            catch (IllegalArgumentException expected) { checks++; }
        }
        check(audio.getBiome() == 4, "Rejected biome leaves requested music unchanged");

        holdNextWrite();
        audio.setBiome(1);
        AudioTrack late = awaitBlocked();
        audio.release();
        audio.release();
        audio.resume(); audio.setBiome(5); audio.setMusicEnabled(true);
        check(recovered.released && !manager.held, "Destruction immediately releases the current output/focus");
        releaseWrite();
        await(() -> late.released && AudioTrack.liveCount == 0, "A native write completing after destruction is also released");
        Handler.advanceBy(1500);
        check(late.plays == 0 && Handler.pending() == 0, "No stale candidate or fade restarts output after destruction");
        check(AudioTrack.maximumLiveCount <= 2, "All race scenarios obey the bounded-player invariant");
        int maximumLiveObserved = AudioTrack.maximumLiveCount;

        // The first worker is already queued by the constructor. Changing worlds
        // before that first write publishes must neither lose the request nor
        // briefly start the default atmosphere.
        AudioTrack.resetTestState();
        holdNextWrite();
        AudioEngine early = new AudioEngine(new Context(Files.createTempDirectory("luma-biome-early").toFile(), new AudioManager()));
        early.resume();
        AudioTrack firstCandidate = awaitBlocked();
        early.setBiome(9);
        releaseWrite();
        AudioTrack finalCandidate = awaitPlaying(9);
        check(firstCandidate.released && firstCandidate.plays == 0 && finalCandidate != firstCandidate,
                "A biome requested during initial preparation supersedes the initial composition");
        early.release();
        check(finalCandidate.released && AudioTrack.liveCount == 0 && Handler.pending() == 0,
                "Initial-preparation fixture frees every output and pending fade");
        System.out.println("PASS " + checks + " biome audio lifecycle/concurrency checks (JVM Android model): ten themes, fade/release,"
                + " 1000 coalesced requests, stale/initial write, returning world, pause/focus, mute, failed write, destruction; maximum live tracks="
                + Math.max(maximumLiveObserved, AudioTrack.maximumLiveCount) + ".");
    }

    private static AudioTrack awaitPlaying(int biome) throws Exception {
        int frames = (int) Math.round(32 * 60.0 / Synth.biomeTempo(biome) * Synth.RATE);
        await(() -> AudioTrack.lastCreated != null && AudioTrack.lastCreated.pcmLength == frames
                && AudioTrack.lastCreated.looped && AudioTrack.lastCreated.getPlayState() == AudioTrack.PLAYSTATE_PLAYING,
                "Requested arrangement is ready and playing: " + biome);
        await(() -> Handler.pending() > 0, "Replacement entrance fade is scheduled");
        return AudioTrack.lastCreated;
    }
    private static void holdNextWrite() {
        AudioTrack.writeEntered = new CountDownLatch(1); AudioTrack.allowWrite = new CountDownLatch(1);
        AudioTrack.blockNextWrite.set(true);
    }
    private static AudioTrack awaitBlocked() throws Exception {
        check(AudioTrack.writeEntered.await(5, TimeUnit.SECONDS), "Controlled native write is in flight");
        return AudioTrack.lastCreated;
    }
    private static void releaseWrite() { AudioTrack.allowWrite.countDown(); }
    private static void await(BooleanSupplier ready, String message) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (!ready.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(10);
        check(ready.getAsBoolean(), message);
    }
    private static void check(boolean good, String message) { checks++; if (!good) throw new AssertionError(message); }
}
