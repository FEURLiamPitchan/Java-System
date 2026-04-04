package com.mycompany.javasystem;

public class UserSession {
    private static String currentUserEmail = null;
    private static String currentUserRole = null;

    public static void setCurrentUser(String email, String role) {
        currentUserEmail = email;
        currentUserRole = role;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail != null ? currentUserEmail : "guest@barangay.com";
    }

    public static String getCurrentUserRole() {
        return currentUserRole != null ? currentUserRole : "guest";
    }

    public static void clearSession() {
        currentUserEmail = null;
        currentUserRole = null;
    }
}
