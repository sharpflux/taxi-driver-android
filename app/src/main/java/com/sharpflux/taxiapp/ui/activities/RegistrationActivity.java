package com.sharpflux.taxiapp.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.model.Customer;
import com.sharpflux.taxiapp.data.model.DropdownItem;
import com.sharpflux.taxiapp.data.model.Registration;
import com.sharpflux.taxiapp.data.repository.CustomerRepository;
import com.sharpflux.taxiapp.data.repository.RegistrationRepository;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";

    private EditText etFirstName, etMiddleName, etLastName, etEmail, etMobileNumber, etAddress, etPincode;
    private AutoCompleteTextView actvCity, actvState;
    private ImageButton btnBackArrow;
    private Button btnEdit, btnSave;

    private List<DropdownItem> stateList = new ArrayList<>();
    private List<DropdownItem> cityList = new ArrayList<>();
    private ArrayAdapter<DropdownItem> stateAdapter;
    private ArrayAdapter<DropdownItem> cityAdapter;
    private boolean isEditing = false;
    private CustomerRepository customerRepository;
    private RegistrationRepository registrationRepository;
    private Customer currentCustomer;
    private int customerId = 0; // You can pass this from previous activity via Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializeViews();
        initializeRepositories();
        setupListeners();
        loadDropdownData();

        // Get customer ID from intent if passed
        if (getIntent().hasExtra("CUSTOMER_ID")) {
            customerId = getIntent().getIntExtra("CUSTOMER_ID", 0);
        }

        // Load customer data
        if (customerId > 0) {
            loadCustomerData(customerId);
        } else {
            // Load first customer for demo (or keep fields empty for new registration)
            loadCustomerData(1); // Change this based on your requirement
        }
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etAddress = findViewById(R.id.etAddress);
        etPincode = findViewById(R.id.etPincode);
        actvCity = findViewById(R.id.actvCity);
        actvState = findViewById(R.id.actvState);

        btnBackArrow = findViewById(R.id.btnBackArrow);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);

        // Add ProgressBar to your layout if not already present
        // progressBar = findViewById(R.id.progressBar);
    }

    private void initializeRepositories() {
        customerRepository = new CustomerRepository(this);
        registrationRepository = new RegistrationRepository(this);
    }

    private void setupListeners() {
        btnBackArrow.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> enableEditing());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCustomerData(int startIndex) {
        showLoading(true);
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int customersId = prefs.getInt("user_id", 0);
        customerRepository.getCustomers(startIndex, 10, "0", "0",customersId,
                new CustomerRepository.CustomerCallback() {
                    @Override
                    public void onSuccess(List<Customer> customers) {
                        showLoading(false);
                        if (customers != null && !customers.isEmpty()) {
                            currentCustomer = customers.get(0);
                            bindCustomerData(currentCustomer);
                        } else {
                            Toast.makeText(RegistrationActivity.this,
                                    "No customer data found", Toast.LENGTH_SHORT).show();
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

    private void bindCustomerData(Customer customer) {
        if (customer == null) return;

        etFirstName.setText(customer.getFirstName() != null ? customer.getFirstName() : "");
        etMiddleName.setText(customer.getMiddleName() != null ? customer.getMiddleName() : "");
        etLastName.setText(customer.getLastName() != null ? customer.getLastName() : "");
        etEmail.setText(customer.getEmailId() != null ? customer.getEmailId() : "");
        etMobileNumber.setText(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "");
        etAddress.setText(customer.getAddress() != null ? customer.getAddress() : "");
        etPincode.setText(customer.getPincode() != null ? customer.getPincode() : "");

        // Set state and city after dropdowns are loaded
        if (customer.getStateId() > 0) {
            for (DropdownItem item : stateList) {
                if (item.getId() == customer.getStateId()) {
                    actvState.setText(item.getText(), false);
                    loadCities(customer.getStateId());
                    break;
                }
            }
        }

        if (customer.getCityId() > 0) {
            for (DropdownItem item : cityList) {
                if (item.getId() == customer.getCityId()) {
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

        // Create Registration object from form data
        Registration registration = new Registration();
        registration.setFirstName(etFirstName.getText().toString().trim());
        registration.setMiddleName(etMiddleName.getText().toString().trim());
        registration.setLastName(etLastName.getText().toString().trim());
        registration.setEmailId(etEmail.getText().toString().trim());
        registration.setPhoneNumber(etMobileNumber.getText().toString().trim());
        registration.setAddress(etAddress.getText().toString().trim());

        // TODO: Get actual city and state IDs from your dropdown selections
        registration.setCityId(currentCustomer != null ? currentCustomer.getCityId() : 0);
        registration.setStateId(currentCustomer != null ? currentCustomer.getStateId() : 0);
        registration.setRoleId(currentCustomer != null ? currentCustomer.getRoleId() : 1);
        registration.setActive(true);

        registrationRepository.registerUser(registration,
                new RegistrationRepository.RegistrationCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        showLoading(false);
                        isEditing = false;
                        setFieldsEditable(false);
                        btnSave.setVisibility(View.GONE);
                        btnEdit.setVisibility(View.VISIBLE);
                        Toast.makeText(RegistrationActivity.this,
                                "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(RegistrationActivity.this,
                                "Error saving profile: " + error, Toast.LENGTH_LONG).show();
                    }
                });
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

        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (etMobileNumber.getText().toString().trim().isEmpty()) {
            etMobileNumber.setError("Mobile number is required");
            etMobileNumber.requestFocus();
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
        etPincode.setEnabled(editable);
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
                    currentCustomer.setStateId(selectedState.getId());
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
                    currentCustomer.setCityId(selectedCity.getId());
                });

                // Set city if customer has one
                if (currentCustomer != null && currentCustomer.getCityId() > 0) {
                    for (DropdownItem item : cityList) {
                        if (item.getId() == currentCustomer.getCityId()) {
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