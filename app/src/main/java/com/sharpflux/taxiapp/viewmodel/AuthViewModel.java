package com.sharpflux.taxiapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sharpflux.taxiapp.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private AuthRepository repository = new AuthRepository();

    public LiveData<Boolean> login(String username, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.setValue(repository.login(username, password));
        return result;
    }
}
