package crypto;

import java.security.SecureRandom;

//This class works as the backend and will generate the password

public class PasswordGenerator {

    // Character pools
    // these String will hold the characters/numbers/symbols that we are going to randomly pick to generate our password
    public static final String LOWERCASE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPERCASE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String NUMBERS = "0123456789";
    public static final String SPECIAL_SYMBOLS = "!@#$%^&*()-=+[]{};:,.<>/?";

    // SecureRandom is a cryptographically strong random number generator.
    // java.util.Random is predictable (it's a linear congruential generator) and must
    // never be used to generate secrets such as passwords.
    private final SecureRandom random;

    // Constructor
    public PasswordGenerator(){random = new SecureRandom();}

    // length - length of password to be generated (taken from the user)
    // includeUppercase and etc... considers if the password should include uppercase, lowercase, etc... (taken from the user)
    public String generatePassword(int length, boolean includeUppercase, boolean includeLowercase, boolean includeNumbers, boolean includeSpecialSymbols){
        // We will use String Builder over string for better efficiency
        StringBuilder passwordBuilder = new StringBuilder();

        // store valid characters (toggle states)
        String validCharacters = "";
        if(includeUppercase) validCharacters += UPPERCASE_CHARACTERS;
        if(includeLowercase) validCharacters += LOWERCASE_CHARACTERS;
        if(includeNumbers) validCharacters += NUMBERS;
        if(includeSpecialSymbols) validCharacters += SPECIAL_SYMBOLS;

        // build password
        for(int i = 0; i < length; i++){
            // generate a random index
            int randomIndex = random.nextInt(validCharacters.length());

            // get the char based on the random index
            char randomChar = validCharacters.charAt(randomIndex);

            // Store char into password builder
            passwordBuilder.append(randomChar);

            // do this until we have reached the length that the user has provided to us

        }

        // Return the result
        return passwordBuilder.toString();
    }
}