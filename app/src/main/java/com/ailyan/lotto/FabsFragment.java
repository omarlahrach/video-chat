package com.ailyan.lotto;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabsFragment extends Fragment {
    private boolean fab_theme_clicked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab_theme = view.findViewById(R.id.fab_theme);

        int defaultTheme = AppCompatDelegate.getDefaultNightMode();
        fab_theme_clicked = defaultTheme == AppCompatDelegate.MODE_NIGHT_YES;
        if (fab_theme_clicked) {
            fab_theme.setImageResource(R.drawable.ic_night);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            fab_theme.setImageResource(R.drawable.ic_light);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        fab_theme.setOnClickListener(fab -> {
            if (fab_theme_clicked) {
                fab_theme.setImageResource(R.drawable.ic_light);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                fab_theme_clicked = false;
            } else {
                fab_theme.setImageResource(R.drawable.ic_night);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                fab_theme_clicked = true;
            }
        });
    }
}