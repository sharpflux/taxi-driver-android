package com.sharpflux.taxiapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sharpflux.taxiapp.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository repository;

    // Correct constructor with Application
    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<Boolean> login(String Email, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        repository.login(Email, password, (success, message) -> result.postValue(success));

        return result;
    }
}
