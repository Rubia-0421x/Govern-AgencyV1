package com.govagency.util;

import java.util.regex.Pattern;

public class Validator {

    private static final Pattern CITIZEN_NAME = Pattern.compile("^[A-Za-z. ]+$");
    private static final Pattern CITIZEN_NUMBER = Pattern.compile("(\\+\\d{1,3}[- ]?)?\\d{10}");
    private static final Pattern CITIZEN_ID_PATTERN = Pattern.compile("\\d+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");

    public static boolean isValidCitizenId(String id) {
        return id != null && CITIZEN_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidCitizenName(String name) {
        return name != null && CITIZEN_NAME.matcher(name).matches();
    }

    public static boolean isValidCitizenNumber(String number) {
        return number != null && CITIZEN_NUMBER.matcher(number).matches();
    }
}