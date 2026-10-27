package android.media;
public final class AudioTrack {
    public static final int MODE_STATIC = 0, STATE_UNINITIALIZED = 0, STATE_INITIALIZED = 1,
            STATE_NO_STATIC_DATA = 2, PLAYSTATE_STOPPED = 1, PLAYSTATE_PAUSED = 2,
            PLAYSTATE_PLAYING = 3, SUCCESS = 0;
    public static volatile AudioTrack lastCreated;
    public static volatile int createdCount, liveCount, maximumLiveCount;
    public static final java.util.concurrent.atomic.AtomicBoolean blockNextWrite = new java.util.concurrent.atomic.AtomicBoolean();
    public static final java.util.concurrent.atomic.AtomicBoolean failNextWrite = new java.util.concurrent.atomic.AtomicBoolean();
    public static volatile java.util.concurrent.CountDownLatch writeEntered, allowWrite;
    public volatile int state = STATE_NO_STATIC_DATA, playState = PLAYSTATE_STOPPED;
    public volatile boolean released, looped;
    public volatile int writes, plays;
    public volatile int pcmLength, bufferBytes;
    public volatile float volume = 1;
    public static final class Builder {
        private int bytes;
        public Builder setAudioAttributes(AudioAttributes attributes) { return this; }
        public Builder setAudioFormat(AudioFormat format) { return this; }
        public Builder setBufferSizeInBytes(int bytes) { this.bytes = bytes; return this; }
        public Builder setTransferMode(int mode) { return this; }
        public AudioTrack build() {
            AudioTrack track = new AudioTrack(); track.bufferBytes = bytes;
            synchronized (AudioTrack.class) {
                createdCount++; liveCount++; maximumLiveCount = Math.max(maximumLiveCount, liveCount);
            }
            lastCreated = track; return track;
        }
    }
    public int getState() { return state; }
    public int write(short[] samples, int offset, int length) {
        if (blockNextWrite.compareAndSet(true, false)) {
            java.util.concurrent.CountDownLatch entered = writeEntered, allowed = allowWrite;
            if (entered != null) entered.countDown();
            boolean interrupted = false;
            if (allowed != null) {
                while (true) {
                    try { allowed.await(); break; }
                    catch (InterruptedException ignored) { interrupted = true; }
                }
            }
            if (interrupted) Thread.currentThread().interrupt();
        }
        writes++; pcmLength = length;
        if (failNextWrite.compareAndSet(true, false)) return 0;
        state = STATE_INITIALIZED; return length;
    }
    public int setLoopPoints(int start, int end, int count) {
        if (state != STATE_INITIALIZED) return -1; looped = true; return SUCCESS;
    }
    public int setVolume(float gain) {
        if (released) throw new IllegalStateException("Released output");
        volume = gain; return SUCCESS;
    }
    public int getPlayState() { return playState; }
    public void play() {
        if (state != STATE_INITIALIZED || released) throw new IllegalStateException("PCM must be loaded first");
        plays++; playState = PLAYSTATE_PLAYING;
    }
    public void pause() { playState = PLAYSTATE_PAUSED; }
    public void release() {
        synchronized (AudioTrack.class) { if (!released) { released = true; liveCount--; } }
        playState = PLAYSTATE_STOPPED; state = STATE_UNINITIALIZED;
    }
    public static void resetTestState() {
        synchronized (AudioTrack.class) {
            if (liveCount != 0) throw new IllegalStateException("Previous test leaked an AudioTrack");
            createdCount = maximumLiveCount = 0;
        }
        lastCreated = null; blockNextWrite.set(false); failNextWrite.set(false);
        writeEntered = allowWrite = null;
    }
}
