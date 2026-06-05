package com.example.aistudyassistant.features.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.aistudyassistant.R;
import com.example.aistudyassistant.fragments.auth.LoginFragment;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            loadFragment(new LoginFragment());
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    
    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_container, fragment)
                .commit();
    }
}
