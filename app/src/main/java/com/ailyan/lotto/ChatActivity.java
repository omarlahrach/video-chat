package com.ailyan.lotto;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ailyan.lotto.databinding.ActivityChatBinding;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "###";
    private static final int RC_CALL = 1;
    private ActivityChatBinding binding;
    private int currentPlayer = -1;
    private boolean isVideoEnabled;
    private boolean isMicEnabled;
    private boolean isCameraSwitched;
    private boolean isAudioEnabled;
    private TypedValue typedValue;
    private Resources.Theme theme;
    private OptionsViewModel optionsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        boolean chatEnabled = intent.getBooleanExtra("chatEnabled", false);
        if (chatEnabled) {
            isVideoEnabled = intent.getBooleanExtra("isVideoEnabled", true);
            isMicEnabled = intent.getBooleanExtra("isMicEnabled", true);
            isCameraSwitched = intent.getBooleanExtra("isCameraSwitched", false);
            isAudioEnabled = intent.getBooleanExtra("isAudioEnabled", true);
            start();
            binding.chat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CALL)
    private void start() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            SignallingServer signallingServer = new ViewModelProvider(this).get(SignallingServer.class);
            signallingServer.addPlayer().observe(this, currentPlayer -> {
                boolean alreadyAdded = this.currentPlayer == currentPlayer;
                if (!alreadyAdded) {
                    this.currentPlayer = currentPlayer;
                    Log.d(TAG, "############ Current player : " + currentPlayer + " ############");
                    SdpServices sdpServices = new SdpServices(this, currentPlayer, binding);

                    optionsViewModel = new ViewModelProvider(this).get(OptionsViewModel.class);
                    typedValue = new TypedValue();
                    theme = getTheme();

                    initVideo();
                    initMic();
                    initCamera();
                    initAudio();

                    binding.video.setOnClickListener(video -> checkVideo());
                    binding.mic.setOnClickListener(mic -> checkMic());
                    binding.camera.setOnClickListener(camera -> checkCamera());
                    binding.audio.setOnClickListener(audio -> checkAudio());
                    binding.end.setOnClickListener(view -> sdpServices.end());
                }
            });
        } else
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
    }

    private void initVideo() {
        if (!isVideoEnabled) {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
            binding.video.setImageResource(R.drawable.ic_baseline_videocam_off_24);
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            binding.video.setImageResource(R.drawable.ic_baseline_videocam_24);
        }
        @ColorInt int color = typedValue.data;
        binding.video.setBackgroundTintList(ColorStateList.valueOf(color));
        optionsViewModel.isVideoEnabled.postValue(isVideoEnabled);
    }

    private void initMic() {
        if (!isMicEnabled) {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
            binding.mic.setImageResource(R.drawable.ic_baseline_mic_off_24);
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            binding.mic.setImageResource(R.drawable.ic_baseline_mic_24);
        }
        @ColorInt int color = typedValue.data;
        binding.mic.setBackgroundTintList(ColorStateList.valueOf(color));
        optionsViewModel.isMicEnabled.postValue(isMicEnabled);
    }

    private void initCamera() {
        if (isCameraSwitched) {
            optionsViewModel.isCameraSwitched.postValue(true);
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
        }
        @ColorInt int color = typedValue.data;
        binding.camera.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void initAudio() {
        if (!isAudioEnabled) {
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
            binding.audio.setImageResource(R.drawable.ic_baseline_hearing_disabled_24);
        } else {
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            binding.audio.setImageResource(R.drawable.ic_baseline_hearing_24);
        }
        @ColorInt int color = typedValue.data;
        binding.audio.setBackgroundTintList(ColorStateList.valueOf(color));
        optionsViewModel.isAudioEnabled.postValue(isAudioEnabled);
    }

    private void checkVideo() {
        if (isVideoEnabled) {
            isVideoEnabled = false;
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
            binding.video.setImageResource(R.drawable.ic_baseline_videocam_off_24);
        } else {
            isVideoEnabled = true;
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            binding.video.setImageResource(R.drawable.ic_baseline_videocam_24);
        }
        @ColorInt int color = typedValue.data;
        binding.video.setBackgroundTintList(ColorStateList.valueOf(color));
        optionsViewModel.isVideoEnabled.postValue(isVideoEnabled);
    }

    private void checkMic() {
        if (isMicEnabled) {
            isMicEnabled = false;
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
            binding.mic.setImageResource(R.drawable.ic_baseline_mic_off_24);
        } else {
            isMicEnabled = true;
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            binding.mic.setImageResource(R.drawable.ic_baseline_mic_24);
        }
        @ColorInt int color = typedValue.data;
        binding.mic.setBackgroundTintList(ColorStateList.valueOf(color));
        optionsViewModel.isMicEnabled.postValue(isMicEnabled);
    }

    private void checkCamera() {
        if (isCameraSwitched) {
            isCameraSwitched = false;
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        } else {
            isCameraSwitched = true;
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
        }
        @ColorInt int color = typedValue.data;
        binding.camera.setBackgroundTintList(ColorStateList.valueOf(color));
        optionsViewModel.isCameraSwitched.postValue(isCameraSwitched);
    }

    private void checkAudio() {
        if (isAudioEnabled) {
            isAudioEnabled = false;
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
            binding.audio.setImageResource(R.drawable.ic_baseline_hearing_disabled_24);
        } else {
            isAudioEnabled = true;
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            binding.audio.setImageResource(R.drawable.ic_baseline_hearing_24);
        }
        @ColorInt int color = typedValue.data;
        binding.audio.setBackgroundTintList(ColorStateList.valueOf(color));
        optionsViewModel.isAudioEnabled.postValue(isAudioEnabled);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }
}
