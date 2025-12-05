package com.sharpflux.taxiapp.ui.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.model.Customer;
import com.sharpflux.taxiapp.data.model.Driver;
import com.sharpflux.taxiapp.data.model.DropdownItem;
import com.sharpflux.taxiapp.data.model.Registration;
import com.sharpflux.taxiapp.data.repository.CustomerRepository;
import com.sharpflux.taxiapp.data.repository.RegistrationRepository;
import com.sharpflux.taxiapp.data.repository.DriverRepository;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";

    private EditText etFirstName, etMiddleName, etLastName, etEmail, etMobileNumber, etAddress;
    private AutoCompleteTextView actvCity, actvState;
    private ImageButton btnBackArrow;
    private Button btnEdit, btnSave;

    private List<DropdownItem> stateList = new ArrayList<>();
    private List<DropdownItem> cityList = new ArrayList<>();
    private ArrayAdapter<DropdownItem> stateAdapter;
    private ArrayAdapter<DropdownItem> cityAdapter;
    private boolean isEditing = false;
    private DriverRepository driverRepository;
    private RegistrationRepository registrationRepository;
    private Driver currentDriver;
    private int driverId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        //notification bar
        Window window = getWindow();
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            window.getDecorView().setSystemUiVisibility(0); // remove light icons flag
        } else {
            // Light mode
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        //layout
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                int topInset = insets.getInsets(WindowInsets.Type.statusBars()).top;
                v.setPadding(0, topInset, 0, 0);
                return insets;
            });
        } else {
            getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                int topInset = insets.getSystemWindowInsetTop();
                v.setPadding(0, topInset, 0, 0);
                return insets.consumeSystemWindowInsets();
            });
        }

        initializeViews();
        initializeRepositories();
        setupListeners();
        loadDropdownData();
        loadDriverData();

//        // Get Driver ID from intent
//        if (getIntent().hasExtra("DRIVER_ID")) {
//            driverId = getIntent().getIntExtra("DRIVER_ID", 0);
//        }
//
//        // Load driver data
//        if (driverId > 0) {
//            loadDriverData();
//        } else {
//
//            loadDriverData();
//        }
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etAddress = findViewById(R.id.etAddress);
        //etPincode = findViewById(R.id.etPincode);
        actvCity = findViewById(R.id.actvCity);
        actvState = findViewById(R.id.actvState);

        btnBackArrow = findViewById(R.id.btnBackArrow);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);

        // Add ProgressBar to your layout if not already present
        // progressBar = findViewById(R.id.progressBar);
    }

    private void initializeRepositories() {
        driverRepository = new DriverRepository(this);
        registrationRepository = new RegistrationRepository(this);
    }

    private void setupListeners() {
        btnBackArrow.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> enableEditing());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadDriverData() {
        showLoading(true);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int driversId = prefs.getInt("user_id", 0);
        driverRepository.getDrivers(driversId,
                new DriverRepository.DriverCallback() {
                    @Override
                    public void onSuccess(List<Driver> drivers) {
                        showLoading(false);
                        if (drivers != null && !drivers.isEmpty()) {
                            currentDriver = drivers.get(0);
                            bindDriverData(currentDriver);
                        } else {
                            Toast.makeText(RegistrationActivity.this,
                                    "No driver data found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(RegistrationActivity.this,
                                "Error loading data: " + error, Toast.LENGTH_LONG).show();
                    }
                });

    }
    private void bindDriverData(Driver driver) {
        if (driver == null) return;

        etFirstName.setText(driver.getFirstName() != null ? driver.getFirstName() : "");
        etMiddleName.setText(driver.getMiddleName() != null ? driver.getMiddleName() : "");
        etLastName.setText(driver.getLastName() != null ? driver.getLastName() : "");
        etEmail.setText(driver.getEmailId() != null ? driver.getEmailId() : "");
        etMobileNumber.setText(driver.getPhoneNumber() != null ? driver.getPhoneNumber() : "");
        etAddress.setText(driver.getAddress() != null ? driver.getAddress() : "");

        // Set state and city after dropdowns are loaded
        if (driver.getStateId() > 0) {
            for (DropdownItem item : stateList) {
                if (item.getId() == driver.getStateId()) {
                    actvState.setText(item.getText(), false);
                    loadCities(driver.getStateId());
                    break;
                }
            }
        }

        if (driver.getCityId() > 0) {
            for (DropdownItem item : cityList) {
                if (item.getId() == driver.getCityId()) {
                    actvCity.setText(item.getText(), false);
                    break;
                }
            }
        }
    }

    private void setupCityStateAdapters() {
        stateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, stateList);
        actvState.setAdapter(stateAdapter);

        cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cityList);
        actvCity.setAdapter(cityAdapter);
    }

    private void enableEditing() {
        isEditing = true;
        setFieldsEditable(true);
        btnEdit.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void saveProfile() {

        if (!validateFields()) {
            return;
        }

        showLoading(true);

        // ✅ Get driverId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int driverId = prefs.getInt("user_id", 0);

        if (driverId == 0) {
            showLoading(false);
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Registration object
        Driver registration = new Driver();
        registration.setDriverId(driverId);
        registration.setFirstName(etFirstName.getText().toString().trim());
        registration.setMiddleName(etMiddleName.getText().toString().trim());
        registration.setLastName(etLastName.getText().toString().trim());
        registration.setEmailId(etEmail.getText().toString().trim());
        registration.setPhoneNumber(etMobileNumber.getText().toString().trim());
        registration.setAddress(etAddress.getText().toString().trim());

        // Get State ID
        int selectedStateId = 0;
        String selectedStateText = actvState.getText().toString();
        for (DropdownItem item : stateList) {
            if (item.getText().equals(selectedStateText)) {
                selectedStateId = item.getId();
                break;
            }
        }

        // Get City ID
        int selectedCityId = 0;
        String selectedCityText = actvCity.getText().toString();
        for (DropdownItem item : cityList) {
            if (item.getText().equals(selectedCityText)) {
                selectedCityId = item.getId();
                break;
            }
        }

        registration.setStateId(selectedStateId);
        registration.setCityId(selectedCityId);
        registration.setRoleId(currentDriver != null ? currentDriver.getRoleId() : 1);
        registration.setIsActive(true);

        //  Call UPDATE API
        driverRepository.registerDriver(registration,
                new DriverRepository.RegistrationCallback() {

                    @Override
                    public void onResult(boolean success, String message,int driverId) {
                        showLoading(false);

                        if (success) {
                            isEditing = false;
                            setFieldsEditable(false);
                            btnSave.setVisibility(View.GONE);
                            btnEdit.setVisibility(View.VISIBLE);

                            Toast.makeText(RegistrationActivity.this,
                                    "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                            loadDriverData();
                        } else {
                            Toast.makeText(RegistrationActivity.this,
                                    message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isValidSelection(String enteredText, List<DropdownItem> list) {
        for (DropdownItem item : list) {
            if (item.getText().equalsIgnoreCase(enteredText.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean validateFields() {
        if (etFirstName.getText().toString().trim().isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        if (etLastName.getText().toString().trim().isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }

        String email = etEmail.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (etMobileNumber.getText().toString().trim().isEmpty()) {
            etMobileNumber.setError("Mobile number is required");
            etMobileNumber.requestFocus();
            return false;
        }
        String stateText = actvState.getText().toString().trim();
        if (stateText.isEmpty() || !isValidSelection(stateText, stateList)) {
            actvState.setError("Select a valid state from dropdown");
            actvState.requestFocus();
            return false;
        }

        String cityText = actvCity.getText().toString().trim();
        if (cityText.isEmpty() || !isValidSelection(cityText, cityList)) {
            actvCity.setError("Select a valid city from dropdown");
            actvCity.requestFocus();
            return false;
        }

        return true;
    }

    private void setFieldsEditable(boolean editable) {
        etFirstName.setEnabled(editable);
        etMiddleName.setEnabled(editable);
        etLastName.setEnabled(editable);
        etEmail.setEnabled(editable);
        etMobileNumber.setEnabled(editable);
        etAddress.setEnabled(editable);
        //etPincode.setEnabled(editable);
        actvCity.setEnabled(editable);
        actvState.setEnabled(editable);

    }

    private void showLoading(boolean show) {
        // Implement progress bar visibility
        // if (progressBar != null) {
        //     progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        // }
        btnEdit.setEnabled(!show);
        btnSave.setEnabled(!show);
    }
    private void loadDropdownData() {
        // Load States (type = 1)
        registrationRepository.getDropdownData("", 1, 100, 1, 0, (success, items, message) -> {
            if (success && items != null) {
                Log.d(TAG, "States loaded: " + items.size());
                stateList = items;

                stateAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, stateList);
                actvState.setAdapter(stateAdapter);

                actvState.setOnItemClickListener((parent, view, position, id) -> {
                    DropdownItem selectedState = (DropdownItem) parent.getItemAtPosition(position);
                    currentDriver.setStateId(selectedState.getId());
                    loadCities(selectedState.getId());
                });
            } else {
                Log.e(TAG, "Failed to load states: " + message);
                Toast.makeText(this, "Failed to load states: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCities(int stateId) {
        registrationRepository.getDropdownData("", 1, 100, 2, stateId, (success, items, message) -> {
            if (success && items != null) {
                Log.d(TAG, "Cities loaded: " + items.size());
                cityList = items;

                cityAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, cityList);
                actvCity.setAdapter(cityAdapter);
                actvCity.setEnabled(true); // Enable city dropdown

                actvCity.setOnItemClickListener((parent, view, position, id) -> {
                    DropdownItem selectedCity = (DropdownItem) parent.getItemAtPosition(position);
                    currentDriver.setCityId(selectedCity.getId());
                });

                // Set city if customer has one
                if (currentDriver != null && currentDriver.getCityId() > 0) {
                    for (DropdownItem item : cityList) {
                        if (item.getId() == currentDriver.getCityId()) {
                            actvCity.setText(item.getText(), false);
                            break;
                        }
                    }
                }
            } else {
                Log.e(TAG, "Failed to load cities: " + message);
                Toast.makeText(this, "Failed to load cities: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}