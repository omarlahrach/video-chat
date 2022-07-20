package com.ailyan.lotto;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import com.ailyan.lotto.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private boolean chatEnabled;
    private boolean isVideoEnabled = true;
    private boolean isMicEnabled = true;
    private boolean isCameraSwitched = false;
    private boolean isAudioEnabled = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.chat.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            chatEnabled = isChecked;
            binding.controls.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        binding.video.setOnClickListener(video -> {
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
        });
        
        binding.mic.setOnClickListener(mic -> {
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
        });
        
        binding.camera.setOnClickListener(camera -> {
            if (isCameraSwitched) {
                isCameraSwitched = false;
                theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
            } else {
                isCameraSwitched = true;
                theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            }
            @ColorInt int color = typedValue.data;
            binding.camera.setBackgroundTintList(ColorStateList.valueOf(color));
        });
        
        binding.audio.setOnClickListener(audio -> {
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
        });
        
        binding.play.setOnClickListener(play -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatEnabled", chatEnabled);
            if (chatEnabled) {
                intent.putExtra("isVideoEnabled", isVideoEnabled);
                intent.putExtra("isMicEnabled", isMicEnabled);
                intent.putExtra("isCameraSwitched", isCameraSwitched);
                intent.putExtra("isAudioEnabled", isAudioEnabled);
            }
            startActivity(intent);
        });
    }
}












