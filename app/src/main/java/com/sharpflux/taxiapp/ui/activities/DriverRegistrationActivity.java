package com.sharpflux.taxiapp.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.model.DocumentType;
import com.sharpflux.taxiapp.data.model.Driver;
import com.sharpflux.taxiapp.data.model.DropdownItem;
import com.sharpflux.taxiapp.data.repository.DriverRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import com.google.android.material.card.MaterialCardView;
import com.sharpflux.taxiapp.data.model.Driver.DriverLanguage;

public class DriverRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "DriverRegistration";

    private ViewFlipper viewFlipper;
    private DriverRepository repository;
    private Driver driver;
    private ImageView ivBackButton;

    // Step 1 - Personal Information
    private EditText etFirstName, etMiddleName, etLastName, etEmailId, etPhoneNumber, etAadhar, etPassword;
    private RadioGroup rgGender;
    private Button btnNext1;

    // Step 2 - Address Information
    private EditText etAddress, edtPincode, edtPanNumber, edtDrivingLicenceNo;
    private AutoCompleteTextView actvState, actvCity;
    private Button btnBack2, btnNext2;
    private List<DropdownItem> stateList = new ArrayList<>();
    private List<DropdownItem> cityList = new ArrayList<>();
    private ArrayAdapter<DropdownItem> stateAdapter;
    private ArrayAdapter<DropdownItem> cityAdapter;

    // Step 3 - Vehicle Information
    private EditText etVehicleNumber;
    private AutoCompleteTextView actvVehicleType,spnFuelType;
    private EditText etDlValidTo, etInsuranceValidTo, etVehicleValidTo;
    private Button btnBack3, btnNext3;
    private List<DropdownItem> vehicleTypeList = new ArrayList<>();
    private List<DropdownItem> fuelTypeList = new ArrayList<>();
    private ArrayAdapter<DropdownItem> vehicleTypeAdapter;
    private ArrayAdapter<DropdownItem> fuelTypeAdapter;

    // Step 4 - Dynamic Documents & Language
    private AutoCompleteTextView actvLanguage;
    private List<DropdownItem> languageList = new ArrayList<>();
    private ArrayAdapter<DropdownItem> languageAdapter;
    private CheckBox cbSpeak, cbUnderstand, cbTerms;
    private Button btnBack4, btnRegister;

    // Dynamic document containers
    private LinearLayout documentsContainer;
    private List<DocumentType> documentTypes = new ArrayList<>();
    private Map<Integer, Uri> documentUriMap = new HashMap<>();
    private Map<Integer, ImageView> documentImageViewMap = new HashMap<>();
    private int currentDocumentTypeId = 0;

    //dynamic language
    LinearLayout languageContainer;
    Button btnAddLanguage;
    private int selectedLanguageId = 0;

    List<DriverLanguage> selectedLanguages = new ArrayList<>();


    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private View rootView;
    private boolean termsAccepted = false;
    private MaterialCardView cvTermsConditions;
    private ImageView ivTermsCheck;
    private TextView tvTermsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        rootView = findViewById(android.R.id.content);

        repository = new DriverRepository(this);
        driver = new Driver();
        setupKeyboardDetection();

        // Default values
        driver.setUserId(1);
        driver.setIsActive(true);
        driver.setRoleId(2); // Driver role
        driver.setVerificationStatus(3); // Pending
        driver.setVerified(false);
        driver.setVerifiedBy(0);
        driver.setLocationId(0);

        initializeViews();

        languageContainer = findViewById(R.id.languageContainer);
        btnAddLanguage = findViewById(R.id.btnAddLanguage);

        setupImagePicker();
        setupListeners();
        loadDropdownData();
        loadDocumentTypes();

//        // Dynamic checkbox update on language select
//        actvLanguage.setOnItemClickListener((parent, view, position, id) -> {
//            String selectedLanguage = ((DropdownItem) parent.getItemAtPosition(position)).getText();
//            cbSpeak.setText("I can speak " + selectedLanguage);
//            cbUnderstand.setText("I can understand " + selectedLanguage);
//        });



    }

    private void setupKeyboardDetection() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = 0;

            @Override
            public void onGlobalLayout() {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                if (heightDiff > 200) {
                    onKeyboardVisible();
                } else if (previousHeight > 200 && heightDiff <= 200) {
                    onKeyboardHidden();
                }
                previousHeight = heightDiff;
            }
        });
    }

    private void onKeyboardVisible() {
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            focusedView.postDelayed(() -> scrollToView(focusedView), 100);
        }
    }

    private void onKeyboardHidden() {
        // Optional: Handle keyboard hidden event
    }

    private void scrollToView(final View view) {
        View currentView = viewFlipper.getChildAt(viewFlipper.getDisplayedChild());
        if (currentView instanceof NestedScrollView) {
            scrollNestedScrollView((NestedScrollView) currentView, view);
        } else {
            ScrollView scrollView = findScrollViewInView(currentView);
            if (scrollView != null) {
                scrollScrollView(scrollView, view);
            }
        }
    }

    private ScrollView findScrollViewInView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                ScrollView found = findScrollViewInView(group.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void scrollNestedScrollView(final NestedScrollView nestedScrollView, final View targetView) {
        nestedScrollView.post(() -> {
            int[] location = new int[2];
            targetView.getLocationOnScreen(location);
            int[] scrollLocation = new int[2];
            nestedScrollView.getLocationOnScreen(scrollLocation);
            int scrollY = location[1] - scrollLocation[1];
            int offset = 100;
            nestedScrollView.smoothScrollTo(0, scrollY - offset);
        });
    }

    private void scrollScrollView(final ScrollView scrollView, final View targetView) {
        scrollView.post(() -> {
            int[] location = new int[2];
            targetView.getLocationOnScreen(location);
            int[] scrollLocation = new int[2];
            scrollView.getLocationOnScreen(scrollLocation);
            int scrollY = location[1] - scrollLocation[1];
            int offset = 100;
            scrollView.smoothScrollTo(0, scrollY - offset);
        });
    }

    private void initializeViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        ivBackButton = findViewById(R.id.ivBackButton);

        // Step 1
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etEmailId = findViewById(R.id.etEmailId);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAadhar = findViewById(R.id.etAadhar);
        edtPanNumber = findViewById(R.id.edtPanNumber);
        etPassword = findViewById(R.id.etPassword);
        rgGender = findViewById(R.id.rgGender);
        btnNext1 = findViewById(R.id.btnNext1);

        // Step 2
        etAddress = findViewById(R.id.etAddress);
        actvState = findViewById(R.id.actvState);
        actvCity = findViewById(R.id.actvCity);
        edtPincode = findViewById(R.id.edtPincode);
        btnBack2 = findViewById(R.id.btnBack2);
        btnNext2 = findViewById(R.id.btnNext2);

        // Step 3
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        actvVehicleType = findViewById(R.id.actvVehicleType);
        spnFuelType = findViewById(R.id.spnFuelType);
        edtDrivingLicenceNo = findViewById(R.id.edtDrivingLicenceNo);
        etDlValidTo = findViewById(R.id.etDlValidTo);
        etInsuranceValidTo = findViewById(R.id.etInsuranceValidTo);
        etVehicleValidTo = findViewById(R.id.etVehicleValidTo);
        btnBack3 = findViewById(R.id.btnBack3);
        btnNext3 = findViewById(R.id.btnNext3);

        // Step 4
        documentsContainer = findViewById(R.id.documentsContainer);
        actvLanguage = findViewById(R.id.actvLanguage);
//        cbSpeak = findViewById(R.id.etSpeak);
//        cbUnderstand = findViewById(R.id.etUnderstand);
        btnBack4 = findViewById(R.id.btnBack4);
        btnRegister = findViewById(R.id.btnRegister);

        cvTermsConditions = findViewById(R.id.cvTermsConditions);
        ivTermsCheck = findViewById(R.id.ivTermsCheck);
        tvTermsStatus = findViewById(R.id.tvTermsStatus);

//        edtPincode = findViewById(R.id.edtPincode);
//        edtPanNumber = findViewById(R.id.edtPanNumber);
//        edtDrivingLicenceNo = findViewById(R.id.edtDrivingLicenceNo);
//
//
//        spnFuelType = findViewById(R.id.spnFuelType);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null && currentDocumentTypeId != 0) {
                            documentUriMap.put(currentDocumentTypeId, selectedImage);

                            ImageView imageView = documentImageViewMap.get(currentDocumentTypeId);
                            if (imageView != null) {
                                imageView.setImageURI(selectedImage);
                                imageView.setVisibility(View.VISIBLE);

                                // Hide placeholder
                                View parent = (View) imageView.getParent();
                                LinearLayout llPlaceholder = parent.findViewById(R.id.llPlaceholder);
                                if (llPlaceholder != null) llPlaceholder.setVisibility(View.GONE);
                            }

                            // Convert to Base64 and store
                            String base64Image = convertUriToBase64(selectedImage);
                            if (base64Image != null && !base64Image.isEmpty()) {
                                if (driver.getDocumentBase64Map() == null) {
                                    driver.setDocumentBase64Map(new HashMap<>());
                                }
                                driver.getDocumentBase64Map().put(currentDocumentTypeId, base64Image);
                                Log.d(TAG, "✅ Image stored for docId=" + currentDocumentTypeId +
                                        ", size=" + (base64Image.length() / 1024) + " KB");
                            } else {
                                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        ivBackButton.setOnClickListener(v -> handleBackPress());

        btnAddLanguage.setOnClickListener(v -> addLanguageCard());

        btnNext1.setOnClickListener(v -> {
            if (validateStep1()) {
                saveStep1Data();
                viewFlipper.showNext();
            }
        });

        btnBack2.setOnClickListener(v -> viewFlipper.showPrevious());

        btnNext2.setOnClickListener(v -> {
            if (validateStep2()) {
                saveStep2Data();
                viewFlipper.showNext();
            }
        });

        btnBack3.setOnClickListener(v -> viewFlipper.showPrevious());

        btnNext3.setOnClickListener(v -> {
            if (validateStep3()) {
                saveStep3Data();
                viewFlipper.showNext();
            }
        });

        btnBack4.setOnClickListener(v -> viewFlipper.showPrevious());
        btnRegister.setOnClickListener(v -> registerDriver());

        etDlValidTo.setOnClickListener(v -> showDatePicker(etDlValidTo));
        etInsuranceValidTo.setOnClickListener(v -> showDatePicker(etInsuranceValidTo));
        etVehicleValidTo.setOnClickListener(v -> showDatePicker(etVehicleValidTo));

        actvState.setOnItemClickListener((parent, view, position, id) -> {
            DropdownItem selectedState = (DropdownItem) parent.getItemAtPosition(position);
            Log.d(TAG, "State selected: " + selectedState.getText());
            actvCity.setText("");
            cityList.clear();
            loadCities(selectedState.getId());
        });

        cvTermsConditions.setOnClickListener(v -> showTermsDialog());
    }
    private void showTermsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_terms_conditions);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        CheckBox cbAgreeTerms = dialog.findViewById(R.id.cbAgreeTerms);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnAccept = dialog.findViewById(R.id.btnAccept);

        // Initialize checkbox based on current state
        cbAgreeTerms.setChecked(termsAccepted);

        // Disable accept button initially
        btnAccept.setEnabled(termsAccepted);
        btnAccept.setAlpha(termsAccepted ? 1f : 0.5f);

        // Enable button when checked
        cbAgreeTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAccept.setEnabled(isChecked);
            btnAccept.setAlpha(isChecked ? 1f : 0.5f);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAccept.setOnClickListener(v -> {
            termsAccepted = cbAgreeTerms.isChecked();
            updateTermsUI(termsAccepted);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateTermsUI(boolean accepted)
    {
        if (accepted) {
            tvTermsStatus.setText("Terms & Conditions Accepted ✓");
            tvTermsStatus.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            ivTermsCheck.setImageResource(android.R.drawable.checkbox_on_background);
            ivTermsCheck.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent));
        }
        else {
            tvTermsStatus.setText(getString(R.string.view_terms_conditions));
            tvTermsStatus.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            ivTermsCheck.setImageResource(android.R.drawable.checkbox_off_background);
            ivTermsCheck.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }



    private void handleBackPress() {
        int currentStep = viewFlipper.getDisplayedChild();
        if (currentStep == 0) {
            finish();
        } else {
            viewFlipper.showPrevious();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackPress();
    }

    private class SearchableDropdownAdapter extends ArrayAdapter<DropdownItem> implements Filterable {
        private final List<DropdownItem> allItems;
        private final List<DropdownItem> filteredItems;

        public SearchableDropdownAdapter(List<DropdownItem> items) {
            super(DriverRegistrationActivity.this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(items));
            this.allItems = new ArrayList<>(items);
            this.filteredItems = new ArrayList<>(items);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    filteredItems.clear();
                    if (constraint == null || constraint.length() == 0) {
                        filteredItems.addAll(allItems);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (DropdownItem item : allItems) {
                            if (item.getText().toLowerCase().contains(filterPattern)) {
                                filteredItems.add(item);
                            }
                        }
                    }
                    results.values = filteredItems;
                    results.count = filteredItems.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    if (results != null && results.values != null) {
                        addAll((List<DropdownItem>) results.values);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return ((DropdownItem) resultValue).getText();
                }
            };
        }
    }

    private void loadDropdownData() {
        // States
        repository.getDropdownData("", 1, 100, 1, 0, (success, items, message) -> {
            if (success && items != null) {
                stateList = items;
                stateAdapter = new SearchableDropdownAdapter(stateList);
                actvState.setAdapter(stateAdapter);
                actvState.setThreshold(1);
                actvState.setOnClickListener(v -> actvState.showDropDown());
            }
        });

        // Vehicle types
        repository.getDropdownData("", 1, 100, 4, 0, (success, items, message) -> {
            if (success && items != null) {
                vehicleTypeList = items;
                vehicleTypeAdapter = new SearchableDropdownAdapter(vehicleTypeList);
                actvVehicleType.setAdapter(vehicleTypeAdapter);
                actvVehicleType.setThreshold(1);
                actvVehicleType.setOnClickListener(v -> actvVehicleType.showDropDown());
            }
        });

        // Fuel types
        repository.getDropdownData("", 1, 100, 10, 0, (success, items, message) -> {
            if (success && items != null) {
                fuelTypeList = items;
                fuelTypeAdapter = new SearchableDropdownAdapter(fuelTypeList);
                spnFuelType.setAdapter(fuelTypeAdapter);
                spnFuelType.setThreshold(1);
                spnFuelType.setOnClickListener(v -> spnFuelType.showDropDown());
            }
        });

        // Language Dropdown
        repository.getDropdownData("", 1, 100, 9, 0, (success, items, message) -> {
            if (success && items != null) {
                languageList = items;
                languageAdapter = new SearchableDropdownAdapter(languageList);
                actvLanguage.setAdapter(languageAdapter);
                actvLanguage.setThreshold(1);
                actvLanguage.setOnClickListener(v -> actvLanguage.showDropDown());

                // ✅ ADD THIS: Capture selected language ID
                actvLanguage.setOnItemClickListener((parent, view, position, id) -> {
                    DropdownItem selectedItem = (DropdownItem) parent.getItemAtPosition(position);
                    selectedLanguageId = selectedItem.getId();
                    Log.d(TAG, "Language selected: " + selectedItem.getText() + " (ID: " + selectedLanguageId + ")");
                });
            } else {
                Log.e(TAG, "Language dropdown failed: " + message);
            }
        });
    }

    private void loadDocumentTypes() {
        repository.getDocumentTypes((success, types, message) -> {
            if (success && types != null && !types.isEmpty()) {
                documentTypes.clear();
                documentTypes.addAll(types);
                createDynamicDocumentUI();
            } else {
                Toast.makeText(this, "Failed to load document types: " + message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Document fetch failed: " + message);
            }
        });
    }

    private void addLanguageCard() {
        String selectedLangName = actvLanguage.getText().toString().trim();

        if (selectedLangName.isEmpty() || selectedLanguageId == 0) {
            Toast.makeText(this, "Please choose a language from dropdown", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for duplicates
        for (Driver.DriverLanguage dl : selectedLanguages) {
            if (dl.getLanguageId() == selectedLanguageId) {
                Toast.makeText(this, "Language already added", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        View card = LayoutInflater.from(this).inflate(R.layout.item_language_card, languageContainer, false);

        TextView tvLangName = card.findViewById(R.id.tvLangName);
        CheckBox cbSpeak = card.findViewById(R.id.cbSpeak);
        CheckBox cbRead = card.findViewById(R.id.cbRead);
        CheckBox cbWrite = card.findViewById(R.id.cbWrite);
        ImageView ivRemove = card.findViewById(R.id.ivRemoveLanguage);

        tvLangName.setText(selectedLangName);

        // Create language model
        Driver.DriverLanguage dl = new Driver.DriverLanguage();
        dl.setDriverLanguageId(0);
        dl.setDriversId(0);
        dl.setLanguageId(selectedLanguageId);
        dl.setCanSpeak(false);
        dl.setCanRead(false);
        dl.setCanWrite(false);

        selectedLanguages.add(dl);

        // Update model when checkboxes change
        cbSpeak.setOnCheckedChangeListener((compoundButton, checked) -> dl.setCanSpeak(checked));
        cbRead.setOnCheckedChangeListener((compoundButton, checked) -> dl.setCanRead(checked));
        cbWrite.setOnCheckedChangeListener((compoundButton, checked) -> dl.setCanWrite(checked));

        // Remove card functionality
        if (ivRemove != null) {
            ivRemove.setOnClickListener(v -> {
                languageContainer.removeView(card);
                selectedLanguages.remove(dl);
                Toast.makeText(this, "Language removed", Toast.LENGTH_SHORT).show();
            });
        }

        // Add card to container
        languageContainer.addView(card);

        // Clear selection
        actvLanguage.setText("");
        selectedLanguageId = 0;

        Toast.makeText(this, "Language added", Toast.LENGTH_SHORT).show();
    }

    private void createDynamicDocumentUI() {
        documentsContainer.removeAllViews();
        documentUriMap.clear();
        documentImageViewMap.clear();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DocumentType docType : documentTypes) {
            View documentCard = inflater.inflate(R.layout.item_document_upload, documentsContainer, false);

            TextView tvDocumentName = documentCard.findViewById(R.id.tvDocumentName);
            ImageView ivDocument = documentCard.findViewById(R.id.ivDocument);
            LinearLayout llPlaceholder = documentCard.findViewById(R.id.llPlaceholder);
            Button btnUpload = documentCard.findViewById(R.id.btnUploadDocument);

            tvDocumentName.setText(docType.getDocumentName() + " *");
            documentImageViewMap.put(docType.getDocumentTypeId(), ivDocument);

            btnUpload.setOnClickListener(v -> {
                currentDocumentTypeId = docType.getDocumentTypeId();
                openImagePicker(docType.getDocumentTypeId());
            });

            ivDocument.setVisibility(View.GONE);
            llPlaceholder.setVisibility(View.VISIBLE);

            documentsContainer.addView(documentCard);
        }
    }

    private void loadCities(int stateId) {
        repository.getDropdownData("", 1, 100, 2, stateId, (success, items, message) -> {
            if (success && items != null) {
                cityList = items;
                cityAdapter = new SearchableDropdownAdapter(cityList);
                actvCity.setAdapter(cityAdapter);
                actvCity.setEnabled(true);
                actvCity.setThreshold(1);
                actvCity.setOnClickListener(v -> actvCity.showDropDown());
            }
        });
    }

    private boolean validateStep1() {
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
        String email = etEmailId.getText().toString().trim();

        // Email NOT required, but if entered → must be valid
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailId.setError("Enter a valid email");
            etEmailId.requestFocus();
            return false;
        }

        if (etPhoneNumber.getText().toString().trim().length() < 10) {
            etPhoneNumber.setError("Valid 10-digit phone number required");
            etPhoneNumber.requestFocus();
            return false;
        }
        if (etAadhar.getText().toString().trim().length() != 12) {
            etAadhar.setError("Valid 12-digit Aadhar required");
            etAadhar.requestFocus();
            return false;
        }
        String pan = edtPanNumber.getText().toString().trim();

        if (!pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
            edtPanNumber.setError("Enter valid PAN (eg.ABCDE1234F)");
            edtPanNumber.requestFocus();
            return false;
        }

        if (etPassword.getText().toString().trim().length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep2() {

        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return false;
        }

        if (actvState.getText().toString().trim().isEmpty()) {
            actvState.setError("State is required");
            actvState.requestFocus(); return false;
        }
        if (actvCity.getText().toString().trim().isEmpty()) {
            actvCity.setError("City is required");
            actvCity.requestFocus(); return false;
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

        if (edtPincode.getText().toString().trim().isEmpty()) {
            edtPincode.setError("Pin code is required");
            edtPincode.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateStep3() {
        if (etVehicleNumber.getText().toString().trim().isEmpty()) {
            etVehicleNumber.setError("Vehicle number required");
            etVehicleNumber.requestFocus();
            return false;
        }
        if (actvVehicleType.getText().toString().trim().isEmpty()) {
            actvVehicleType.setError("Vehicle type required");
            actvVehicleType.requestFocus();
            return false;
        }
        if (spnFuelType.getText().toString().trim().isEmpty()) {
            spnFuelType.setError("Fuel type required");
            spnFuelType.requestFocus();
            return false;
        }
        String vehicleType = actvVehicleType.getText().toString().trim();
        if (vehicleType.isEmpty() || !isValidSelection(vehicleType, vehicleTypeList)) {
            actvVehicleType.setError("Select valid vehicle type");
            actvVehicleType.requestFocus();
            return false;
        }

        String fuelType = spnFuelType.getText().toString().trim();
        if (fuelType.isEmpty() || !isValidSelection(fuelType, fuelTypeList)) {
            spnFuelType.setError("Select valid fuel type");
            spnFuelType.requestFocus();
            return false;
        }
        String dl = edtDrivingLicenceNo.getText().toString().trim();
        String dlPattern = "^[A-Z]{2}[ -]?[0-9]{2}[ -]?[0-9]{4}[0-9]{7,10}$";

        if (dl.isEmpty()) {
            edtDrivingLicenceNo.setError("Licence number required");
            edtDrivingLicenceNo.requestFocus();
            return false;
        }

        if (!dl.matches(dlPattern)) {
            edtDrivingLicenceNo.setError("Invalid licence number format");
            edtDrivingLicenceNo.requestFocus();
            return false;
        }

        edtDrivingLicenceNo.setError(null);

        if (etDlValidTo.getText().toString().trim().isEmpty()) {
            etDlValidTo.setError("Driving License valid date required");
            etDlValidTo.requestFocus();
            return false;
        }
        if (etInsuranceValidTo.getText().toString().trim().isEmpty()) {
            etInsuranceValidTo.setError("Insurance valid date required");
            etInsuranceValidTo.requestFocus();
            return false;
        }
        if (etVehicleValidTo.getText().toString().trim().isEmpty()) {
            etVehicleValidTo.setError("Vehicle valid date required");
            etVehicleValidTo.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isValidSelection(String enteredText, List<DropdownItem> list) {
        for (DropdownItem item : list) {
            if (item.getText().equalsIgnoreCase(enteredText.trim())) {
                return true;
            }
        }
        return false;
    }


    private void saveStep1Data() {
        driver.setFirstName(etFirstName.getText().toString().trim());
        driver.setMiddleName(etMiddleName.getText().toString().trim());
        driver.setLastName(etLastName.getText().toString().trim());
        driver.setEmailId(etEmailId.getText().toString().trim());
        driver.setPhoneNumber(etPhoneNumber.getText().toString().trim());
        driver.setAadharNumber(etAadhar.getText().toString().trim());
        driver.setPanNumber(edtPanNumber.getText().toString().trim());
        driver.setPassword(etPassword.getText().toString().trim());
        driver.setGenderId((rgGender.getCheckedRadioButtonId() == R.id.rbMale) ? 1 : 2);

        Log.d(TAG, "Step 1 saved: " + driver.getFirstName() + " " + driver.getLastName());
    }

    private void saveStep2Data() {
        driver.setAddress(etAddress.getText().toString().trim());

        String stateText = actvState.getText().toString();
        for (DropdownItem item : stateList) {
            if (item.getText().equals(stateText)) {
                driver.setStateId(item.getId());
                break;
            }
        }

        String cityText = actvCity.getText().toString();
        for (DropdownItem item : cityList) {
            if (item.getText().equals(cityText)) {
                driver.setCityId(item.getId());
                break;
            }
        }
        driver.setPincode(edtPincode.getText().toString().trim());

        Log.d(TAG, "Step 2 saved: StateId=" + driver.getStateId() + ", CityId=" + driver.getCityId());
    }

    private void saveStep3Data() {
        driver.setVehicleNumber(etVehicleNumber.getText().toString().trim().toUpperCase());
        driver.setDrivingLicenceNo(edtDrivingLicenceNo.getText().toString().trim());
        driver.setDrivingLicenseValidTo(etDlValidTo.getText().toString().trim());
        driver.setInsuranceValidTo(etInsuranceValidTo.getText().toString().trim());
        driver.setVehicleValidTo(etVehicleValidTo.getText().toString().trim());

        String vehicleTypeText = actvVehicleType.getText().toString();
        for (DropdownItem item : vehicleTypeList) {
            if (item.getText().equals(vehicleTypeText)) {
                driver.setVehicleTypeId(item.getId());
                break;
            }
        }
        String fuelTypeText = spnFuelType.getText().toString();
        for (DropdownItem item : fuelTypeList) {
            if (item.getText().equals(fuelTypeText)) {
                driver.setFuelTypeId(item.getId());
                break;
            }
        }

        Log.d(TAG, "Step 3 saved: Vehicle=" + driver.getVehicleNumber() +
                ", TypeId=" + driver.getVehicleTypeId());
    }

    private void saveStep4Data() {

        driver.setLanguages(selectedLanguages);

        driver.setTermsConditions(termsAccepted);

//        Log.d(TAG, "Step 4 saved: LanguageId=" + driver.getLanguageId() +
//                ", Speak=" + driver.isSpeak() + ", Understand=" + driver.isUnderstand());
        Log.d(TAG, "Step 4 saved: TermsAccepted=" + termsAccepted);
    }

    private void openImagePicker(int documentTypeId) {
        currentDocumentTypeId = documentTypeId;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showDatePicker(EditText editText) {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .setTheme(R.style.CustomDatePickerTheme)
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            editText.setText(sdf.format(new Date(selection)));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }


    private void registerDriver() {
        saveStep4Data();

        // Validate all required documents are uploaded
        for (DocumentType docType : documentTypes) {
            if (docType.isActive() && !documentUriMap.containsKey(docType.getDocumentTypeId())) {
                Toast.makeText(this, "Please upload " + docType.getDocumentName(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Check if terms are accepted using the boolean flag
        if (!termsAccepted) {
            Toast.makeText(this, getString(R.string.please_accept_terms), Toast.LENGTH_LONG).show();
            cvTermsConditions.requestFocus();
            // Optionally shake the card to draw attention
            cvTermsConditions.animate()
                    .translationX(-10f)
                    .setDuration(100)
                    .withEndAction(() ->
                            cvTermsConditions.animate()
                                    .translationX(10f)
                                    .setDuration(100)
                                    .withEndAction(() ->
                                            cvTermsConditions.animate()
                                                    .translationX(0f)
                                                    .setDuration(100)
                                                    .start()
                                    )
                                    .start()
                    )
                    .start();
            return;
        }

        // Final validation
        Map<Integer, String> base64Map = driver.getDocumentBase64Map();
        if (base64Map == null || base64Map.isEmpty()) {
            Toast.makeText(this, "Please upload all required documents", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable button to prevent double submission
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        Log.d(TAG, "=== STARTING REGISTRATION ===");
        Log.d(TAG, "Driver: " + driver.getFirstName() + " " + driver.getLastName());
        Log.d(TAG, "Documents count: " + base64Map.size());
        Log.d(TAG, "Terms Accepted: " + termsAccepted);

        repository.registerDriver(driver, (success, message) -> {
            runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register as Driver");

                if (success) {
                    Toast.makeText(this, "✅ Registration successful!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "✅ Registration completed successfully");
                    finish();
                } else {
                    Toast.makeText(this, "❌ " + message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "❌ Registration failed: " + message);
                }
            });
        });
    }

    private String convertUriToBase64(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            // Resize image to max 800px
            int maxSize = 800;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxSize || height > maxSize) {
                float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                bitmap.recycle();
                bitmap = resized;
                Log.d(TAG, "Image resized to: " + newWidth + "x" + newHeight);
            }

            // Compress to JPEG with 70% quality
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            bitmap.recycle();

            // Use NO_WRAP to avoid newlines
            String base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            Log.d(TAG, "Base64 created - Size: " + (base64.length() / 1024) + " KB");
            return base64;

        } catch (IOException e) {
            Log.e(TAG, "Error converting image to Base64", e);
            return null;
        }
    }

}