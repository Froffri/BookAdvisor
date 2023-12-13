package it.unipi.lsmsdb.bookadvisor.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingUtility {

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash the password", e);
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        // Generate the hashed version of the plain password
        String generatedHash = hashPassword(plainPassword);
        // Compare the generated hash with the stored hash
        return generatedHash != null && generatedHash.equals(hashedPassword);
    }
}
