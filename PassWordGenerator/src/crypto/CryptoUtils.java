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
    // Un sel fixe pour simplifier le stockage dans un premier temps (à générer dynamiquement par la suite)
    private static final byte[] SALT = new byte[]{ 0x1, 0x5, 0x3, 0x7, 0x2, 0x6, 0x4, 0x8, 0x9, 0x0, 0x2, 0x4, 0x6, 0x8, 0x1, 0x3 };
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // 1. Dériver une clé AES-256 à partir du mot de passe maître
    private static SecretKey deriveKey(String masterPassword) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), SALT, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    // 2. Chiffrer une chaîne de caractères
    public static String encrypt(String data, String masterPassword) throws Exception {
        SecretKey secretKey = deriveKey(masterPassword);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv); // Génère un vecteur d'initialisation unique

        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));

        // On colle l'IV au début des données chiffrées pour pouvoir le réutiliser au déchiffrement
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // 3. Déchiffrer une chaîne de caractères
    public static String decrypt(String encryptedBase64, String masterPassword) throws Exception {
        SecretKey secretKey = deriveKey(masterPassword);
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);

        // Extraire l'IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, iv.length);

        // Extraire les données chiffrées
        int encryptedDataLength = combined.length - GCM_IV_LENGTH;
        byte[] encryptedData = new byte[encryptedDataLength];
        System.arraycopy(combined, GCM_IV_LENGTH, encryptedData, 0, encryptedDataLength);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, "UTF-8");
    }
}