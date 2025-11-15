package com.sharpflux.taxiapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** * Helper class to manage user preferences and session data */
public class UserPreferences {

    private static final String PREF_NAME = "MyAppPrefs";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public UserPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ============ SAVE METHODS ============

    /** * Save complete user session from OTP verification response  */
    public void saveOTPVerificationSession(String authToken, String expiresIn,
                                           int driverId, String phoneNumber) {
        editor.putString("authToken", authToken);
        editor.putString("expiresIn", expiresIn);
        editor.putInt("driverId", driverId);
        editor.putString("user_phone", phoneNumber);
        editor.putBoolean("isLoggedIn", true);
        editor.putString("loginMethod", "OTP");
        editor.apply();
    }

    /** * Save user details */
    public void saveUserDetails(int userId, String name, String email, String phoneNumber,
                                boolean isActive, int roleId, int locationId,
                                String roleName, String companyLogoURL, int statusId) {
        editor.putInt("user_id", userId);
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.putString("user_phone", phoneNumber);
        editor.putBoolean("isActive", isActive);
        editor.putInt("roleId", roleId);
        editor.putInt("locationId", locationId);
        editor.putString("roleName", roleName);
        editor.putString("companyLogoURL", companyLogoURL);
        editor.putInt("verificationStatus", statusId);
        editor.apply();
    }

    // ============ GET METHODS ============

    public String getAuthToken() {
        return prefs.getString("authToken", "");
    }

    public int getUserId() {
        return prefs.getInt("user_id", -1);
    }

    public String getUserName() {
        return prefs.getString("user_name", "");
    }

    public String getUserEmail() {
        return prefs.getString("user_email", "");
    }

    public String getUserPhone() {
        return prefs.getString("user_phone", "");
    }

    public int getDriverId() {
        return prefs.getInt("driverId", -1);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public boolean isActive() {
        return prefs.getBoolean("isActive", false);
    }

    public int getRoleId() {
        return prefs.getInt("roleId", -1);
    }

    public int getLocationId() {
        return prefs.getInt("locationId", -1);
    }

    public String getRoleName() {
        return prefs.getString("roleName", "");
    }

    public String getCompanyLogoURL() {
        return prefs.getString("companyLogoURL", "");
    }

    public String getExpiresIn() {
        return prefs.getString("expiresIn", "");
    }

    public String getLoginMethod() {
        return prefs.getString("loginMethod", "");
    }

    // ============ UTILITY METHODS ============

    /** * Check if auth token exists and is not empty */
    public boolean hasValidToken() {
        String token = getAuthToken();
        return token != null && !token.isEmpty();
    }

    /** * Clear all user session data (logout)  */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /** * Clear only auth token (for token refresh scenarios) */
    public void clearAuthToken() {
        editor.remove("authToken");
        editor.remove("expiresIn");
        editor.apply();
    }

    /** * Update auth token (for token refresh) */
    public void updateAuthToken(String newToken, String expiresIn) {
        editor.putString("authToken", newToken);
        editor.putString("expiresIn", expiresIn);
        editor.apply();
    }
}