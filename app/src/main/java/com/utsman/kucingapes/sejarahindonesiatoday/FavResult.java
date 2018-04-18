package com.utsman.kucingapes.sejarahindonesiatoday;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jaeger.library.StatusBarUtil;
import com.utsman.kucingapes.sejarahindonesiatoday.Lib.RoundedCornerLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import jp.wasabeef.glide.transformations.BlurTransformation;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class FavResult extends AppCompatActivity {

    private ImageView imgView, share, btnFav, imgBg, backBtn;
    private TextView tvTitle, tvBody, tvDate, tvToolbar;
    private FloatingActionButton fabMenu;
    private Toolbar toolbar;

    ProgressDialog mProgressDialog;
    RoundedCornerLayout shareLayout;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fav_result);
        setContentView(R.layout.activity_main_app);
        StatusBarUtil.setTranslucentForImageView(this, (int) 5f, shareLayout);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Tajawal-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        bindView();

        btnFav.setVisibility(View.GONE);
        fabMenu.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        final String img = bundle.getString("img");
        final String title = bundle.getString("title");
        String date = bundle.getString("date");
        String body = bundle.getString("body");

        Glide.with(this)
                .load(img)
                .into(imgView);

        Glide.with(this)
                .load(img)
                .apply(bitmapTransform(new BlurTransformation(25, 3)))
                .into(imgBg);

        tvTitle.setText(title);
        tvDate.setText(date);
        tvBody.setText(body);
        tvToolbar.setText(date);

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

                //shareLayout.setDrawingCacheEnabled(true);
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

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

    private void bindView() {
        imgView = findViewById(R.id.img_view);
        tvTitle = findViewById(R.id.tv_judul);
        tvBody = findViewById(R.id.materi_body);
        tvDate = findViewById(R.id.tv_date);
        share = findViewById(R.id.share);
        btnFav = findViewById(R.id.btn_fav);
        shareLayout = findViewById(R.id.lay_container);
        imgBg = findViewById(R.id.img_bg);
        tvToolbar = findViewById(R.id.title_toolbar);
        fabMenu = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        backBtn = findViewById(R.id.back);
    }
}
