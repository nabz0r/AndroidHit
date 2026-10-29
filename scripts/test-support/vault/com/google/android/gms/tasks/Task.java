package com.google.android.gms.tasks;
public final class Task<T> {
    private final boolean successful,deferred;
    private final T result;
    private OnCompleteListener<T> listener;
    public Task(boolean successful,T result,boolean deferred){this.successful=successful;this.result=result;this.deferred=deferred;}
    public boolean isSuccessful(){return successful;}
    public T getResult(){return result;}
    public Exception getException(){return null;}
    public Task<T> addOnCompleteListener(OnCompleteListener<T> listener){this.listener=listener;if(!deferred)listener.onComplete(this);return this;}
    public void complete(){if(listener!=null)listener.onComplete(this);}
}
