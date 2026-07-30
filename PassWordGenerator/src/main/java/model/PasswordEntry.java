package model;

import javafx.scene.image.Image;

public class PasswordEntry {
    private String website;
    private String username;
    private String password;
    private String dateAdded;
    private String category;
    private String tags;
    private boolean favorite;

    // Constructor
    public PasswordEntry(String website, String username, String password) {
        this(website, username, password, java.time.LocalDate.now().toString(), "Général", "", false);
    }

    public PasswordEntry(String website, String username, String password, String dateAdded) {
        this(website, username, password, dateAdded, "Général", "", false);
    }

    public PasswordEntry(String website, String username, String password, String dateAdded,
                         String category, String tags, boolean favorite) {
        this.website = website;
        this.username = username;
        this.password = password;
        this.dateAdded = dateAdded;
        this.category = (category == null || category.trim().isEmpty()) ? "Général" : category;
        this.tags = tags != null ? tags : "";
        this.favorite = favorite;
    }

    public String getWebsite() { return website; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDateAdded() { return dateAdded; }

    public void setWebsite(String website) {
        this.website = website;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setCategory(String category){this.category = category;}

    /**
     * Tente de récupérer le favicon du site (chargement asynchrone en arrière-plan,
     * ce qui évite de bloquer le thread JavaFX contrairement à l'ancienne version Swing).
     * Retourne toujours une Image (jamais null) ; si le domaine est vide, l'Image
     * ne se chargera simplement pas (isError() sera true côté vue si besoin).
     */
    public Image fetchFaviconImage() {
        if (website == null || website.trim().isEmpty()) {
            return null;
        }

        try {
            String cleanDomain = website.trim()
                    .toLowerCase()
                    .replaceAll("https?://", "")
                    .replaceAll("www\\.", "")
                    .replaceAll("/.*", "");

            if (cleanDomain.isEmpty()) return null;

            // Service de favicons Google (taille 16x16 px), chargement en arrière-plan
            String iconUrlStr = "https://www.google.com/s2/favicons?domain=" + cleanDomain + "&sz=16";
            return new Image(iconUrlStr, 16, 16, true, true, true);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }
}
