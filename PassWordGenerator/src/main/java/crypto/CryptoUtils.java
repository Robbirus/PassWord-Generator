package crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CryptoUtils {

    private static final int KEY_LENGTH = 256;
    private static final int ITERATIONS = 65536;
    private static final int SALT_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // Old fixed salt, kept ONLY to be able to decipher the chests
    // created before this patch. Never use it to encrypt again.
    @Deprecated
    private static final byte[] LEGACY_FIXED_SALT =
            new byte[]{ 0x1, 0x5, 0x3, 0x7, 0x2, 0x6, 0x4, 0x8, 0x9, 0x0, 0x2, 0x4, 0x6, 0x8, 0x1, 0x3 };

    // 1. Derive an AES-256 key from the master password AND a given salt
    private static SecretKey deriveKey(String masterPassword, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    // 2. Encrypt a character string
    public static String encrypt(String data, String masterPassword) throws Exception {
        // Random salt, unique to EVERY encryption (is not secret, but must be unpredictable)
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        SecretKey secretKey = deriveKey(masterPassword, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv); // Generates a single initialization vector

        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));

        // Format de sortie : SALT || IV || CIPHERTEXT, encodé en Base64
        byte[] combined = new byte[salt.length + iv.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // 3. Decrypt a character string
    public static String decrypt(String encryptedBase64, String masterPassword) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);

        if (combined.length >= SALT_LENGTH + GCM_IV_LENGTH) {
            try {
                byte[] salt = new byte[SALT_LENGTH];
                System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

                byte[] iv = new byte[GCM_IV_LENGTH];
                System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);

                int encryptedDataLength = combined.length - SALT_LENGTH - GCM_IV_LENGTH;
                byte[] encryptedData = new byte[encryptedDataLength];
                System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, encryptedData, 0, encryptedDataLength);

                SecretKey secretKey = deriveKey(masterPassword, salt);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

                byte[] decryptedData = cipher.doFinal(encryptedData);
                return new String(decryptedData, "UTF-8");
            } catch (Exception ignoredNewFormatFailure) {
                // We try again with the old format below (pre-existing chest)
            }
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

        int encryptedDataLength = combined.length - GCM_IV_LENGTH;
        byte[] encryptedData = new byte[encryptedDataLength];
        System.arraycopy(combined, GCM_IV_LENGTH, encryptedData, 0, encryptedDataLength);

        SecretKey secretKey = deriveKey(masterPassword, LEGACY_FIXED_SALT);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, "UTF-8");
    }
}