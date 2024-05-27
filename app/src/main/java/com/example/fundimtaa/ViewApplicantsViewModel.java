package com.example.fundimtaa;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class ViewApplicantsViewModel extends ViewModel {
    private final MutableLiveData<List<Worker>> workersLiveData = new MutableLiveData<>();

    public LiveData<List<Worker>> getWorkersLiveData() {
        return workersLiveData;
    }

    public void setWorkers(List<Worker> workers) {
        workersLiveData.setValue(workers);
    }
}

