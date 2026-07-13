package com.elikill58.negativity.proxy;

import org.jetbrains.annotations.Nullable;

/**
 * Validates plugin-message origin. Never trust client-originated payloads for bans/alerts.
 */
public final class ProxyMessageValidator {

    private ProxyMessageValidator() {
    }

    public static boolean isTrustedServerSender(boolean senderIsServer, boolean senderIsPlayer) {
        // Backend server → proxy is trusted. Player → proxy is not.
        return senderIsServer && !senderIsPlayer;
    }

    public static boolean isPlausibleChannel(@Nullable String tag) {
        if (tag == null) {
            return false;
        }
        String lower = tag.toLowerCase();
        return lower.equals(ProxyChannels.CHANNEL) || lower.contains("negativity");
    }
}
