package com.odinseye;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class U1_enterdata_todatabase extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference ref;
    EditText qr_key_head, qr_number, qr_question, qr_scans, qr_value, qr_answer, qr_points;
    Button send_data_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u1_enterdata_todatabase);

        qr_key_head = findViewById(R.id.U1_qr_qrval_head);
        qr_number = findViewById(R.id.U1_qr_number);
        qr_question = findViewById(R.id.U1_qr_question);
        qr_scans = findViewById(R.id.U1_qr_scans);
        qr_value = findViewById(R.id.U1_qr_value);
        qr_answer = findViewById(R.id.U1_qr_answer);
        qr_points = findViewById(R.id.U1_qr_points);
        send_data_btn = findViewById(R.id.U1_addbtn);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        send_data_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = FirebaseDatabase.getInstance();
                auth = FirebaseAuth.getInstance();
//                FirebaseUser user = auth.getCurrentUser();


                qrdata data = new qrdata(qr_number.getText().toString().trim(), qr_question.getText().toString().trim(), qr_value.getText().toString().trim(), Integer.parseInt(qr_scans.getText().toString()), qr_answer.getText().toString(), null, Integer.parseInt(qr_points.getText().toString()));

                try {
                    ref = db.getReference().child("QRcodes").child(qr_key_head.getText().toString().trim());
                    ref.setValue(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }
}