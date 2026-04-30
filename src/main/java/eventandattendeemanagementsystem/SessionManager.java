package main.java.eventandattendeemanagementsystem;

/**
 * SessionManager – holds the currently logged-in user for the session.
 */
public class SessionManager {

    private static int    currentUserId   = -1;
    private static String currentUsername = null;
    private static String currentFullName = null;

    public static void login(int userId, String username, String fullName) {
        currentUserId   = userId;
        currentUsername = username;
        currentFullName = fullName;
    }

    public static void logout() {
        currentUserId   = -1;
        currentUsername = null;
        currentFullName = null;
    }

    public static boolean isLoggedIn()       { return currentUserId != -1; }
    public static int     getUserId()        { return currentUserId; }
    public static String  getUsername()      { return currentUsername; }
    public static String  getFullName()      { return currentFullName; }
}
