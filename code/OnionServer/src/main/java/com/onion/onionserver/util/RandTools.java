package com.onion.onionserver.util;

public class RandTools {
    public static String randString(int length) {
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return randString(length, charSet);
    }

    public static String randString(int length, String charSet) {
        StringBuilder sb = new StringBuilder();
        int charSetLength = charSet.length();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * charSetLength);
            sb.append(charSet.charAt(randomIndex));
        }
        return sb.toString();
    }
}
