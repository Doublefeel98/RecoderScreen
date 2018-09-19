package com.uit.thaithang.recoderscreen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;



public class MainActivity extends AppCompatActivity {
    //view
    private LinearLayout rootLayout;
    private ToggleButton btnRec;
    private Button btnPause;
    private Button btnStop;

    private static final int REQUEST_PERMISSION = 1001;
    private static final int REQUEST_CODE = 1000;


    private boolean isRec = false;

    VideoRecoderHelper videoRecoderHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //view
        btnRec = (ToggleButton) findViewById(R.id.btnRec);
        btnPause = (Button)findViewById(R.id.btnPause);
        btnStop = (Button)findViewById(R.id.btnStop);
        rootLayout = findViewById(R.id.rootLayout);

        btnPause.setEnabled(false);
        btnStop.setEnabled(false);

        videoRecoderHelper = new VideoRecoderHelper(this);

        //event
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        + ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)
//                        != PackageManager.PERMISSION_GRANTED)
//                {
//                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                            ||
//                            ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.RECORD_AUDIO))
//                    {
//                        btnRec.setChecked(false);
//                        Snackbar.make(rootLayout, "Permission",Snackbar.LENGTH_INDEFINITE)
//                                .setAction("ENABLE", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        ActivityCompat.requestPermissions(MainActivity.this,
//                                                new String[]{
//                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                                        Manifest.permission.RECORD_AUDIO
//                                                },REQUEST_PERMISSION);
//                                    }
//                                }).show();
//                    }
//                    else
//                    {
//                        ActivityCompat.requestPermissions(MainActivity.this,
//                                new String[]{
//                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                        Manifest.permission.RECORD_AUDIO
//                                },REQUEST_PERMISSION);
//                    }
//                }
//                else{
//
//                }
                toogleScreenShare(view);
            }

        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRec.setEnabled(true);
                btnPause.setEnabled(false);

                //VideoRecoderHelper.getInstance(getApplicationContext(),MainActivity.this).pause();
                videoRecoderHelper.pause();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRec = false;


                btnRec.setEnabled(true);
                btnRec.setChecked(false);

                btnPause.setEnabled(false);
                btnStop.setEnabled(false);

                //VideoRecoderHelper.getInstance(getApplicationContext(),MainActivity.this).stop();
                videoRecoderHelper.stop(true);

            }
        });
    }

    private void toogleScreenShare(View view) {
        if(((ToggleButton)view).isChecked()){
            view.setEnabled(false);
            btnStop.setEnabled(true);
            btnPause.setEnabled(true);
            isRec = true;

            //VideoRecoderHelper.getInstance(getApplicationContext(),this).start();
            videoRecoderHelper.start(true);
        }
        else
        {
            if (isRec)
            {
                view.setEnabled(false);
                btnStop.setEnabled(true);
                btnPause.setEnabled(true);

                //VideoRecoderHelper.getInstance(getApplicationContext(),this).resume();
                videoRecoderHelper.resume();
            }
//            mediaRecorder.stop();
//            mediaRecorder.reset();
//            stopRecordScreen();

            //Play in Video View
//            videoView.setVisibility(View.VISIBLE);
//            videoView.setVideoURI(Uri.parse(videoUri));
//            videoView.start();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != REQUEST_CODE)
        {
            Toast.makeText(this,"Unk Error",Toast.LENGTH_SHORT).show();
            return;
        }

        if (resultCode != RESULT_OK)
        {
            Toast.makeText(this,"Permission dendied",Toast.LENGTH_SHORT).show();
            return;
        }
        videoRecoderHelper.createScreen(resultCode,data);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
//            case REQUEST_PERMISSION:
//            {
//                if((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED))
//                {
//                    toogleScreenShare(btnRec);
//                }
//                else
//                {
//                    btnRec.setChecked(false);
//                    Snackbar.make(rootLayout, "Permission",Snackbar.LENGTH_INDEFINITE)
//                            .setAction("ENBLE", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    ActivityCompat.requestPermissions(MainActivity.this,
//                                            new String[]{
//                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                                    Manifest.permission.RECORD_AUDIO
//                                            },REQUEST_PERMISSION);
//                                }
//                            }).show();
//                }
//                return;
//            }
            case REQUEST_PERMISSION:
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] + grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED)
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
