package com.utsman.kucingapes.sejarahindonesiatoday;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FavoritActivity extends AppCompatActivity {

    @BindView(R.id.back) ImageView backBtn;
    @BindView(R.id.title_toolbar) TextView titleBar;
    @BindView(R.id.empity) TextView tvEmpity;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;


    FirebaseAuth mAuth;
    FirebaseUser user;
    String dataFav;
    private ProgressDialog mProgressDialog;

    private FirebaseRecyclerAdapter<Getter, FavoritActivity.ItemViewHolder> recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorit);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Tajawal-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        ButterKnife.bind(this);
        configActionBar();
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setLightMode(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
        mDatabase.keepSynced(true);

        //RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Query query = mDatabase.orderByKey();

        FirebaseRecyclerOptions optionsItem = new FirebaseRecyclerOptions.Builder<Getter>()
                .setQuery(query, Getter.class).build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Getter, ItemViewHolder>(optionsItem) {
            @Override
            protected void onBindViewHolder(@NonNull final ItemViewHolder holder, int position, @NonNull final Getter model) {
                holder.setTitle(model.getTitle());
                holder.setDate(model.getDate());
                holder.setImg(getBaseContext(), model.getImg());

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String img = model.getImg();
                        final String title = model.getTitle();
                        final String date = model.getDate();
                        final String body = model.getBody();
                        Intent intent = new Intent(getApplicationContext(), FavResult.class);
                        intent.putExtra("img", img);
                        intent.putExtra("title", title);
                        intent.putExtra("date", date);
                        intent.putExtra("body", body);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_fav, parent, false);
                return new FavoritActivity.ItemViewHolder(view);
            }
        };

        recyclerView.setAdapter(recyclerAdapter);

        showProgressDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
                DatabaseReference favDatabase = FirebaseDatabase.getInstance().getReference().child("favDat");
                favDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataFav = dataSnapshot.child(user.getUid()).child("kosong").getValue(String.class);

                        if (dataFav != null) {
                            if (dataFav.equals("gak")){
                                tvEmpity.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }, 1000);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void configActionBar() {
        backBtn.setImageResource(R.drawable.back_black);
        titleBar.setTextColor(getResources().getColor(R.color.dark));
        titleBar.setText("Poster Favorit");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        recyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        recyclerAdapter.stopListening();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;

        public ItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setTitle(String title){
            TextView tvJudul = view.findViewById(R.id.re_judul);
            tvJudul.setText(title);
        }

        public void setDate (String date){
            TextView tvDate = view.findViewById(R.id.re_date);
            tvDate.setText(date);
        }

        public void setImg(Context ctx, String img){
            ImageView imgView = view.findViewById(R.id.re_img);
            Glide.with(ctx).load(img).into(imgView);
        }
    }
}
