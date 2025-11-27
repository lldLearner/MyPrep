// RowKeyCodec.java
package ParkingSpot.codec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Simple, robust codec for composite primary keys -> rowKey (byte[]).
 *
 * Encoding format (field repeated):
 *  [type (1 byte)] [length (4 bytes, big-endian)] [payload bytes]
 *
 * Type IDs:
 *  0x01 = INT32  (4 bytes big-endian)
 *  0x02 = INT64  (8 bytes big-endian)
 *  0x03 = UUID   (16 bytes, msb/lsb)
 *  0x04 = BYTES  (raw)
 *  0x05 = STRING (utf-8 bytes)
 *
 * Advantages: deterministic, binary-safe, easy to decode.
 *
 */
public final class RowKeyCodec {

    // Type markers
    private static final byte T_INT32 = 0x01;
    private static final byte T_INT64 = 0x02;
    private static final byte T_UUID  = 0x03;
    private static final byte T_BYTES = 0x04;
    private static final byte T_STRING = 0x05;

    private RowKeyCodec() {}

    // --------- Public API ---------
    public static byte[] encode(Object... parts) {
        // gather sizes then pack
        List<byte[]> chunks = new ArrayList<>(parts.length);
        int total = 0;
        for (Object p : parts) {
            byte[] chunk = encodePart(p);
            chunks.add(chunk);
            total += chunk.length;
        }
        ByteBuffer buf = ByteBuffer.allocate(total);
        for (byte[] c : chunks) buf.put(c);
        return buf.array();
    }

    public static List<Object> decode(byte[] rowKey) {
        List<Object> out = new ArrayList<>();
        ByteBuffer buf = ByteBuffer.wrap(rowKey);
        while (buf.remaining() > 0) {
            byte t = buf.get();
            int len = buf.getInt();
            if (len < 0 || len > buf.remaining()) {
                throw new IllegalArgumentException("Invalid encoding, len=" + len);
            }
            byte[] payload = new byte[len];
            buf.get(payload);
            out.add(decodePart(t, payload));
        }
        return out;
    }

    /**
     * Produce a shard id in [0, numShards-1] for the given rowKey.
     * Uses SHA-256 and take first 8 bytes as unsigned long for hashing.
     */
    public static int shardForKey(byte[] rowKey, int numShards) {
        if (numShards <= 0) throw new IllegalArgumentException("numShards>0");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(rowKey);
            // use first 8 bytes -> long
            long v = 0L;
            for (int i = 0; i < 8; i++) {
                v = (v << 8) | (h[i] & 0xFFL);
            }
            long unsigned = v & 0x7fffffffffffffffL; // positive
            return (int) (unsigned % numShards);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ---------- helpers ----------
    private static byte[] encodePart(Object p) {
        if (p == null) throw new IllegalArgumentException("null part not supported");
        if (p instanceof Integer) {
            ByteBuffer b = ByteBuffer.allocate(1 + 4 + 4);
            b.put(T_INT32);
            b.putInt(4);
            b.putInt((Integer) p);
            return b.array();
        } else if (p instanceof Long) {
            ByteBuffer b = ByteBuffer.allocate(1 + 4 + 8);
            b.put(T_INT64);
            b.putInt(8);
            b.putLong((Long) p);
            return b.array();
        } else if (p instanceof UUID) {
            ByteBuffer b = ByteBuffer.allocate(1 + 4 + 16);
            b.put(T_UUID);
            b.putInt(16);
            UUID u = (UUID) p;
            b.putLong(u.getMostSignificantBits());
            b.putLong(u.getLeastSignificantBits());
            return b.array();
        } else if (p instanceof byte[]) {
            byte[] payload = (byte[]) p;
            ByteBuffer b = ByteBuffer.allocate(1 + 4 + payload.length);
            b.put(T_BYTES);
            b.putInt(payload.length);
            b.put(payload);
            return b.array();
        } else { // fallback to string
            byte[] payload = p.toString().getBytes(StandardCharsets.UTF_8);
            ByteBuffer b = ByteBuffer.allocate(1 + 4 + payload.length);
            b.put(T_STRING);
            b.putInt(payload.length);
            b.put(payload);
            return b.array();
        }
    }

    private static Object decodePart(byte t, byte[] payload) {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        switch (t) {
            case T_INT32:
                return bb.getInt();
            case T_INT64:
                return bb.getLong();
            case T_UUID:
                long msb = bb.getLong();
                long lsb = bb.getLong();
                return new UUID(msb, lsb);
            case T_BYTES:
                return payload; // raw bytes
            case T_STRING:
                return new String(payload, StandardCharsets.UTF_8);
            default:
                throw new IllegalArgumentException("Unknown type " + t);
        }
    }

    // -------- Example / Dry run ----------
    public static void main(String[] args) {
        // composite PK: (userId:int, orderId:long, regionId:string, uuid)
        int userId = 42;
        long orderId = 1_234_567_890L;
        String region = "us-west";
        UUID tid = UUID.randomUUID();

        byte[] rowKey = RowKeyCodec.encode(userId, orderId, region, tid);
        System.out.println("rowKey length=" + rowKey.length);
        List<Object> decoded = RowKeyCodec.decode(rowKey);
        System.out.println("decoded = " + decoded);

        int numShards = 16;
        int shard = RowKeyCodec.shardForKey(rowKey, numShards);
        System.out.println("shard = " + shard);
    }
}
