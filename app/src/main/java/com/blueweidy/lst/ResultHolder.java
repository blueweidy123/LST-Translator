package com.blueweidy.lst;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;

import java.io.FileDescriptor;
import java.io.IOException;

public class ResultHolder extends AppCompatActivity implements View.OnClickListener{

    TranslatorOptions options;
    Translator jap_to_En;

    private ImageView output, back, reload;
    Intent intent;

    private TextRecognizer textRecognizer;

    Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_holder);
        initTranslator();

        textRecognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());

        output = findViewById(R.id.output);

        intent=getIntent();
        Uri uri = (Uri) intent.getParcelableExtra("img");

        recogText(uri);

        output.setImageURI(uri);

        back = findViewById(R.id.backBttn);
        back.setOnClickListener(this::onClick);
        reload = findViewById(R.id.reloadBttn);
        reload.setOnClickListener(this::onClick);
        //not supported yet
        reload.setVisibility(View.GONE);
    }

    public void recogText(Uri inputImg){
        if(inputImg!=null){
            try {
                InputImage inputImage = InputImage.fromFilePath(ResultHolder.this, inputImg);
                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                try {
                                    // Load the bitmap from the URI
                                    Bitmap bitmap = getBitmapFromUri(inputImg);
                                    // Create a new bitmap with the same dimensions as the original
                                    Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                    // Draw the text on top of the bitmap
                                    Canvas canvas = new Canvas(mutableBitmap);
                                    Paint paint = new Paint();
                                    paint.setColor(Color.WHITE);
                                    paint.setTextSize(50);
                                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                                    for (Text.TextBlock block : text.getTextBlocks()) {
                                        for (Text.Line line : block.getLines()) {
                                            RectF rect = new RectF(line.getBoundingBox());
                                            canvas.drawRect(rect, paint);
                                            paint.setStyle(Paint.Style.FILL);
                                            canvas.drawText(line.getText(), line.getBoundingBox().left + line.getBoundingBox().width() / 2f,
                                                    line.getBoundingBox().top + line.getBoundingBox().height() / 2f, paint);
                                        }
                                    }
//                                    output.setImageBitmap(mutableBitmap);
                                    overlayTextOnImage(inputImg, text, output);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).addOnFailureListener(e -> Toast.makeText(ResultHolder.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Input not found!", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    public void initTranslator(){
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

    private void overlayTextOnImage(Uri imageUri, Text text, ImageView out) {
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri);
            // Create a mutable copy of the bitmap
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            // Create a canvas to draw onto the new bitmap
            Canvas canvas = new Canvas(mutableBitmap);
            // Draw the original bitmap onto the canvas
            canvas.drawBitmap(bitmap, 0, 0, null);
            // Set the paint settings for the rectangle
            Paint rectPaint = new Paint();
            rectPaint.setColor(Color.WHITE);
            rectPaint.setAlpha(200);
            rectPaint.setStyle(Paint.Style.FILL);
            rectPaint.setAntiAlias(true);
            // Draw a rounded rectangle over the old text
            for (Text.TextBlock textBlock : text.getTextBlocks()) {
                for (Text.Line line : textBlock.getLines()) {
                    for (Text.Element element : line.getElements()) {
                        // Calculate the bounding box for the text element
                        RectF boundingBox = new RectF(element.getBoundingBox());
                        // Add some padding to the bounding box
                        float padding =0;
//                        float padding = boundingBox.height() * 0.1f;
                        boundingBox.top -= padding;
                        boundingBox.left -= padding;
                        boundingBox.right += padding;
                        boundingBox.bottom += padding;
                        // Draw a rounded rectangle over the old text
                        float cornerRadius = boundingBox.height() * 0.1f;
                        canvas.drawRoundRect(boundingBox, cornerRadius, cornerRadius, rectPaint);
                    }
                }
            }
            // Set the paint settings for the text
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(200);
            textPaint.setTextAlign(Paint.Align.LEFT);
//            textPaint.setTextAlign(Paint.Align.CENTER);
            // Draw each recognized text block onto the canvas
            for (Text.TextBlock textBlock : text.getTextBlocks()) {
                for (Text.Line line : textBlock.getLines()) {
                    for (Text.Element element : line.getElements()) {
                        // Calculate the bounding box for the text element
                        RectF boundingBox = new RectF(element.getBoundingBox());
                        // Add some padding to the bounding box
                        float padding = 0;
//                        float padding = boundingBox.height() * 0.1f;
                        boundingBox.top -= padding;
                        boundingBox.left -= padding;
                        boundingBox.right += padding;
                        boundingBox.bottom += padding;
                        // Set the text size to fit inside the bounding box
                        float textSize = textPaint.getTextSize();
                        while (textPaint.measureText(element.getText()) > boundingBox.width()) {
                            textSize -= 1;
                            textPaint.setTextSize(textSize);
                        }
                        // Draw the new text onto the canvas
                        float x = boundingBox.left + 8;
                        float y = boundingBox.bottom - boundingBox.height() / 2 - ((textPaint.descent() + textPaint.ascent()) / 2);
                        jap_to_En.translate(element.getText())
                                .addOnSuccessListener(s -> {
                                        canvas.drawText(s, x, y, textPaint);
                                        output.setImageBitmap(mutableBitmap);
                                }
                                ).addOnFailureListener(e -> canvas.drawText(element.getText(), x, y, textPaint));
                    }
                }
            }
            out.setImageBitmap(mutableBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBttn:
                onBackPressed();
                break;
        }
    }
}