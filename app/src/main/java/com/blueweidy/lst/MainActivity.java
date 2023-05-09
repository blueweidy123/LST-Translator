package com.blueweidy.lst;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int NOTI_ID = 001;
    private Button start_bttn;
    private Button select_Bttn;
    private static final int REQUEST_CAMERA_CODE = 100;

    ImageView test;

    TranslatorOptions options;
    Translator jap_to_En;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        test = findViewById(R.id.test);

        start_bttn = findViewById(R.id.startBttn);
        start_bttn.setOnClickListener(this::onClick);
        select_Bttn = findViewById(R.id.selectBttn);
        select_Bttn.setOnClickListener(this::onClick);

        checkCameraPermission();
        getOverlayPermission();

        initTranslator();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }
    }

    public void getOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
        }
    }


    public void initTranslator() {
        options = new TranslatorOptions.Builder()
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .setSourceLanguage(TranslateLanguage.JAPANESE)
                .build();
        jap_to_En = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        jap_to_En.downloadModelIfNeeded(conditions);
        getLifecycle().addObserver(jap_to_En);
    }

    public void selectImage(View view) {
        ImagePicker.with(MainActivity.this)
                .crop()
                .start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
            if (data != null) {
                Uri imageURI = data.getData();
                Intent intent = new Intent(MainActivity.this, ResultHolder.class);
                intent.putExtra("img", imageURI);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "image not selected  ", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startBttn:
                if(!Settings.canDrawOverlays(MainActivity.this)){
                    getOverlayPermission();
                }else {
                    Intent intent = new Intent(MainActivity.this, WidgetService.class);
                    startService(intent);
                    finish();
                }
                break;
            case R.id.selectBttn:
                selectImage(view);
                break;
        }
    }
}