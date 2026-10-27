package com.androidhit.lumadrift.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Actual PCM checks and export; the test does not claim Android speaker playback. */
public final class AudioBiomeTest {
    private static int checks;
    private static final String[] NAMES = {"First Light", "Mint Meadows", "Peach Coast", "Cloud Gardens",
            "Crystal Lagoon", "Star Orchard", "Moon Festival", "Pumpkin Grove", "Rose Aurora", "Dream Summit"};

    public static void main(String[] args) throws Exception {
        Path directory = Paths.get(args.length == 0 ? "docs/validation/v1.7/audio" : args[0]);
        Files.createDirectories(directory);
        StringBuilder historical = new StringBuilder("AMBIENT ").append(hash(Synth.ambientLoop())).append('\n');
        for (AudioEngine.Sound sound : AudioEngine.Sound.values()) historical.append(sound.name()).append(' ')
                .append(hash(Synth.effect(sound.name()))).append('\n');
        String baseline = Files.readString(Paths.get("docs/validation/v1.7/audio/legacy-pcm.sha256"));
        check(historical.toString().equals(baseline), "Original no-argument theme and all eleven effects retain historical PCM identity");
        check(Arrays.equals(Synth.ambientLoop(), Synth.ambientLoop(0)), "Biome zero is the historical theme byte for byte");
        check(AudioEngine.BIOME_COUNT == Synth.BIOME_COUNT && Synth.BIOME_COUNT == 10, "Ten biomes are available");

        StringBuilder csv = new StringBuilder("biome,name,bpm,seconds,pcm_bytes,rms,peak,dc_samples,seam_step,max_adjacent_step,sha256\n");
        Set<String> hashes = new HashSet<>();
        Set<Integer> lengths = new HashSet<>();
        short[] preview = new short[Synth.RATE * NAMES.length * 2];
        int biggest = 0;
        for (int biome = 0; biome < Synth.BIOME_COUNT; biome++) {
            short[] pcm = Synth.ambientLoop(biome);
            check(Arrays.equals(pcm, Synth.ambientLoop(biome)), "Every composition is deterministic: " + biome);
            String sha = hash(pcm);
            check(hashes.add(sha), "Every composition has different PCM");
            check(lengths.add(pcm.length), "Every arrangement has its own tempo/duration");
            double duration = pcm.length / (double) Synth.RATE;
            check(duration >= 20 && duration <= 30, "One bounded eight-bar loop, no long recording cache");
            check(pcm.length == Math.round(32 * 60.0 / Synth.biomeTempo(biome) * Synth.RATE), "PCM length matches the arrangement tempo");
            double squares = 0, sum = 0;
            int peak = 0, maxStep = 0;
            for (int i = 0; i < pcm.length; i++) {
                sum += pcm[i]; squares += pcm[i] * (double) pcm[i];
                peak = Math.max(peak, Math.abs((int) pcm[i]));
                if (i > 0) maxStep = Math.max(maxStep, Math.abs(pcm[i] - pcm[i - 1]));
            }
            double rms = Math.sqrt(squares / pcm.length) / 32767, dc = sum / pcm.length;
            int seam = Math.abs(pcm[0] - pcm[pcm.length - 1]);
            check(rms >= .115 && rms <= .141, "Audible consistent RMS: " + biome);
            check(peak < .621 * 32767, "All arrangements leave effects headroom");
            check(Math.abs(dc) < 1, "No material DC offset");
            check(seam <= maxStep && seam < .012 * 32767, "No exceptional wrapped loop boundary: " + biome);
            for (int second = 0; second < (int) duration; second++) {
                double energy = 0;
                for (int i = second * Synth.RATE; i < (second + 1) * Synth.RATE; i++) energy += pcm[i] * (double) pcm[i];
                check(Math.sqrt(energy / Synth.RATE) / 32767 > .035, "No silent second in a biome loop");
            }
            check(pcm.length * 2 < 1_300_000, "One static PCM buffer stays below 1.3 MB");
            biggest = Math.max(biggest, pcm.length * 2);
            csv.append(biome).append(',').append(NAMES[biome]).append(',').append(Synth.biomeTempo(biome)).append(',')
                    .append(String.format(Locale.ROOT, "%.6f,%d,%.6f,%.6f,%.6f,%d,%d,", duration, pcm.length * 2,
                            rms, peak / 32767.0, dc, seam, maxStep)).append(sha).append('\n');
            // An original 20-second montage: two seconds per theme at the actual default gain.
            for (int i = 0; i < Synth.RATE * 2; i++) {
                double t = i / (double) Synth.RATE;
                double fade = Math.min(1, t / .22) * Math.min(1, (2 - t) / .22);
                preview[biome * Synth.RATE * 2 + i] = (short) Math.round(pcm[i] * AudioEngine.DEFAULT_MUSIC_VOLUME * fade);
            }
        }
        for (int invalid : new int[]{-1,10,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
            try { Synth.ambientLoop(invalid); throw new AssertionError("Invalid biome accepted"); }
            catch (IllegalArgumentException expected) { checks++; }
        }
        Files.write(directory.resolve("audio-biome-metrics.csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
        writeWave(directory.resolve("audio-biome-preview.wav"), preview);
        Files.writeString(directory.resolve("audio-biome-memory.txt"), "Largest PCM16 static buffer: " + biggest
                + " bytes. The synthesizer allocates one double[] mix (4x PCM bytes) plus its resulting short[]; the engine retains no array cache for all ten themes. At most the current native track and one replacement native track coexist during handoff. Actual Android/native allocation measurements require runtime profiling.\n");
        System.out.println("PASS " + checks + " biome DSP checks; ten distinct eight-bar arrangements, deterministic PCM,"
                + " historical theme/SFX identity, loudness/DC/headroom/seams, bounded buffers; largest PCM=" + biggest + " bytes.");
        System.out.println("Preview is a 20-second rendered montage at default gain, not a recording of device playback.");
    }

    private static String hash(short[] pcm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (short value : pcm) { digest.update((byte) value); digest.update((byte) (value >>> 8)); }
        StringBuilder text = new StringBuilder();
        for (byte value : digest.digest()) text.append(String.format(Locale.ROOT, "%02x", value));
        return text.toString();
    }
    private static void writeWave(Path file, short[] pcm) throws Exception {
        ByteBuffer bytes = ByteBuffer.allocate(44 + pcm.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        bytes.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(36 + pcm.length * 2)
                .put("WAVEfmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16).putShort((short)1).putShort((short)1)
                .putInt(Synth.RATE).putInt(Synth.RATE * 2).putShort((short)2).putShort((short)16)
                .put("data".getBytes(StandardCharsets.US_ASCII)).putInt(pcm.length * 2);
        for (short value : pcm) bytes.putShort(value);
        Files.write(file, bytes.array());
    }
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
}
