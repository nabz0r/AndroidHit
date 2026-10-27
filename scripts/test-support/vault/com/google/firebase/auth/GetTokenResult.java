package com.google.firebase.auth;
public final class GetTokenResult {
    private final String token;
    public GetTokenResult(String token){this.token=token;}
    public String getToken(){return token;}
}
