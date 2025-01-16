package com.odinseyenavigation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient mgoogleSignInClient;
    ProgressDialog pg;
    FirebaseDatabase db;
    DatabaseReference ref;
    GoogleSignInClient gsc;
    ConstraintLayout loginbtn_conslay;
    LinearLayout astroinsta_linearlay;
    ImageView plinth_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginbtn_conslay = findViewById(R.id.main_activity_loginbtn_constraintlayout);
        astroinsta_linearlay = findViewById(R.id.main_astro_insta);
        plinth_img = findViewById(R.id.main_plinth);


        astroinsta_linearlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.instagram.com/astronomylnmiit/?hl=en");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        plinth_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://plinth.co.in/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


        pg = new ProgressDialog(MainActivity.this);
        pg.setMessage("Loading...");

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mgoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        gsc = GoogleSignIn.getClient(MainActivity.this, gso);


        loginbtn_conslay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signin();
            }
        });

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            add_data_to_firbase();
            pg.dismiss();
            Intent intent = new Intent(MainActivity.this, E1_afterLogin.class);
            startActivity(intent);
            finish();
        }

    }

    public void add_data_to_firbase() {
        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            try {
                ref = db.getReference().child("allowed").child(sanitizeEmail(user.getEmail()));
                ref.setValue("1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    int RC_SIGN_IN = 40;

    public void signin() {
        Intent intent = mgoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (pg.isShowing()) {
                    pg.dismiss();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pg.show();
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String email = sanitizeEmail(account.getEmail());
                db = FirebaseDatabase.getInstance();
                auth = FirebaseAuth.getInstance();

                ref = db.getReference().child("allowed").child(email);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String value = snapshot.getValue(String.class);
                            if (Integer.parseInt(value) == 0) {
                                firebaseauth(account.getIdToken());
                            } else {
                                Toast.makeText(MainActivity.this, "Already loged in some other device", Toast.LENGTH_SHORT).show();
                                Toast.makeText(MainActivity.this, "if you think this is a mistake contact Coordinators", Toast.LENGTH_SHORT).show();
                                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                                if (acct != null) {
                                    signOut();
                                } else {
                                    if (pg.isShowing()) {
                                        pg.dismiss();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "You are not registered go to Registration-desk", Toast.LENGTH_SHORT).show();
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                            if (acct != null) {
                                signOut();
                            } else {
                                if (pg.isShowing()) {
                                    pg.dismiss();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ApiException e) {
                pg.dismiss();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void firebaseauth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();

                    users us = new users();
                    us.setName(user.getDisplayName());
                    us.setUserid(user.getUid());
                    us.setProfile(user.getPhotoUrl().toString());


                    DatabaseReference refrence = FirebaseDatabase.getInstance().getReference().child("Users").child(sanitizeEmail(user.getEmail()));
                    refrence.child(user.getUid()).setValue(us).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                add_data_to_firbase();
                                Toast.makeText(MainActivity.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();

                                pg.dismiss();
                                Intent intent = new Intent(MainActivity.this, E1_afterLogin.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String sanitizeEmail(String email) {
        return email != null ? email.replace(".", "_dot_").replace("@", "_at_") : "error";
    }

}