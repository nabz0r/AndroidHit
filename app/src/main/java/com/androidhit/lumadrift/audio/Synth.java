package com.androidhit.lumadrift.audio;

/** Pure Java DSP, deterministic and intentionally independent from Android for offline checks. */
final class Synth {
    static final int RATE = 22050;
    private static final double TAU = Math.PI * 2;

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

    /** A 12.8-second original ambient loop at 75 BPM; all note tails wrap exactly. */
    static short[] ambientLoop() {
        double beat = 0.8;
        double[] output = new double[(int) Math.round(16 * beat * RATE)];
        int[][] chords = { { 48, 55, 59, 64 }, { 45, 52, 55, 60 },
                { 41, 48, 52, 57 }, { 43, 50, 55, 59 } };
        int[][] melody = { { 76, 79, 83, 79 }, { 76, 72, 79, 76 },
                { 77, 81, 76, 72 }, { 74, 79, 83, 79 } };
        for (int bar = 0; bar < 4; bar++) {
            double start = bar * beat * 4;
            for (int note : chords[bar]) pad(output, start, beat * 4, hz(note), 0.034);
            for (int step = 0; step < 4; step++) {
                double t = start + step * beat + (step % 2 == 0 ? 0 : 0.075);
                bell(output, t, 1.15, hz(melody[bar][step]), 0.075);
                bell(output, t + 0.27, 0.65, hz(melody[bar][step]), 0.014);
            }
        }
        return pcm(output, 0.65);
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
