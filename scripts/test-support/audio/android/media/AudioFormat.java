package android.media;
public final class AudioFormat {
    public static final int ENCODING_PCM_16BIT = 2, CHANNEL_OUT_MONO = 4;
    public static final class Builder {
        public Builder setEncoding(int encoding) { return this; }
        public Builder setSampleRate(int rate) { return this; }
        public Builder setChannelMask(int mask) { return this; }
        public AudioFormat build() { return new AudioFormat(); }
    }
}
