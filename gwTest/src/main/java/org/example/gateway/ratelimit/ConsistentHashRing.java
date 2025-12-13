package org.example.gateway.ratelimit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.List;

public class ConsistentHashRing {

    private final NavigableMap<Integer, LimiterNode> ring = new TreeMap<>();

    public ConsistentHashRing(List<LimiterNode> nodes) {
        // для простоты — без "виртуальных нод"
        for (LimiterNode node : nodes) {
            int h = hash(node.id() + ":" + node.host() + ":" + node.port());
            ring.put(h, node);
        }
        if (ring.isEmpty()) {
            throw new IllegalArgumentException("Ring must have at least one node");
        }
    }

    public LimiterNode getNodeForKey(String key) {
        int h = hash(key);
        // ищем первый ключ в ring >= h
        var entry = ring.ceilingEntry(h);
        if (entry == null) {
            // если не нашли — оборачиваемся по кольцу, берём первый
            return ring.firstEntry().getValue();
        }
        return entry.getValue();
    }

    private int hash(String s) {
        // можно проще: s.hashCode(), но sha-1 даёт более стабильное распределение
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] bytes = sha1.digest(s.getBytes(StandardCharsets.UTF_8));
            // берём первые 4 байта как int
            return ((bytes[0] & 0xff) << 24)
                    | ((bytes[1] & 0xff) << 16)
                    | ((bytes[2] & 0xff) << 8)
                    | (bytes[3] & 0xff);
        } catch (NoSuchAlgorithmException e) {
            // fallback
            return s.hashCode();
        }
    }
}
