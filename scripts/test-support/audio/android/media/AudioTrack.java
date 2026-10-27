package android.media;
public final class AudioTrack {
    public static final int MODE_STATIC = 0, STATE_UNINITIALIZED = 0, STATE_INITIALIZED = 1,
            STATE_NO_STATIC_DATA = 2, PLAYSTATE_STOPPED = 1, PLAYSTATE_PAUSED = 2,
            PLAYSTATE_PLAYING = 3, SUCCESS = 0;
    public static volatile AudioTrack lastCreated;
    public volatile int state = STATE_NO_STATIC_DATA, playState = PLAYSTATE_STOPPED;
    public volatile boolean released, looped;
    public volatile int writes, plays;
    public volatile float volume = 1;
    public static final class Builder {
        public Builder setAudioAttributes(AudioAttributes attributes) { return this; }
        public Builder setAudioFormat(AudioFormat format) { return this; }
        public Builder setBufferSizeInBytes(int bytes) { return this; }
        public Builder setTransferMode(int mode) { return this; }
        public AudioTrack build() { AudioTrack track = new AudioTrack(); lastCreated = track; return track; }
    }
    public int getState() { return state; }
    public int write(short[] samples, int offset, int length) { writes++; state = STATE_INITIALIZED; return length; }
    public int setLoopPoints(int start, int end, int count) {
        if (state != STATE_INITIALIZED) return -1; looped = true; return SUCCESS;
    }
    public int setVolume(float gain) { volume = gain; return SUCCESS; }
    public int getPlayState() { return playState; }
    public void play() {
        if (state != STATE_INITIALIZED || released) throw new IllegalStateException("PCM must be loaded first");
        plays++; playState = PLAYSTATE_PLAYING;
    }
    public void pause() { playState = PLAYSTATE_PAUSED; }
    public void release() { released = true; playState = PLAYSTATE_STOPPED; state = STATE_UNINITIALIZED; }
}
