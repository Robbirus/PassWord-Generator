package crypto;

import java.io.File;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Scanner;

public class RecoveryKeyManager {

    private static final String ALPHANUM = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // Without confused characters (0, O, 1, I)

    /**
     * Generates a random backup key of the type: VK-XXXX-XXXX-XXXX-XXXX
     */
    public static String generateKey() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("VK-");

        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append("-");
            }
            int index = random.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Writes the text file .txt that the user will keep in a safe place
     */
    public static boolean saveKeyToFile(File destinationFile, String login, String recoveryKey) {
        try (PrintWriter writer = new PrintWriter(destinationFile)) {
            writer.println("====================================================");
            writer.println("   VAULTJAR - CLE DE SECOURS / RECOVERY KEY");
            writer.println("====================================================");
            writer.println("Identifiant : " + login);
            writer.println("Clé de Secours : " + recoveryKey);
            writer.println();
            writer.println("IMPORTANT : Conservez ce fichier ou réimprimez-le.");
            writer.println("En cas d'oubli du mot de passe maître, cette clé est");
            writer.println("le SEUL moyen de récupérer l'accès à vos mots de passe.");
            writer.println("====================================================");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads a text file .txt imported to extract the VK-XXXX key...
     */
    public static String readKeyFromFile(File sourceFile) {
        try (Scanner scanner = new Scanner(sourceFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("Clé de Secours :")) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}