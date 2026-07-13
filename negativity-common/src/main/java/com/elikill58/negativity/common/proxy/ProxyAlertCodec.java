package com.elikill58.negativity.common.proxy;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Binary protocol for backend → proxy alerts.
 * Format: magic(2) + version(1) + fields...
 */
public final class ProxyAlertCodec {

    public static final short MAGIC = (short) 0x4E47; // NG
    public static final byte VERSION = 2;
    public static final byte TYPE_ALERT = 1;

    public record AlertPayload(
            @NotNull UUID playerId,
            @NotNull String playerName,
            @NotNull String checkId,
            @NotNull String subcheck,
            double vl,
            double buffer,
            int ping,
            double tps,
            @NotNull String server,
            @NotNull String debug
    ) {
    }

    private ProxyAlertCodec() {
    }

    public static byte[] writeAlert(@NotNull AlertPayload payload) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bos)) {
            out.writeShort(MAGIC);
            out.writeByte(VERSION);
            out.writeByte(TYPE_ALERT);
            out.writeLong(payload.playerId().getMostSignificantBits());
            out.writeLong(payload.playerId().getLeastSignificantBits());
            writeString(out, payload.playerName());
            writeString(out, payload.checkId());
            writeString(out, payload.subcheck());
            out.writeDouble(payload.vl());
            out.writeDouble(payload.buffer());
            out.writeInt(payload.ping());
            out.writeDouble(payload.tps());
            writeString(out, payload.server());
            writeString(out, payload.debug());
        }
        return bos.toByteArray();
    }

    public static AlertPayload readAlert(byte[] data) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            short magic = in.readShort();
            if (magic != MAGIC) {
                throw new IOException("Bad magic");
            }
            byte version = in.readByte();
            if (version != VERSION) {
                throw new IOException("Unsupported protocol version: " + version);
            }
            byte type = in.readByte();
            if (type != TYPE_ALERT) {
                throw new IOException("Unsupported message type: " + type);
            }
            UUID id = new UUID(in.readLong(), in.readLong());
            return new AlertPayload(
                    id,
                    readString(in),
                    readString(in),
                    readString(in),
                    in.readDouble(),
                    in.readDouble(),
                    in.readInt(),
                    in.readDouble(),
                    readString(in),
                    readString(in)
            );
        }
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] bytes = (s == null ? "" : s).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 1024) {
            byte[] cut = new byte[1024];
            System.arraycopy(bytes, 0, cut, 0, 1024);
            bytes = cut;
        }
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        if (len > 2048) {
            throw new IOException("String too long: " + len);
        }
        byte[] bytes = in.readNBytes(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
