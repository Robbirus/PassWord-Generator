package model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PasswordEntry {
    private String website;
    private String username;
    private String password;
    private String dateAdded;

    // Constructor
    public PasswordEntry(String website, String username, String password, String dateAdded) {
        this.website = website;
        this.username = username;
        this.password = password;
        this.dateAdded = dateAdded;
    }

    // Handy builder (automatically generates today’s date)
    public PasswordEntry(String website, String username, String password) {
        this(website, username, password, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
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
}