package com.odinseye;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class E4_scansare_over extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e4_scansare_over);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(E4_scansare_over.this, E1_afterlogin.class));
        finish();
    }
}