package com.sharpflux.taxiapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.network.APIs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";

    // Views
    private ImageButton btnBack;
    private TextInputEditText etFirstName, etMiddleName, etLastName;
    private TextInputEditText etEmail, etPhoneNumber, etAddress;
    private TextInputEditText etPassword, etConfirmPassword;
    private AutoCompleteTextView actvState, actvCity, actvLocation;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;

    // Input Layouts for error handling
    private TextInputLayout tilFirstName, tilLastName, tilEmail, tilPhoneNumber;
    private TextInputLayout tilAddress, tilPassword, tilConfirmPassword;
    private TextInputLayout tilState, tilCity, tilLocation;

    // Data
    private List<StateData> stateList = new ArrayList<>();
    private List<CityData> cityList = new ArrayList<>();
    private List<LocationData> locationList = new ArrayList<>();

    private int selectedStateId = 0;
    private int selectedCityId = 0;
    private int selectedLocationId = 0;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initViews();
        setupClickListeners();
        initRequestQueue();
        loadStates();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        // Personal info
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);

        // Contact info
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);

        // Address info
        etAddress = findViewById(R.id.etAddress);
        actvState = findViewById(R.id.actvState);
        actvCity = findViewById(R.id.actvCity);
        actvLocation = findViewById(R.id.actvLocation);

        // Security
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Other elements
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        progressBar = findViewById(R.id.progressBar);

        // Input Layouts
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilAddress = findViewById(R.id.tilAddress);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilState = findViewById(R.id.tilState);
        tilCity = findViewById(R.id.tilCity);
        tilLocation = findViewById(R.id.tilLocation);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                registerCustomer();
            }
        });

        tvLoginLink.setOnClickListener(v -> {
            // Navigate to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // State selection listener
        actvState.setOnItemClickListener((parent, view, position, id) -> {
            StateData selectedState = stateList.get(position);
            selectedStateId = selectedState.getId();
            actvState.setText(selectedState.getName());

            // Reset city and location
            actvCity.setText("");
            actvLocation.setText("");
            selectedCityId = 0;
            selectedLocationId = 0;

            // Enable city dropdown and load cities
            actvCity.setEnabled(true);
            loadCities(selectedStateId);
        });

        // City selection listener
        actvCity.setOnItemClickListener((parent, view, position, id) -> {
            CityData selectedCity = cityList.get(position);
            selectedCityId = selectedCity.getId();
            actvCity.setText(selectedCity.getName());

            // Reset location
            actvLocation.setText("");
            selectedLocationId = 0;

            // Enable location dropdown and load locations
            actvLocation.setEnabled(true);
            loadLocations(selectedCityId);
        });

        // Location selection listener
        actvLocation.setOnItemClickListener((parent, view, position, id) -> {
            LocationData selectedLocation = locationList.get(position);
            selectedLocationId = selectedLocation.getId();
            actvLocation.setText(selectedLocation.getName());
        });
    }

    private void initRequestQueue() {
        requestQueue = Volley.newRequestQueue(this);
    }

    private void loadStates() {
        String url = APIs.Main_URL + "States"; // Adjust endpoint as per your API

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            stateList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject stateObj = response.getJSONObject(i);
                                int id = stateObj.getInt("id");
                                String name = stateObj.getString("name");
                                stateList.add(new StateData(id, name));
                            }

                            ArrayAdapter<StateData> adapter = new ArrayAdapter<>(
                                    RegistrationActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, stateList);
                            actvState.setAdapter(adapter);

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing states", e);
                            loadDummyStates(); // Fallback to dummy data
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error loading states", error);
                        loadDummyStates(); // Fallback to dummy data
                    }
                }
        );

        requestQueue.add(request);
    }

    private void loadDummyStates() {
        // Fallback dummy data
        stateList.clear();
        stateList.add(new StateData(1, "Maharashtra"));
        stateList.add(new StateData(2, "Karnataka"));
        stateList.add(new StateData(3, "Tamil Nadu"));
        stateList.add(new StateData(4, "Gujarat"));

        ArrayAdapter<StateData> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, stateList);
        actvState.setAdapter(adapter);
    }

    private void loadCities(int stateId) {
        String url = APIs.Main_URL + "Cities/" + stateId; // Adjust endpoint as per your API

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            cityList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject cityObj = response.getJSONObject(i);
                                int id = cityObj.getInt("id");
                                String name = cityObj.getString("name");
                                cityList.add(new CityData(id, name, stateId));
                            }

                            ArrayAdapter<CityData> adapter = new ArrayAdapter<>(
                                    RegistrationActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, cityList);
                            actvCity.setAdapter(adapter);

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing cities", e);
                            loadDummyCities(stateId);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error loading cities", error);
                        loadDummyCities(stateId);
                    }
                }
        );

        requestQueue.add(request);
    }

    private void loadDummyCities(int stateId) {
        // Fallback dummy data
        cityList.clear();

        switch (stateId) {
            case 1: // Maharashtra
                cityList.add(new CityData(1, "Mumbai", stateId));
                cityList.add(new CityData(2, "Pune", stateId));
                cityList.add(new CityData(3, "Nashik", stateId));
                break;
            case 2: // Karnataka
                cityList.add(new CityData(4, "Bangalore", stateId));
                cityList.add(new CityData(5, "Mysore", stateId));
                cityList.add(new CityData(6, "Mangalore", stateId));
                break;
            case 3: // Tamil Nadu
                cityList.add(new CityData(7, "Chennai", stateId));
                cityList.add(new CityData(8, "Coimbatore", stateId));
                cityList.add(new CityData(9, "Madurai", stateId));
                break;
            case 4: // Gujarat
                cityList.add(new CityData(10, "Ahmedabad", stateId));
                cityList.add(new CityData(11, "Surat", stateId));
                cityList.add(new CityData(12, "Vadodara", stateId));
                break;
            default:
                cityList.add(new CityData(100, "Default City", stateId));
                break;
        }

        ArrayAdapter<CityData> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cityList);
        actvCity.setAdapter(adapter);
    }

    private void loadLocations(int cityId) {
        String url = APIs.Main_URL + "Locations/" + cityId; // Adjust endpoint as per your API

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            locationList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject locationObj = response.getJSONObject(i);
                                int id = locationObj.getInt("id");
                                String name = locationObj.getString("name");
                                locationList.add(new LocationData(id, name, cityId));
                            }

                            ArrayAdapter<LocationData> adapter = new ArrayAdapter<>(
                                    RegistrationActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, locationList);
                            actvLocation.setAdapter(adapter);

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing locations", e);
                            loadDummyLocations(cityId);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error loading locations", error);
                        loadDummyLocations(cityId);
                    }
                }
        );

        requestQueue.add(request);
    }

    private void loadDummyLocations(int cityId) {
        // Fallback dummy data
        locationList.clear();

        // Sample locations for different cities
        switch (cityId) {
            case 1: // Mumbai
                locationList.add(new LocationData(1, "Andheri", cityId));
                locationList.add(new LocationData(2, "Bandra", cityId));
                locationList.add(new LocationData(3, "Powai", cityId));
                break;
            case 2: // Pune
                locationList.add(new LocationData(4, "Hinjewadi", cityId));
                locationList.add(new LocationData(5, "Koregaon Park", cityId));
                locationList.add(new LocationData(6, "Wakad", cityId));
                break;
            default:
                locationList.add(new LocationData(100, "Central Area", cityId));
                locationList.add(new LocationData(101, "North Zone", cityId));
                locationList.add(new LocationData(102, "South Zone", cityId));
                break;
        }

        ArrayAdapter<LocationData> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, locationList);
        actvLocation.setAdapter(adapter);
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Clear previous errors
        clearErrors();

        // First Name validation
        if (TextUtils.isEmpty(etFirstName.getText())) {
            tilFirstName.setError("First name is required");
            isValid = false;
        }

        // Last Name validation
        if (TextUtils.isEmpty(etLastName.getText())) {
            tilLastName.setError("Last name is required");
            isValid = false;
        }

        // Email validation
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        // Phone validation
        String phone = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            tilPhoneNumber.setError("Phone number is required");
            isValid = false;
        } else if (phone.length() < 10) {
            tilPhoneNumber.setError("Please enter a valid phone number");
            isValid = false;
        }

        // Address validation
        if (TextUtils.isEmpty(etAddress.getText())) {
            tilAddress.setError("Address is required");
            isValid = false;
        }

        // State validation
        if (selectedStateId == 0) {
            tilState.setError("Please select a state");
            isValid = false;
        }

        // City validation
        if (selectedCityId == 0) {
            tilCity.setError("Please select a city");
            isValid = false;
        }

        // Location validation
        if (selectedLocationId == 0) {
            tilLocation.setError("Please select a location");
            isValid = false;
        }

        // Password validation
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        // Confirm password validation
        String confirmPassword = etConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        // Terms and conditions validation
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept Terms and Conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilEmail.setError(null);
        tilPhoneNumber.setError(null);
        tilAddress.setError(null);
        tilState.setError(null);
        tilCity.setError(null);
        tilLocation.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void registerCustomer() {
        showLoading(true);

        String url = APIs.Main_URL + "Customers/register"; // Adjust endpoint as per your API

        try {
            // Create JSON request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("customersId", 0);
            requestBody.put("locationId", selectedLocationId);
            requestBody.put("firstName", etFirstName.getText().toString().trim());
            requestBody.put("middleName", etMiddleName.getText().toString().trim());
            requestBody.put("lastName", etLastName.getText().toString().trim());
            requestBody.put("emailId", etEmail.getText().toString().trim());
            requestBody.put("phoneNumber", etPhoneNumber.getText().toString().trim());
            requestBody.put("passwordHash", hashPassword(etPassword.getText().toString()));
            requestBody.put("address", etAddress.getText().toString().trim());
            requestBody.put("cityId", selectedCityId);
            requestBody.put("stateId", selectedStateId);
            requestBody.put("roleId", 2); // Assuming 2 is customer role
            requestBody.put("isActive", true);
            requestBody.put("userId", 0);

            Log.d(TAG, "Registration request: " + requestBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, url, requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            showLoading(false);
                            handleRegistrationResponse(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showLoading(false);
                            handleRegistrationError(error);
                        }
                    }
            );

            requestQueue.add(request);

        } catch (JSONException e) {
            showLoading(false);
            Log.e(TAG, "Error creating request body", e);
            Toast.makeText(this, "Error preparing registration data", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegistrationResponse(JSONObject response) {
        try {
            boolean success = response.optBoolean("success", false);
            String message = response.optString("message", "Registration completed");

            if (success) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show();

                // Navigate to login activity
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("email", etEmail.getText().toString().trim());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing registration response", e);
            Toast.makeText(this, "Registration completed but response unclear", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegistrationError(VolleyError error) {
        Log.e(TAG, "Registration error", error);

        String message = "Registration failed. Please try again.";

        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 400:
                    message = "Invalid data provided. Please check your inputs.";
                    break;
                case 409:
                    message = "An account with this email already exists.";
                    break;
                case 500:
                    message = "Server error. Please try again later.";
                    break;
            }
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return password; // Fallback, but not recommended for production
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);

        // Disable form during loading
        setFormEnabled(!show);
    }

    private void setFormEnabled(boolean enabled) {
        etFirstName.setEnabled(enabled);
        etMiddleName.setEnabled(enabled);
        etLastName.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etPhoneNumber.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        actvState.setEnabled(enabled);
        actvCity.setEnabled(enabled && selectedStateId != 0);
        actvLocation.setEnabled(enabled && selectedCityId != 0);
        etPassword.setEnabled(enabled);
        etConfirmPassword.setEnabled(enabled);
        cbTerms.setEnabled(enabled);
    }

    // Data classes for dropdowns
    private static class StateData {
        private int id;
        private String name;

        public StateData(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() { return name; }
    }

    private static class CityData {
        private int id;
        private String name;
        private int stateId;

        public CityData(int id, String name, int stateId) {
            this.id = id;
            this.name = name;
            this.stateId = stateId;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getStateId() { return stateId; }

        @Override
        public String toString() { return name; }
    }

    private static class LocationData {
        private int id;
        private String name;
        private int cityId;

        public LocationData(int id, String name, int cityId) {
            this.id = id;
            this.name = name;
            this.cityId = cityId;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getCityId() { return cityId; }

        @Override
        public String toString() { return name; }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}