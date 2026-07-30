package crypto;

import model.PasswordEntry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

public class CsvService {

    private static final String CSV_HEADER = "website,username,password,category,tags,favorite,dateAdded";

    /**
     * Exports the database to a CSV file (comma or semicolon separator)
     */
    public static void exportToCsv(File file, ArrayList<PasswordEntry> entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (PasswordEntry entry : entries) {
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                        escapeCsv(entry.getWebsite()),
                        escapeCsv(entry.getUsername()),
                        escapeCsv(entry.getPassword()),
                        escapeCsv(entry.getCategory()),
                        escapeCsv(entry.getTags()),
                        entry.isFavorite(),
                        escapeCsv(entry.getDateAdded())
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Imports entries from a CSV file
     */
    public static ArrayList<PasswordEntry> importFromCsv(File file) throws IOException {
        ArrayList<PasswordEntry> importedEntries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Ignorer l'en-tête CSV si présent
                if (isFirstLine && line.toLowerCase().contains("website")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = parseCsvLine(line);
                if (parts.length >= 3) {
                    String website = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    String category = parts.length > 3 && !parts[3].isEmpty() ? parts[3] : "Général";
                    String tags = parts.length > 4 ? parts[4] : "";
                    boolean favorite = parts.length > 5 && Boolean.parseBoolean(parts[5]);
                    String dateAdded = parts.length > 6 && !parts[6].isEmpty() ? parts[6] : LocalDate.now().toString();

                    importedEntries.add(new PasswordEntry(website, username, password, dateAdded, category, tags, favorite));
                }
            }
        }
        return importedEntries;
    }

    private static String escapeCsv(String data) {
        if (data == null) return "";
        return data.replace("\"", "\"\"");
    }

    private static String[] parseCsvLine(String line) {
        // Rudimentary clipping that manages quotes and commas/semicolons
        String delimiter = line.contains(";") ? ";" : ",";
        String[] rawTokens = line.split(delimiter + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < rawTokens.length; i++) {
            rawTokens[i] = rawTokens[i].trim().replaceAll("^\"|\"$", "").replace("\"\"", "\"");
        }
        return rawTokens;
    }
}