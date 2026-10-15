package com.scraply.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Custom CookieSerializer that encrypts session IDs before writing to the cookie
 * and decrypts them when reading from incoming requests before Spring Session validates them.
 */
public class EncryptedCookieSerializer implements CookieSerializer {

    private static final Logger log = LoggerFactory.getLogger(EncryptedCookieSerializer.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96-bit IV for AES-GCM
    private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag

    private final DefaultCookieSerializer delegate;
    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public EncryptedCookieSerializer(DefaultCookieSerializer delegate, String secretKeyString) {
        this.delegate = delegate;
        this.secretKey = deriveKey(secretKeyString);
        this.secureRandom = new SecureRandom();
    }

    @Override
    public void writeCookieValue(CookieValue cookieValue) {
        String rawValue = cookieValue.getCookieValue();

        // If the cookie is being cleared/invalidated, rawValue is empty or null
        if (rawValue == null || rawValue.isEmpty()) {
            delegate.writeCookieValue(cookieValue);
            return;
        }

        try {
            String encryptedValue = encrypt(rawValue);
            CookieValue encryptedCookieValue = new CookieValue(
                    cookieValue.getRequest(),
                    cookieValue.getResponse(),
                    encryptedValue
            );
            delegate.writeCookieValue(encryptedCookieValue);
        } catch (Exception ex) {
            log.error("Failed to encrypt session cookie", ex);
            throw new IllegalStateException("Could not encrypt session cookie", ex);
        }
    }

    @Override
    public List<String> readCookieValues(HttpServletRequest request) {
        List<String> encryptedValues = delegate.readCookieValues(request);
        List<String> decryptedValues = new ArrayList<>();

        for (String encryptedVal : encryptedValues) {
            if (encryptedVal == null || encryptedVal.isBlank()) {
                continue;
            }
            try {
                String decrypted = decrypt(encryptedVal);
                if (decrypted != null && !decrypted.isBlank()) {
                    decryptedValues.add(decrypted);
                }
            } catch (Exception ex) {
                log.debug("Failed to decrypt session cookie (tampered or expired): {}", ex.getMessage());
            }
        }
        return decryptedValues;
    }

    private String encrypt(String plainText) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
    }

    private String decrypt(String base64Encrypted) throws Exception {
        byte[] decoded;
        try {
            decoded = Base64.getUrlDecoder().decode(base64Encrypted);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (decoded.length < GCM_IV_LENGTH + (GCM_TAG_LENGTH / 8)) {
            return null;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private SecretKey deriveKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive AES key for session cookie encryption", e);
        }
    }
}
