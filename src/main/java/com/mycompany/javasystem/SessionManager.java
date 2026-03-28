package com.mycompany.javasystem;

public class SessionManager {
    private static String currentUserEmail = null;
    private static String currentUserRole  = null;
    private static String currentUserName  = null;

    public static void login(String email, String role, String name) {
        currentUserEmail = email;
        currentUserRole  = role;
        currentUserName  = name;
    }

    public static String getEmail() { return currentUserEmail; }
    public static String getRole()  { return currentUserRole; }
    public static String getName()  { return currentUserName != null 
                                        ? currentUserName : "Admin"; }

    public static void logout() {
        currentUserEmail = null;
        currentUserRole  = null;
        currentUserName  = null;
    }
}