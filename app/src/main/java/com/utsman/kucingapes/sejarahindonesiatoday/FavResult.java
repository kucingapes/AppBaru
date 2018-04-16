package com.utsman.kucingapes.sejarahindonesiatoday;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class FavResult extends AppCompatActivity {

    private ImageView imgView;
    private TextView tvTitle, tvBody, tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_result);
        bindView();

        Bundle bundle = getIntent().getExtras();
        String img = bundle.getString("img");
        String title = bundle.getString("title");
        String date = bundle.getString("date");
        String body = bundle.getString("body");

        Glide.with(this)
                .load(img)
                .into(imgView);

        tvTitle.setText(title);
        tvDate.setText(date);
        tvBody.setText(body);
    }

    private void bindView() {
        imgView = findViewById(R.id.img_view);
        tvTitle = findViewById(R.id.tv_judul);
        tvBody = findViewById(R.id.materi_body);
        tvDate = findViewById(R.id.tv_date);
    }
}
