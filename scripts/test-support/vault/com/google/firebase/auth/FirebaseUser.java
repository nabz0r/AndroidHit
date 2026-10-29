package com.google.firebase.auth;
import com.google.android.gms.tasks.Task;
public final class FirebaseUser {
    private final String uid;
    public int tokenCalls;
    public Task<GetTokenResult> nextTask;
    public FirebaseUser(String uid){this.uid=uid;}
    public String getUid(){return uid;}
    public String getDisplayName(){return "Test Google";}
    public Task<GetTokenResult> getIdToken(boolean force){tokenCalls++;Task<GetTokenResult> task=nextTask;nextTask=null;return task==null?new Task<>(false,null,false):task;}
}
