package com.oracle.iot.model;

import com.oracle.json.JsonObject;

public class AccessToken {
	private final long expires;
	private final String tokenType;
	private final String token;
	private final long expirationTime;

	public AccessToken(long expires, String tokenType, String token) {
		this.expires = expires;
		this.tokenType = tokenType;
		this.token = token;
		this.expirationTime = System.currentTimeMillis() + expires;
	}

	public final boolean hasExpired() {
		return (System.currentTimeMillis() >= this.expirationTime);
	}

	public long getExpires() {
		return expires;
	}

	public String getTokenType() {
		return tokenType;
	}

	public String getToken() {
		return token;
	}

	@Override
	public String toString() {
		return "Accessor.Token{" + "expires=" + expires + ", tokenType='" + tokenType + '\'' + ", token='" + token
				+ '\'' + '}';
	}

	public static AccessToken fromJSON(final JsonObject jsonObject) {
		AccessToken token = new AccessToken(jsonObject.getInt("expires_in"), jsonObject.getString("token_type"),
				jsonObject.getString("access_token"));
		return token;
	}
}
