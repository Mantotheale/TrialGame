package com.game.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Stream;

public final class HashingUtils {
    private HashingUtils() { }

    public static String hash(Stream<byte[]> data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        data.forEach(digest::update);
        return HexFormat.of().formatHex(digest.digest());
    }
}
