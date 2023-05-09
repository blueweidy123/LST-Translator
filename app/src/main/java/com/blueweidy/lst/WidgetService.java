package com.blueweidy.lst;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;


import java.util.Calendar;

public class WidgetService extends Service {

    int LAYOUT_FLAG;
    View mFloatingView;
    View result_O;
    WindowManager windowManager;
    ImageView imgClose;
    ImageView output_img;
    TextView lstAction;
    float w, h;

    MediaProjection mediaProjection;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.o_widget_layout, null);
        result_O = LayoutInflater.from(this).inflate(R.layout.overlay_output, null);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.x = 0;
        layoutParams.y = 100;

        WindowManager.LayoutParams imageParam = new WindowManager.LayoutParams(140, 140, LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        imageParam.gravity = Gravity.BOTTOM | Gravity.CENTER;
        imageParam.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        imgClose = new ImageView(this);
        imgClose.setImageResource(R.drawable.baseline_cancel_24);
        imgClose.setVisibility(View.INVISIBLE);
        windowManager.addView(imgClose, imageParam);
//        windowManager.addView(result_O, layoutParams);
        windowManager.addView(mFloatingView, layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);

        h = windowManager.getDefaultDisplay().getHeight();
        w = windowManager.getDefaultDisplay().getWidth();

        lstAction = (TextView) mFloatingView.findViewById(R.id.lst_action);
        lstAction.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float initialTouchX, initialTouchY;
            long startClickTime;
            int MAX_CLICK_DURATION = 500;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        imgClose.setVisibility(View.VISIBLE);

//                        initialX = layoutParams.x;
                        initialY = layoutParams.y;

                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        imgClose.setVisibility(View.GONE);

                        if (clickDuration < MAX_CLICK_DURATION) {
                            Toast.makeText(WidgetService.this, "lst ", Toast.LENGTH_SHORT).show();

                        } else {
                            if (layoutParams.y > (h * 0.6)) {
                                stopSelf();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
//                        layoutParams.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                        layoutParams.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);

                        windowManager.updateViewLayout(mFloatingView, layoutParams);
                        if (layoutParams.y > (h * 0.6)) {
                            imgClose.setImageResource(R.drawable.baseline_cancel_24);
                        } else {
                            imgClose.setImageResource(R.drawable.baseline_cancel_24);
                        }
                        return true;
                }
                return false;
            }
        });


        return START_STICKY;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            windowManager.removeView(mFloatingView);
        }
        if (imgClose != null) {
            windowManager.removeView(imgClose);
        }
    }
}
