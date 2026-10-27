package android.media;
public final class AudioFocusRequest {
    AudioManager.OnAudioFocusChangeListener listener;
    public static final class Builder {
        private final AudioFocusRequest request = new AudioFocusRequest();
        public Builder(int gain) { }
        public Builder setAudioAttributes(AudioAttributes attributes) { return this; }
        public Builder setOnAudioFocusChangeListener(AudioManager.OnAudioFocusChangeListener listener) {
            request.listener = listener; return this;
        }
        public AudioFocusRequest build() { return request; }
    }
}
