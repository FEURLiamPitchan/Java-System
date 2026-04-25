package com.mycompany.javasystem;

public class UserSession {
    private static String currentUserEmail;
    private static String currentUserRole;
    private static String currentUserName;
    
    public static void setCurrentUser(String email, String role, String name) {
        currentUserEmail = email;
        currentUserRole = role;
        currentUserName = name;
    }
    
    public static String getCurrentUserEmail() {
        return currentUserEmail != null ? currentUserEmail : "resident@email.com";
    }
    
    public static String getCurrentUserRole() {
        return currentUserRole != null ? currentUserRole : "resident";
    }
    
    public static String getCurrentUserName() {
        return currentUserName != null ? currentUserName : "Resident User";
    }
    
    public static void clearSession() {
        currentUserEmail = null;
        currentUserRole = null;
        currentUserName = null;
    }
    
    public static boolean isLoggedIn() {
        return currentUserEmail != null;
    }
}