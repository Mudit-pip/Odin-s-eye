package com.odinseyenavigation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.odinseyenavigation.fragments.fragment_info;
import com.odinseyenavigation.fragments.fragment_locationupdates;
import com.odinseyenavigation.fragments.fragment_qrscan_home;

public class E1_afterLogin extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e1_after_login);
        
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        // Load default fragment
        loadFragment(new fragment_qrscan_home());
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_updates) {
                fragment = new fragment_locationupdates();
            } else if (itemId == R.id.nav_home) {
                fragment = new fragment_qrscan_home();
            } else if (itemId == R.id.nav_info) {
                fragment = new fragment_info();
            }
            
            if (fragment != null) {
                loadFragment(fragment);
            }
            
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.E1_container, fragment);
        ft.commit();
    }
}