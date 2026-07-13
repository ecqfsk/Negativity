package com.elikill58.negativity.proxy;

/**
 * Shared proxy channel constants. Messages must only be accepted from backend servers.
 */
public final class ProxyChannels {

    public static final String CHANNEL = "negativity:msg";
    public static final int PROTOCOL_VERSION = 2;

    private ProxyChannels() {
    }
}
