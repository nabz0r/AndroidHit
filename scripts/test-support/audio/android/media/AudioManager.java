package android.media;
public final class AudioManager {
    public static final int AUDIOFOCUS_GAIN = 1, AUDIOFOCUS_LOSS = -1,
            AUDIOFOCUS_LOSS_TRANSIENT = -2, AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3,
            AUDIOFOCUS_REQUEST_GRANTED = 1, AUDIOFOCUS_REQUEST_FAILED = 0;
    public interface OnAudioFocusChangeListener { void onAudioFocusChange(int change); }
    public int result = AUDIOFOCUS_REQUEST_GRANTED;
    public int requests, abandons;
    public boolean held;
    private AudioFocusRequest request;
    public int requestAudioFocus(AudioFocusRequest request) {
        this.request = request; requests++; held = result == AUDIOFOCUS_REQUEST_GRANTED; return result;
    }
    public int abandonAudioFocusRequest(AudioFocusRequest request) { abandons++; held = false; return 1; }
    public void dispatch(int change) { if (request != null) request.listener.onAudioFocusChange(change); }
}
