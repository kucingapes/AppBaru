package com.utsman.kucingapes.sejarahindonesiatoday;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class MainApp extends AppCompatActivity {

    private ImageView imgView, btnFav;
    private TextView tvTitle, tvBody, tvDate;

    private String imgUrl, title, body, date, fav, id;

    FirebaseUser user;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        bindView();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("data");
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imgUrl = dataSnapshot.child("img").getValue(String.class);
                title = dataSnapshot.child("title").getValue(String.class);
                body = dataSnapshot.child("body").getValue(String.class);
                date = dataSnapshot.child("date").getValue(String.class);

                Glide.with(MainApp.this)
                        .load(imgUrl)
                        .into(imgView);
                tvTitle.setText(title);
                tvBody.setText(body);
                tvDate.setText(date);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference favDatabase = FirebaseDatabase.getInstance().getReference().child("favDat");
        //id = favDatabase.push().getKey();
        favDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fav = dataSnapshot.child(user.getUid()).child("fav").getValue(String.class);

               // Toast.makeText(getApplicationContext(), fav, Toast.LENGTH_SHORT).show();

                //assert fav != null;
                if (fav != null) {
                    if (fav.equals("iye")){
                        btnFav.setImageResource(R.drawable.star_fill);
                        btnFav.setEnabled(false);
                    } else if (fav.equals("nggak")){
                        btnFav.setImageResource(R.drawable.star);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.keepSynced(true);


        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btnFav.setImageDrawable(getResources().getDrawable(R.drawable.star_fill));
                HashMap<String, Object> dataMap = new HashMap<>();
                dataMap.put("img", imgUrl);
                dataMap.put("title", title);
                dataMap.put("body", body);
                dataMap.put("date", date);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("user");
                myRef.child(user.getUid()).push().setValue(dataMap);

                btnFav.setEnabled(false);

                HashMap<String, Object> favData = new HashMap<>();
                favData.put("fav", "iye");

                DatabaseReference favRef = database.getReference("favDat");
                favRef.child(user.getUid()).setValue(favData);

                //myRef.child(user.getUid()).updateChildren(dataMap);
            }
        });
    }

    private void bindView() {
        imgView = findViewById(R.id.img_view);
        tvTitle = findViewById(R.id.tv_judul);
        tvBody = findViewById(R.id.materi_body);
        tvDate = findViewById(R.id.tv_date);
        btnFav = findViewById(R.id.btn_fav);
    }

    public void pav(View view) {
        startActivity(new Intent(this, FavoritActivity.class));
    }
}
