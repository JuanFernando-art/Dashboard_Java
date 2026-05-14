package com.meuprojeto.security;

import com.meuprojeto.config.AppConfig;
import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtil {
    private static final int BCRYPT_COST = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtil() {}

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static String hashCpf(String cpf) {
        try {
            String normalizedCpf = normalizeCpf(cpf);
            byte[] key = getKeyBytes("CPF_HASH_KEY");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] hash = mac.doFinal(normalizedCpf.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel gerar o hash do CPF.", e);
        }
    }

    public static String encryptCpf(String cpf) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(getKeyBytes("CPF_ENCRYPTION_KEY"), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] encrypted = cipher.doFinal(normalizeCpf(cpf).getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel criptografar o CPF.", e);
        }
    }

    public static String decryptCpf(String encryptedCpf) {
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedCpf);
            byte[] iv = new byte[GCM_IV_BYTES];
            byte[] encrypted = new byte[payload.length - GCM_IV_BYTES];

            System.arraycopy(payload, 0, iv, 0, iv.length);
            System.arraycopy(payload, iv.length, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getKeyBytes("CPF_ENCRYPTION_KEY"), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel descriptografar o CPF.", e);
        }
    }

    private static byte[] getKeyBytes(String envName) throws Exception {
        String key = AppConfig.envOrDevFallback(envName, "dev-local-" + envName + "-change-before-production");

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException e) {
            decoded = key.getBytes(StandardCharsets.UTF_8);
        }

        if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32) {
            return decoded;
        }

        return MessageDigest.getInstance("SHA-256").digest(decoded);
    }
}
