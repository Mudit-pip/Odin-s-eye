package com.odinseye;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class E5_inside_quest extends AppCompatActivity {

    TextView questtitle_txt, question_txt, points_txt;
    EditText answer_txt;
    Button subbmit_btn;

    FirebaseDatabase db;
    DatabaseReference ref;
    FirebaseAuth auth;
    String answer_not_to_be_displayed;
    ProgressDialog pg;
    String scans_qr, value_qr, points_qr;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e5_inside_quest);

        questtitle_txt = findViewById(R.id.E5_question_name);
        question_txt = findViewById(R.id.E5_question);
        answer_txt = findViewById(R.id.E5_answer);
        subbmit_btn = findViewById(R.id.E5_submit_btn);
        points_txt = findViewById(R.id.E5_point);

        pg = new ProgressDialog(E5_inside_quest.this);
        pg.setMessage("Loading");
        pg.show();
        pg.setCancelable(false);

        Intent intent = getIntent();
        String quename = intent.getStringExtra("quetitle");
        questtitle_txt.setText(quename.trim());

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        FirebaseUser user = auth.getCurrentUser();


        ref = db.getReference().child("QRcodes");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot mydata : snapshot.getChildren()) {
                    qrdata um = mydata.getValue(qrdata.class);
                    if (um != null && um.getNumber().trim().equals(quename.trim())) {
                        answer_not_to_be_displayed = um.getAnswer().toLowerCase();
                        question_txt.setText(um.getQuestion());
                        points_txt.setText(um.getPoints() + "");
                        scans_qr = um.getScans()+"";
                        value_qr = um.getValue();
                        points_qr = um.getPoints()+"";
                    }
                }

                pg.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        subbmit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(answer_txt.getText().toString().trim().isEmpty()){
                    AlertDialog.Builder ab = new AlertDialog.Builder(E5_inside_quest.this);
                    String faceWithRollingEyes = "\uD83D\uDE44";
                    ab.setMessage("Answer is Empty"+faceWithRollingEyes);
                    ab.setPositiveButton("Retry", null);
                    ab.setCancelable(false);
                    ab.create().show();

                } else {
                    if(answer_txt.getText().toString().toLowerCase().trim().equals(answer_not_to_be_displayed)){
                        Calendar cal = Calendar.getInstance();

                        add_data_to_firbase(cal.getTime().toString(), quename, Integer.parseInt(scans_qr), value_qr, Integer.parseInt(points_qr));
                        startActivity(new Intent(E5_inside_quest.this, E1_afterlogin.class));
                        finish();
                        //update firebase that answer fount is correct and increase points of user
                    } else {
                        AlertDialog.Builder ab = new AlertDialog.Builder(E5_inside_quest.this);
                        String pensiveFace = "\uD83D\uDE14";
                        ab.setMessage("Answer is Incorrect"+pensiveFace);
                        ab.setPositiveButton("Retry", null);
                        ab.setCancelable(false);
                        ab.create().show();
                    }
                }
            }
        });


    }

    public void add_data_to_firbase(String time, String number,int scans, String value, int points) {
        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();


        qrdata data = new qrdata(number, null, value, scans, null, time, points);
        if (user != null) {
            try {
                ref = db.getReference().child("Users").child(sanitizeEmail(user.getEmail())).child("Solved").child(number);
                ref.setValue(data);

                Toast.makeText(E5_inside_quest.this, "Solved " + number, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private String sanitizeEmail(String email) {
        return email != null ? email.replace(".", "_dot_").replace("@", "_at_") : "error";
    }
}