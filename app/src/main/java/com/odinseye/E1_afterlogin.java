package com.odinseye;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class E1_afterlogin extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    FirebaseAuth auth;

    FirebaseDatabase db;
    DatabaseReference ref;
    CircleImageView profilepic;

    ImageView scanner_img;
    ListView list;
    ArrayList<String> quest_no, solvedque_no;
    ProgressDialog pg;
    TextView point_txt;
    my_adapter myadapter;

    String[] quest_no_stringarray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e1_afterlogin);

        profilepic = findViewById(R.id.E1_profilepic);
        scanner_img = findViewById(R.id.E1_qrbtn);
        list = findViewById(R.id.E1_list);
        point_txt = findViewById(R.id.E1_point);

        pg = new ProgressDialog(E1_afterlogin.this);
        pg.setMessage("Loading");
        pg.show();
        pg.setCancelable(false);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(E1_afterlogin.this, gso);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(E1_afterlogin.this);
        auth = FirebaseAuth.getInstance();

        db = FirebaseDatabase.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        ref = db.getReference().child("Users").child(sanitizeEmail(user.getEmail())).child("Unlocked");


        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            Uri uri = acct.getPhotoUrl();
            Picasso.get().load(uri).into(profilepic);
        }

        quest_no = new ArrayList<>();
        solvedque_no = new ArrayList<>();

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(E1_afterlogin.this, profilepic);
                popupMenu.getMenuInflater().inflate(R.menu.menu_profileclick, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.details) {
                            Intent inte = new Intent(E1_afterlogin.this, E2_AccountDetails.class);
                            inte.putExtra("uri", acct.getPhotoUrl().toString());
                            inte.putExtra("name", acct.getDisplayName());
                            inte.putExtra("email", acct.getEmail());
                            startActivity(inte);
                        } else if (item.getItemId() == R.id.signout) {
                            signOut();
                        }

                        return true;
                    }
                });

                popupMenu.show();
            }
        });


        scanner_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(E1_afterlogin.this, E3_qrcode.class));
                finish();
            }
        });

        fetchdata();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pg.show();
                // check if the quest is already solved
                FirebaseUser user = auth.getCurrentUser();

                ref = db.getReference().child("Users").child(sanitizeEmail(user.getEmail())).child("Solved").child(quest_no.get(position));
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(E1_afterlogin.this, quest_no.get(position) + " is Already Solved", Toast.LENGTH_SHORT).show();
                            pg.dismiss();
                        } else {
                            Intent intent = new Intent(E1_afterlogin.this, E5_inside_quest.class);
                            intent.putExtra("quetitle", quest_no.get(position));
                            startActivity(intent);
                            pg.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    private void fetchdata() {
        FirebaseUser user = auth.getCurrentUser();

        //fetching question number
        ref = db.getReference().child("Users").child(sanitizeEmail(user.getEmail())).child("Unlocked");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quest_no.clear();

                for (DataSnapshot mydata : snapshot.getChildren()) {
                    qrdata um = mydata.getValue(qrdata.class);
                    if (um != null) {
                        quest_no.add(um.getNumber());
                    }
                }

                ///////////////////////////////////////////////////////
                //fetching points
                ref = db.getReference().child("Users").child(sanitizeEmail(user.getEmail())).child("Solved");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int point = 0;
                        solvedque_no.clear();

                        for (DataSnapshot mydata : snapshot.getChildren()) {
                            qrdata um = mydata.getValue(qrdata.class);
                            if (um != null) {
                                point = point + um.getPoints();
                                solvedque_no.add(um.getNumber());
                            }
                        }

                        point_txt.setText(point + "");

                        myadapter = new my_adapter(E1_afterlogin.this, quest_no.toArray(new String[quest_no.size()]), solvedque_no.toArray(new String[solvedque_no.size()]));
                        list.setAdapter(myadapter);

                        pg.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                ///////////////////////////////////////////////////////


////                myadapter = new my_adapter(getContext(), task_arraylist.toArray(new String[task_arraylist.size()]), time_arraylist.toArray(new String[time_arraylist.size()]));
//                ArrayAdapter<String> myadapter = new ArrayAdapter<>(E1_afterlogin.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, quest_no.toArray(new String[quest_no.size()]));
//                list.setAdapter(myadapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public class my_adapter extends ArrayAdapter<String> {
        String[] mtext;
        String[] mcheckbox;
        Context mContext;

        public my_adapter(Context context, String[] text, String[] checkbox) {
            super(context, R.layout.list_item_for_e1, R.id.list_item_e1_text, text);

            this.mtext = text;
            this.mcheckbox = checkbox;
            mContext = context;

        }


        @SuppressLint("ResourceAsColor")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            VHolder vholder;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.list_item_for_e1, parent, false);

                vholder = new VHolder(row);
                row.setTag(vholder);
            } else {
                vholder = (VHolder) row.getTag();
            }


            vholder.text_vh.setText(mtext[position]);

            if(Arrays.asList(mcheckbox).contains(mtext[position])){
                vholder.checkbox_imgvh.setImageResource(R.drawable.checkedcheckbox);
            }



            return row;
        }
    }

    public class VHolder {
        TextView text_vh;
        ImageView checkbox_imgvh;

        public VHolder(View r) {

            text_vh = r.findViewById(R.id.list_item_e1_text);
            checkbox_imgvh = r.findViewById(R.id.list_item_e1_checkbox_img);
        }

    }


    void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                startActivity(new Intent(E1_afterlogin.this, MainActivity.class));
                finish();
            }
        });
    }

    private String sanitizeEmail(String email) {
        return email != null ? email.replace(".", "_dot_").replace("@", "_at_") : "error";
    }
}