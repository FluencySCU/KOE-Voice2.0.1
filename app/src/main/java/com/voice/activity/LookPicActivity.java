package com.voice.activity;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.voice.R;

public class LookPicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_pic);
        Bundle bundle=getIntent().getExtras();
        Bitmap bitmap= (Bitmap) bundle.get("img");
        ImageView img= (ImageView) findViewById(R.id.lookPic);
        img.setImageBitmap(bitmap);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
