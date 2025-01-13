package com.example.workshiftapp.classes;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class EmailValidator {

    // This is a basic email pattern. It can be made more complex depending on requirements.
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
