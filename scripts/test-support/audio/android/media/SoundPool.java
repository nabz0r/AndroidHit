package android.media;
public final class SoundPool {
    public interface OnLoadCompleteListener { void onLoadComplete(SoundPool pool, int sampleId, int status); }
    public static volatile SoundPool lastCreated;
    private OnLoadCompleteListener listener;
    private int nextId;
    public int played, pauses;
    public boolean released;
    public static final class Builder {
        public Builder setMaxStreams(int streams) { return this; }
        public Builder setAudioAttributes(AudioAttributes attributes) { return this; }
        public SoundPool build() { SoundPool pool = new SoundPool(); lastCreated = pool; return pool; }
    }
    public void setOnLoadCompleteListener(OnLoadCompleteListener listener) { this.listener = listener; }
    public int load(String path, int priority) { int id = ++nextId; listener.onLoadComplete(this, id, 0); return id; }
    public int play(int id, float left, float right, int priority, int loop, float rate) { played++; return played; }
    public void autoPause() { pauses++; }
    public void release() { released = true; }
}
