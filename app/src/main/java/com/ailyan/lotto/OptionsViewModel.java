package com.ailyan.lotto;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OptionsViewModel extends ViewModel {
    public final MutableLiveData<Boolean> isMicEnabled = new MutableLiveData<>();
    public final MutableLiveData<Boolean>  isVideoEnabled = new MutableLiveData<>();
    public final MutableLiveData<Boolean>  isCameraSwitched = new MutableLiveData<>();
    public final MutableLiveData<Boolean>  isAudioEnabled = new MutableLiveData<>();

    public MutableLiveData<Boolean> isMicEnabled() {
        return isMicEnabled;
    }

    public MutableLiveData<Boolean> isVideoEnabled() {
        return isVideoEnabled;
    }

    public MutableLiveData<Boolean> isCameraSwitched() {
        return isCameraSwitched;
    }

    public MutableLiveData<Boolean> isAudioEnabled() {
        return isAudioEnabled;
    }
}
