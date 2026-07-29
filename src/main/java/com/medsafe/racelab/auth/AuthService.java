package com.medsafe.racelab.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

public final class AuthService {
    public static final String DEMO_USERNAME = "student";
    public static final String DEMO_PASSWORD = "MedSafe2026!";

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final byte[] SALT = Base64.getDecoder()
            .decode("TWVkU2FmZVJhY2VMYWIyMDI2");
    private static final byte[] EXPECTED_HASH = Base64.getDecoder()
            .decode("2Pvy25vBTqP6rp4kskkE3DTuzfpcwaYB5e6DjYDZGZw=");

    public boolean authenticate(String username, char[] password) {
        if (!DEMO_USERNAME.equals(username == null ? "" : username.trim())) {
            clear(password);
            return false;
        }

        try {
            byte[] candidateHash = derive(password);
            return MessageDigest.isEqual(EXPECTED_HASH, candidateHash);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password verification is unavailable", exception);
        } finally {
            clear(password);
        }
    }

    private byte[] derive(char[] password) throws GeneralSecurityException {
        PBEKeySpec specification = new PBEKeySpec(password, SALT, ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification)
                    .getEncoded();
        } finally {
            specification.clearPassword();
        }
    }

    private void clear(char[] password) {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
        }
    }
}
