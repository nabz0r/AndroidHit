package com.androidhit.lumadrift.audio;

/** Pure Java DSP, deterministic and intentionally independent from Android for offline checks. */
final class Synth {
    static final int RATE = 22050;
    static final int BIOME_COUNT = 10;
    private static final double TAU = Math.PI * 2;
    // Theme zero remains the original recording below. Each other arrangement
    // has its own tempo, mode, chord order, melodic contour, rhythm and voice.
    private static final int[] BIOME_BPM = {75, 84, 68, 92, 72, 80, 66, 96, 78, 88};
    private static final int[] TONICS = {48, 50, 45, 53, 43, 46, 40, 52, 47, 49};
    private static final int[][] MODES = {
            {0,2,4,5,7,9,11}, {0,2,4,6,7,9,11}, {0,2,3,5,7,9,10},
            {0,2,4,5,7,9,11}, {0,2,3,5,7,8,10}, {0,2,4,5,7,9,10},
            {0,2,3,5,7,8,10}, {0,2,4,6,7,9,11}, {0,2,3,5,7,9,10},
            {0,2,4,5,7,9,11}
    };
    private static final int[][] PROGRESSIONS = {
            {0,5,3,4,0,5,3,4}, {0,1,3,4,0,5,1,4}, {0,3,6,4,0,5,3,4},
            {0,4,5,3,1,4,0,4}, {0,5,2,6,3,0,5,4}, {0,6,3,0,5,3,1,4},
            {0,2,5,3,0,6,3,4}, {0,1,4,5,3,1,0,4}, {0,3,4,6,0,2,3,4},
            {0,5,1,4,3,0,1,4}
    };
    private static final int[][] MOTIFS = {
            {9,11,8,7,9,11,10,8}, {7,9,11,10,8,9,12,11}, {9,7,6,8,10,9,7,11},
            {7,11,9,12,10,8,9,7}, {7,9,8,6,7,10,9,8}, {9,11,10,7,8,10,12,9},
            {11,9,7,8,6,7,9,10}, {7,9,12,11,9,13,10,11}, {9,8,11,7,10,9,12,8},
            {7,9,11,12,10,8,9,14}
    };
    private static final double[][] RHYTHMS = {
            {.15,1.5,2.75}, {0,.75,1.5,2.5,3.25}, {.1,1.8,3.0},
            {0,1,1.75,3.0}, {.2,1.25,2.5,3.2}, {0,.6,1.5,2.6},
            {.15,1.65,2.85}, {0,.5,1.5,2,3.25}, {.1,1,2.25,3},
            {0,.75,1.75,2.5,3.5}
    };

    private Synth() { }

    static short[] effect(String name) {
        double length;
        switch (name) {
            case "WIN": length = 1.45; break;
            case "PURCHASE": length = 0.74; break;
            case "HINT": length = 0.64; break;
            case "LOSE": length = 0.60; break;
            case "GATE": length = 0.46; break;
            case "UNDO": length = 0.36; break;
            case "COIN": length = 0.34; break;
            case "BLOCKED": length = 0.20; break;
            default: length = 0.25;
        }
        double[] output = new double[(int) (length * RATE)];
        switch (name) {
            case "TAP":
                bell(output, 0, 0.095, 880, 0.24);
                bell(output, 0, 0.06, 1320, 0.07);
                break;
            case "SELECT":
                bell(output, 0, 0.20, 659.255, 0.25);
                bell(output, 0.04, 0.18, 987.767, 0.17);
                break;
            case "MOVE":
                sweep(output, 0, 0.16, 430, 770, 0.18);
                bell(output, 0.035, 0.19, 1046.502, 0.13);
                break;
            case "BLOCKED":
                sweep(output, 0, 0.16, 220, 164.814, 0.22);
                bell(output, 0.025, 0.13, 329.628, 0.08);
                break;
            case "GATE":
                sweep(output, 0, 0.26, 329.628, 659.255, 0.12);
                bell(output, 0.10, 0.34, 783.991, 0.23);
                bell(output, 0.14, 0.31, 1174.659, 0.08);
                break;
            case "UNDO":
                sweep(output, 0, 0.29, 783.991, 391.995, 0.17);
                bell(output, 0.13, 0.22, 523.251, 0.13);
                break;
            case "HINT":
                notes(output, new int[] { 76, 79, 83 }, 0.105, 0.37, 0.22);
                break;
            case "COIN":
                bell(output, 0, 0.24, 1318.510, 0.19);
                bell(output, 0.045, 0.28, 1760, 0.17);
                break;
            case "PURCHASE":
                notes(output, new int[] { 72, 76, 79, 84 }, 0.095, 0.36, 0.23);
                bell(output, 0.31, 0.42, 1567.982, 0.10);
                break;
            case "WIN":
                notes(output, new int[] { 72, 76, 79, 83, 84 }, 0.115, 0.66, 0.20);
                bell(output, 0.48, 0.95, 523.251, 0.13);
                bell(output, 0.48, 0.94, 783.991, 0.10);
                break;
            case "LOSE":
                notes(output, new int[] { 67, 64, 60 }, 0.13, 0.32, 0.18);
                break;
            default: break;
        }
        return pcm(output, 0.8);
    }

    /** Original 25.6-second theme at 75 BPM, with overlapping notes and wrapped echo tails. */
    static short[] ambientLoop() {
        double beat = 0.8;
        double[] output = new double[(int) Math.round(32 * beat * RATE)];
        int[][] chords = { { 48, 55, 59, 64 }, { 45, 52, 55, 60 },
                { 41, 48, 52, 57 }, { 43, 50, 55, 59 },
                { 48, 55, 59, 62 }, { 45, 52, 55, 59 },
                { 41, 48, 55, 57 }, { 43, 50, 55, 62 } };
        int[][] melody = { { 76, 79, 74 }, { 72, 76, 79 },
                { 77, 76, 72 }, { 74, 71, 67 },
                { 76, 79, 83 }, { 81, 79, 76 },
                { 77, 74, 72 }, { 74, 71, 79 } };
        for (int bar = 0; bar < chords.length; bar++) {
            double start = bar * beat * 4;
            for (int note : chords[bar]) pad(output, start, beat * 5.3, hz(note), 0.047);
            pad(output, start, beat * 4.6, hz(chords[bar][0] - 12), 0.042);
            for (int step = 0; step < melody[bar].length; step++) {
                double t = start + (step == 0 ? 0.15 : step == 1 ? 1.5 : 2.75) * beat;
                double pitch = hz(melody[bar][step]);
                bell(output, t, 1.85, pitch, step == 0 ? 0.065 : 0.050);
                bell(output, t + beat * 0.75, 1.15, pitch, 0.016);
                bell(output, t + beat * 1.5, 0.85, pitch / 2, 0.008);
            }
            // A quiet lower-register answer gives pulse without a percussion loop.
            bell(output, start + beat * 2.1, 1.5, hz(chords[bar][1]), 0.024);
        }
        return levelMusic(output, 0.14, 0.62);
    }

    /** One original eight-bar arrangement, synthesized only when its biome changes. */
    static short[] ambientLoop(int biome) {
        requireBiome(biome);
        if (biome == 0) return ambientLoop();
        double beat = 60.0 / BIOME_BPM[biome];
        double[] output = new double[(int) Math.round(32 * beat * RATE)];
        int[] scale = MODES[biome], harmony = PROGRESSIONS[biome], motif = MOTIFS[biome];
        double[] rhythm = RHYTHMS[biome];
        for (int bar = 0; bar < 8; bar++) {
            if (Thread.currentThread().isInterrupted()) throw new java.util.concurrent.CancellationException("Ambient generation cancelled");
            double start = bar * 4 * beat;
            int degree = harmony[bar];
            for (int voice = 0; voice < 4; voice++) {
                int midi = scaleNote(TONICS[biome], scale, degree + voice * 2);
                cloud(output, start + voice * beat * .045, beat * (4.7 + (biome % 3) * .2),
                        hz(midi), .041 - voice * .003, biome);
            }
            cloud(output, start, beat * 4.25, hz(scaleNote(TONICS[biome] - 12, scale, degree)), .037, biome);
            for (int note = 0; note < rhythm.length; note++) {
                int index = (bar * 2 + note * (biome % 2 + 1)) % motif.length;
                int contour = motif[index] + (bar == 7 && note == rhythm.length - 1 ? 0 : degree % 3);
                double pitch = hz(scaleNote(TONICS[biome], scale, contour));
                double duration = beat * (biome % 3 == 0 ? 1.35 : biome % 3 == 1 ? 2.0 : 2.6);
                double onset = start + rhythm[note] * beat;
                chime(output, onset, duration, pitch, note == 0 ? .067 : .047, biome % 4);
                chime(output, onset + beat * (biome % 2 == 0 ? .75 : .5), duration * .7,
                        pitch * (biome % 3 == 0 ? .5 : 1), .012, biome % 4);
            }
            // Different low-register answers make the pulse change with the arrangement.
            double answerBeat = biome % 2 == 0 ? 2.0 : 2.75;
            chime(output, start + answerBeat * beat, beat * 1.4,
                    hz(scaleNote(TONICS[biome], scale, degree + 4)), .021, (biome + 1) % 4);
        }
        return levelMusic(output, .13, .60);
    }

    static int biomeTempo(int biome) { requireBiome(biome); return BIOME_BPM[biome]; }

    private static void requireBiome(int biome) {
        if (biome < 0 || biome >= BIOME_COUNT) throw new IllegalArgumentException("Biome must be 0..9");
    }

    private static int scaleNote(int tonic, int[] scale, int degree) {
        return tonic + Math.floorDiv(degree, scale.length) * 12 + scale[Math.floorMod(degree, scale.length)];
    }

    private static void cloud(double[] output, double start, double duration, double frequency, double gain, int colour) {
        int offset = (int) (start * RATE), count = (int) (duration * RATE);
        double overtone = .04 + (colour % 3) * .035;
        double detune = 1.001 + (colour % 4) * .00045;
        for (int i = 0; i < count; i++) {
            double t = i / (double) RATE, phase = TAU * frequency * t;
            double envelope = Math.pow(Math.sin(Math.PI * t / duration), 2);
            double breath = .93 + .07 * Math.sin(TAU * (.17 + colour * .012) * t);
            double tone = .8 * Math.sin(phase) + .2 * Math.sin(phase * detune)
                    + overtone * Math.sin(phase * 2) + .025 * Math.sin(phase * 3);
            output[(offset + i) % output.length] += gain * envelope * breath * tone;
        }
    }

    /** Soft felt, pluck, glass and wooden voices, all original oscillator envelopes. */
    private static void chime(double[] output, double start, double duration, double frequency, double gain, int voice) {
        int offset = (int) (start * RATE), count = (int) (duration * RATE);
        double[] decay = {4.2, 7.3, 3.8, 6.2};
        for (int i = 0; i < count; i++) {
            double t = i / (double) RATE, phase = TAU * frequency * t;
            double envelope = Math.min(1, t / .009) * Math.exp(-decay[voice] * t / duration)
                    * Math.min(1, (duration - t) / .045);
            double tone = Math.sin(phase);
            if (voice == 0) tone += .10 * Math.sin(phase * 2) * Math.exp(-t * 5);
            else if (voice == 1) tone += .14 * Math.sin(phase * 3) + .035 * Math.sin(phase * 5);
            else if (voice == 2) tone += .095 * Math.sin(phase * 2.76) * Math.exp(-t * 3);
            else tone += .3 * Math.sin(phase * 2) * Math.exp(-t * 13);
            output[(offset + i) % output.length] += gain * envelope * tone;
        }
    }

    private static void notes(double[] output, int[] notes, double interval, double duration, double gain) {
        for (int i = 0; i < notes.length; i++) bell(output, i * interval, duration, hz(notes[i]), gain);
    }

    private static void bell(double[] output, double start, double duration, double frequency, double gain) {
        int offset = (int) (start * RATE);
        int count = (int) (duration * RATE);
        for (int i = 0; i < count; i++) {
            double t = (double) i / RATE;
            double envelope = Math.min(1, t / 0.008) * Math.exp(-5.5 * t / duration)
                    * Math.min(1, (duration - t) / 0.035);
            double phase = TAU * frequency * t;
            double tone = Math.sin(phase) + 0.18 * Math.sin(phase * 2.001) * Math.exp(-t * 12)
                    + 0.065 * Math.sin(phase * 3.998) * Math.exp(-t * 20);
            output[(offset + i) % output.length] += gain * envelope * tone;
        }
    }

    private static void sweep(double[] output, double start, double duration, double from, double to, double gain) {
        int offset = (int) (start * RATE);
        int count = (int) (duration * RATE);
        for (int i = 0; i < count; i++) {
            double t = (double) i / RATE;
            double envelope = Math.sin(Math.PI * t / duration);
            double phase = TAU * (from * t + (to - from) * t * t / (2 * duration));
            output[(offset + i) % output.length] += gain * envelope * envelope
                    * (Math.sin(phase) + 0.11 * Math.sin(phase * 2));
        }
    }

    private static void pad(double[] output, double start, double duration, double frequency, double gain) {
        int offset = (int) (start * RATE);
        int count = (int) (duration * RATE);
        for (int i = 0; i < count; i++) {
            double t = (double) i / RATE;
            double envelope = Math.pow(Math.sin(Math.PI * t / duration), 2);
            double phase = TAU * frequency * t;
            double tone = Math.sin(phase) * 0.80 + Math.sin(phase * 1.002) * 0.20;
            output[(offset + i) % output.length] += gain * envelope * tone;
        }
    }

    private static double hz(int midi) { return 440 * Math.pow(2, (midi - 69) / 12.0); }

    private static short[] levelMusic(double[] audio, double targetRms, double ceiling) {
        double sum = 0;
        for (double value : audio) sum += value;
        double mean = sum / audio.length;
        double squares = 0;
        double peak = 0;
        for (int i = 0; i < audio.length; i++) {
            audio[i] -= mean;
            squares += audio[i] * audio[i];
            peak = Math.max(peak, Math.abs(audio[i]));
        }
        double rms = Math.sqrt(squares / audio.length);
        double gain = rms == 0 ? 0 : Math.min(targetRms / rms, ceiling / peak);
        for (int i = 0; i < audio.length; i++) audio[i] *= gain;
        return pcm(audio, ceiling);
    }

    private static short[] pcm(double[] audio, double ceiling) {
        double peak = 0;
        for (double value : audio) peak = Math.max(peak, Math.abs(value));
        double scale = peak > ceiling ? ceiling / peak : 1;
        short[] result = new short[audio.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (short) Math.round(Math.max(-1, Math.min(1, audio[i] * scale)) * 32767);
        }
        return result;
    }
}
