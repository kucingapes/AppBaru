package com.utsman.kucingapes.sejarahindonesiatoday;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;
import com.utsman.kucingapes.sejarahindonesiatoday.Lib.RoundedCornerLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class MainApp extends AppCompatActivity {


    @BindView(R.id.tv_judul) TextView tvTitle;
    @BindView(R.id.materi_body) TextView tvBody;
    @BindView(R.id.tv_date) TextView tvDate;
    @BindView(R.id.img_view) ImageView imgView;
    @BindView(R.id.img_bg) ImageView imgBg;
    @BindView(R.id.share) ImageView share;
    @BindView(R.id.back) ImageView backBtn;
    @BindView(R.id.btn_fav) ImageView btnFav;
    @BindView(R.id.menu) FloatingActionMenu fabMenu;
    @BindView(R.id.menu_fav) FloatingActionButton fabFav;
    @BindView(R.id.menu_about) FloatingActionButton fabAbout;
    @BindView(R.id.lay_container) RoundedCornerLayout shareLayout;
    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;

    Bitmap bitmap;

    private ProgressDialog mProgressDialog;
    private String imgUrl, title, body, date, fav;

    FirebaseUser user;
    FirebaseAuth mAuth;

    String dataFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Tajawal-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        StatusBarUtil.setTranslucentForImageView(this, (int) 5f, shareLayout);
        ButterKnife.bind(this);
        //bindView();
        backBtn.setVisibility(View.GONE);
        fabMenu.setClosedOnTouchOutside(true);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/.kucingapes/cache/");
        folder.mkdirs();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("data");
        mDatabase.keepSynced(true);
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

                Glide.with(MainApp.this)
                        .load(imgUrl)
                        .apply(bitmapTransform(new BlurTransformation(25, 3)))
                        .into(imgBg);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DatabaseReference favDatabase = FirebaseDatabase.getInstance().getReference().child("favDat");
        favDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fav = dataSnapshot.child(user.getUid()).child("fav").getValue(String.class);

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


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String lokasi = "mnt/sdcard/.kucingapes/cache/cacheimg.jpg";

                shareLayout.setDrawingCacheEnabled(true);
                shareLayout.buildDrawingCache();
                bitmap = shareLayout.getDrawingCache();

                Bitmap image = Bitmap.createBitmap(shareLayout.getWidth(),
                        shareLayout.getHeight(),
                        Bitmap.Config.RGB_565);

                shareLayout.draw(new Canvas(image));

                try {
                    image.compress(Bitmap.CompressFormat.JPEG, 95,
                            new FileOutputStream(lokasi));
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(
                            Environment.getExternalStorageDirectory().getPath()+"/.kucingapes/cache/cacheimg.jpg"))));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //String imageUriString="content://media/external/.kucingapes/cache/cacheimg.jpg";
                        hideProgressDialog();
                        Uri imgUri = Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/.kucingapes/cache/cacheimg.jpg");

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, title);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
                        shareIntent.setType("*/*");
                        startActivity(Intent.createChooser(shareIntent, "kirim"));
                    }
                }, 2000);
            }
        });

        buttonOnClik();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void buttonOnClik() {
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFav.setImageDrawable(getResources().getDrawable(R.drawable.star_fill));
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
                favData.put("kosong", "gak");

                DatabaseReference favRef = database.getReference("favDat");
                favRef.child(user.getUid()).setValue(favData);

                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Poster ditambahkan ke favorit", Snackbar.LENGTH_SHORT);
                snackbar.show();

            }
        });

        fabFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainApp.this, FavoritActivity.class);
                intent.putExtra("dataFav", dataFav);
                startActivity(intent);
                ///startActivity(new Intent(MainApp.this, FavoritActivity.class));
                fabMenu.close(true);
            }
        });

        fabAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainApp.this, Profile.class));
                fabMenu.close(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
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
}
