package com.elikill58.negativity.common.proxy;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxyAlertCodecTest {

    @Test
    void roundTrip() throws Exception {
        UUID id = UUID.randomUUID();
        ProxyAlertCodec.AlertPayload original = new ProxyAlertCodec.AlertPayload(
                id, "Steve", "speed", "A", 8.5, 4.2, 55, 19.9, "lobby", "h=0.5"
        );
        byte[] raw = ProxyAlertCodec.writeAlert(original);
        ProxyAlertCodec.AlertPayload decoded = ProxyAlertCodec.readAlert(raw);
        assertEquals(original.playerId(), decoded.playerId());
        assertEquals(original.playerName(), decoded.playerName());
        assertEquals(original.checkId(), decoded.checkId());
        assertEquals(original.subcheck(), decoded.subcheck());
        assertEquals(original.vl(), decoded.vl(), 0.0001);
        assertEquals(original.ping(), decoded.ping());
        assertEquals(original.debug(), decoded.debug());
    }
}
