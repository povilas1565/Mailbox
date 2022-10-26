package com.example.mailbox.ui.mailbox.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import com.example.mailbox.R;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        SwitchCompat switchCompat = rootView.findViewById(R.id.showNotificationSwitch);

        SharedPreferences sharedPref = getContext().getSharedPreferences("notification", Context.MODE_PRIVATE);
        boolean showNotificationSettings = sharedPref.getBoolean("showNotification", true);
        switchCompat.setChecked(showNotificationSettings);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (getContext() == null)
                    return;
                SharedPreferences sharedPref = getContext().getSharedPreferences("notification", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if (isChecked){
                    editor.putBoolean("showNotification", true).apply();
                } else {
                    editor.putBoolean("showNotification", false).apply();
                }
            }
        });

        Button changeEmailButton = rootView.findViewById(R.id.changeEmailButton);
        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangeEmailActivity.class);
                startActivity(intent);
            }
        });

        Button changePasswordButton = rootView.findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu_profile));
        super.onCreateOptionsMenu(menu, inflater);
    }
}