package com.example.passwordmanagerapp;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class EncryptionHelper {

    private static final String SECRET_KEY = "MySimpleKey123";

    public static String encrypt(String plainText) {
        try {
            byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] result = new byte[data.length];

            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] ^ key[i % key.length]);
            }

            return Base64.encodeToString(result, Base64.NO_WRAP);
        } catch (Exception e) {
            return plainText;
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            byte[] data = Base64.decode(encryptedText, Base64.NO_WRAP);
            byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] result = new byte[data.length];

            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] ^ key[i % key.length]);
            }

            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // fallback for old plain text records already saved before encryption
            return encryptedText;
        }
    }
}