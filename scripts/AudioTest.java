package com.androidhit.lumadrift.audio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/** Checks the generated PCM itself, and exports the exact original composition used in the app. */
public final class AudioTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        short[] music = Synth.ambientLoop();
        check(Arrays.equals(music, Synth.ambientLoop()), "The original composition is deterministic");
        check(music.length == Synth.RATE * 256 / 10, "The complete phrase lasts 25.6 seconds");
        double rms = rms(music);
        int peak = peak(music);
        int step = Math.abs(music[0] - music[music.length - 1]);
        int maxStep = 0;
        double sum = 0;
        for (int i = 0; i < music.length; i++) {
            if (i > 0) maxStep = Math.max(maxStep, Math.abs(music[i] - music[i - 1]));
            sum += music[i];
        }
        check(rms >= .13 && rms <= .145, "Ambient level has an audible, bounded RMS");
        check(peak <= .621 * 32767, "Music leaves peak headroom for effects");
        check(Math.abs(sum / music.length) < 1, "No material DC offset");
        check(step <= maxStep && step < .008 * 32767, "The wrapped boundary has no exceptional step");
        for (int second = 0; second < 25; second++) {
            double energy = 0;
            for (int i = second * Synth.RATE; i < (second + 1) * Synth.RATE; i++) {
                energy += music[i] * (double) music[i];
            }
            check(Math.sqrt(energy / Synth.RATE) / 32767 > .04, "No silent one-second gap");
        }
        StringBuilder metrics = new StringBuilder("Original procedural audio, mono PCM16 / 22050 Hz.\n");
        metrics.append(String.format(Locale.ROOT,
                "Music: duration=%.3fs, peak=%.2f dBFS, RMS=%.2f dBFS, DC=%.3f samples\n"
                + "Music default gain=%.2f: peak=%.2f dBFS, RMS=%.2f dBFS\n"
                + "Loop boundary step=%d samples; largest adjacent step=%d samples\n",
                music.length / (double) Synth.RATE, db(peak / 32767.0), db(rms), sum / music.length,
                AudioEngine.DEFAULT_MUSIC_VOLUME, db(peak / 32767.0 * AudioEngine.DEFAULT_MUSIC_VOLUME),
                db(rms * AudioEngine.DEFAULT_MUSIC_VOLUME), step, maxStep));
        for (AudioEngine.Sound sound : AudioEngine.Sound.values()) {
            short[] effect = Synth.effect(sound.name());
            check(effect[0] == 0 && effect[effect.length - 1] == 0, sound + " has quiet endpoints");
            check(peak(effect) < 32767, sound + " does not clip");
            check(rms(effect) > .001, sound + " contains audio");
            metrics.append(String.format(Locale.ROOT, "%s: peak=%.2f dBFS, RMS=%.2f dBFS (before effect gain 0.72)\n",
                    sound, db(peak(effect) / 32767.0), db(rms(effect))));
        }
        check(peak(music) / 32767.0 * AudioEngine.DEFAULT_MUSIC_VOLUME
                        + peak(Synth.effect("MOVE")) / 32767.0 * .72 < .8,
                "Default ambient plus a move leaves substantial mix headroom");
        if (args.length > 0) {
            Path directory = new File(args[0]).toPath();
            Files.createDirectories(directory);
            short[] preview = new short[Synth.RATE * 16];
            for (int i = 0; i < preview.length; i++) {
                double t = i / (double) Synth.RATE;
                double fade = smooth(Math.min(1, t / .9)) * smooth(Math.min(1, (16 - t) / .45));
                preview[i] = (short) Math.round(music[i % music.length] * AudioEngine.DEFAULT_MUSIC_VOLUME * fade);
            }
            writeWave(directory.resolve("ambient-preview.wav"), preview);
            Files.writeString(directory.resolve("audio-levels.txt"), metrics
                    + "Preview: 16 seconds at the actual default music gain, with 900ms fade in and a preview-only 450ms ending.\n"
                    + "Checks cover PCM and a JVM Android lifecycle model, not a physical speaker audition.\n");
        }
        System.out.print(metrics);
        System.out.println(checks + " DSP checks passed");
    }

    private static double smooth(double x) { return x * x * (3 - 2 * x); }
    private static double db(double value) { return 20 * Math.log10(value); }
    private static double rms(short[] pcm) {
        double energy = 0;
        for (short sample : pcm) energy += sample * (double) sample;
        return Math.sqrt(energy / pcm.length) / 32767;
    }
    private static int peak(short[] pcm) {
        int result = 0;
        for (short sample : pcm) result = Math.max(result, Math.abs((int) sample));
        return result;
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    private static void writeWave(Path file, short[] pcm) throws IOException {
        ByteBuffer wav = ByteBuffer.allocate(44 + pcm.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        wav.put(new byte[] {'R', 'I', 'F', 'F'}).putInt(36 + pcm.length * 2);
        wav.put(new byte[] {'W', 'A', 'V', 'E', 'f', 'm', 't', ' '}).putInt(16);
        wav.putShort((short) 1).putShort((short) 1).putInt(Synth.RATE).putInt(Synth.RATE * 2);
        wav.putShort((short) 2).putShort((short) 16);
        wav.put(new byte[] {'d', 'a', 't', 'a'}).putInt(pcm.length * 2);
        for (short sample : pcm) wav.putShort(sample);
        Files.write(file, wav.array());
    }
}
