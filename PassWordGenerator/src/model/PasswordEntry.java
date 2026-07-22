package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import java.net.URL;


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
     * Attempts to download and return the site’s Favicon.
     * If there is an error or no network, returns null.
     */
    public ImageIcon FetchFavicon() {
        if (website == null || website.trim().isEmpty()) {
            return null;
        }

        try {
            // Clean the URL to keep domain name
            String cleanDomain = website.trim()
                    .toLowerCase()
                    .replaceAll("https?://", "")
                    .replaceAll("www\\.", "")
                    .replaceAll("/.*", "");

            if (cleanDomain.isEmpty()) return null;

            // Using the Google Favicon service (size 16x16 px)
            String iconUrlStr = "https://www.google.com/s2/favicons?domain=" + cleanDomain + "&sz=16";
            URL iconUrl = new URL(iconUrlStr);

            return new ImageIcon(iconUrl);
        } catch (Exception e) {
            // No connection or invalid URL -> returns null
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