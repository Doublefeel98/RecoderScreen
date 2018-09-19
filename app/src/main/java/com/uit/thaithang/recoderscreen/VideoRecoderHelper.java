package com.uit.thaithang.recoderscreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class VideoRecoderHelper {

    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCallback mediaProjectionCallback;
    private MediaRecorder mediaRecorder;

    private int mSrceenDensity;

    private static int DISPLAY_WIDTH = 720;
    private static int DISPLAY_HEIGHT = 1280;
    private static int FPS = 30;

    private String videoUri = "";
    private String directory = String.valueOf(new File(Environment.DIRECTORY_DOWNLOADS));
    private final String defaultName = "/ScreenRecord_";
    private String name ="";
    
    Activity activity;

    private boolean isRec = false;
    private boolean recordAudio = true;

    ArrayList<String> sourceFiles = new ArrayList<>();

    private static VideoRecoderHelper instance;

    public static VideoRecoderHelper getInstance(Activity activity)
    {
        if (instance == null) instance = new VideoRecoderHelper(activity);
        return VideoRecoderHelper.instance;
    }

    static {
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private final int[][] validResolutions = {
            // CEA Resolutions
            {640, 480},
            {720, 480},
            {720, 576},
            {1280, 720},
            {1920, 1080},
            // VESA Resolutions
            {800, 600},
            {1024, 768},
            {1152, 864},
            {1280, 768},
            {1280, 800},
            {1360, 768},
            {1366, 768},
            {1280, 1024},
            //{ 1400, 1050 },
            //{ 1440, 900 },
            //{ 1600, 900 },
            {1600, 1200},
            //{ 1680, 1024 },
            //{ 1680, 1050 },
            {1920, 1200},
            // HH Resolutions
            {800, 480},
            {854, 480},
            {864, 480},
            {640, 360},
            //{ 960, 540 },
            {848, 480}
    };

    public VideoRecoderHelper(Activity activity)
    {
        if (activity!=null)
            this.activity = activity;

        //checkPermision();
        checkAndRequestPermissions();

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mSrceenDensity = metrics.densityDpi;

        //Get Srceen
        DISPLAY_HEIGHT = metrics.heightPixels;
        DISPLAY_WIDTH = metrics.widthPixels;


        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager)activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

//    public VideoRecoderHelper(Activity activity,int resolution, int fps, boolean recordAudio, String directory)
//    {
//        if (activity!=null)
//            this.activity = activity;
//
//
//        this.FPS = fps;
//        this.recordAudio = recordAudio;
//        this.directory = directory;
//
//
//        //checkPermision();
//        checkAndRequestPermissions();
//
//        DisplayMetrics metrics = new DisplayMetrics();
//        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        mSrceenDensity = metrics.densityDpi;
//
//        //Get Srceen
//        DISPLAY_HEIGHT = metrics.heightPixels;
//        DISPLAY_WIDTH = metrics.widthPixels;
//
//        int max = Math.max(maxWidth, maxHeight);
//        int min = Math.min(maxWidth, maxHeight);
//        int resConstraint = context.getResources().getInteger(
//                R.integer.config_maxDimension);
//
//        double ratio;
//        boolean landscape = false;
//        boolean resizeNeeded = false;
//
//        if (DISPLAY_WIDTH > DISPLAY_HEIGHT) {
//            // landscape
//            landscape = true;
//            ratio = (double) DISPLAY_WIDTH / (double) DISPLAY_HEIGHT;
//            if (resConstraint >= 0 && DISPLAY_HEIGHT > resConstraint) {
//                min = resConstraint;
//            }
//            if (DISPLAY_WIDTH > max || DISPLAY_HEIGHT > min) {
//                resizeNeeded = true;
//            }
//        } else {
//            // portrait
//            ratio = (double) DISPLAY_HEIGHT / (double) DISPLAY_WIDTH;
//            if (resConstraint >= 0 && DISPLAY_WIDTH > resConstraint) {
//                min = resConstraint;
//            }
//            if (DISPLAY_HEIGHT > max || DISPLAY_WIDTH > min) {
//                resizeNeeded = true;
//            }
//        }
//        // see if we need to resize
//
//        // Figure orientation and ratio first
//        if (DISPLAY_WIDTH > DISPLAY_HEIGHT) {
//            // landscape
//            landscape = true;
//            ratio = (double) DISPLAY_WIDTH / (double) DISPLAY_HEIGHT;
//            if (resConstraint >= 0 && DISPLAY_HEIGHT > resConstraint) {
//                min = resConstraint;
//            }
//            if (DISPLAY_WIDTH > max || DISPLAY_HEIGHT > min) {
//                resizeNeeded = true;
//            }
//        } else {
//            // portrait
//            ratio = (double) DISPLAY_HEIGHT / (double) DISPLAY_WIDTH;
//            if (resConstraint >= 0 && DISPLAY_WIDTH > resConstraint) {
//                min = resConstraint;
//            }
//            if (DISPLAY_HEIGHT > max || DISPLAY_WIDTH > min) {
//                resizeNeeded = true;
//            }
//        }
//
//        if (resizeNeeded) {
//            boolean matched = false;
//            for (int[] resolution : validResolutions) {
//                // All res are in landscape. Find the highest match
//                if (resolution[0] <= max && resolution[1] <= min &&
//                        (!matched || (resolution[0] > (landscape ? DISPLAY_WIDTH : DISPLAY_HEIGHT)))) {
//                    if (((double) resolution[0] / (double) resolution[1]) == ratio) {
//                        // Got a valid one
//                        if (landscape) {
//                            DISPLAY_WIDTH = resolution[0];
//                            DISPLAY_HEIGHT = resolution[1];
//                        } else {
//                            DISPLAY_HEIGHT = resolution[0];
//                            DISPLAY_WIDTH = resolution[1];
//                        }
//                        matched = true;
//                    }
//                }
//            }
//            if (!matched) {
//                // No match found. Go for the lowest... :(
//                DISPLAY_WIDTH = landscape ? 640 : 480;
//                DISPLAY_HEIGHT = landscape ? 480 : 640;
//            }
//
//        mediaRecorder = new MediaRecorder();
//        mediaProjectionManager = (MediaProjectionManager)activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//    }

    public void start(boolean isClick){
        if (!recordAudio) {
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMicrophoneMute(true);
        }
        initRecorder();
        recorderSrceen();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            sourceFiles.add(videoUri);
        }
        if(isClick)
            isRec = true;
    }

    public void stop(boolean isClick){
        mediaRecorder.stop();
        mediaRecorder.reset();
        stopRecordScreen();

        if(isClick) {
            isRec = false;
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMicrophoneMute(false);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {

                videoUri = Environment.getExternalStoragePublicDirectory(directory)
                        + new StringBuilder(defaultName).append(new SimpleDateFormat("dd-MM-yyyy-hh_mm_ss")
                        .format(new Date())).append(".mp4").toString();

                Boolean result = mergeMediaFiles(false, sourceFiles, videoUri);

                if (result)
                    Log.d("stop", "success");
                else
                    Log.d("stop", "fail");

                for(int i = 0; i< sourceFiles.size();i++)
                    deleteFiles(sourceFiles.get(i));

                sourceFiles.clear();
            }
        }

    }

    public void resume(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.resume();
        }
        else
        {
            this.start(false);
        }
    }

    public void pause(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.pause();
        }
        else
        {
            this.stop(false);
        }
    }

    private void recorderSrceen() {
        if(mediaProjection == null)
        {
            activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtuaDisplay();
        mediaRecorder.start();
    }

    public void createScreen(int resultCode, @Nullable Intent data)
    {
        mediaProjectionCallback = new MediaProjectionCallback();
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode,data);
        mediaProjection.registerCallback(mediaProjectionCallback,null);
        virtualDisplay = createVirtuaDisplay();
        mediaRecorder.start();
    }

    private VirtualDisplay createVirtuaDisplay() {
        return mediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH,DISPLAY_HEIGHT,mSrceenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(),null,null);
    }

    private void initRecorder() {
        try{
            if(!isExternalStorageReadable())
                return;
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            if(name.equals("")) {
                videoUri = Environment.getExternalStoragePublicDirectory(directory)
                        + new StringBuilder(defaultName).append(new SimpleDateFormat("dd-MM-yyyy-hh_mm_ss")
                        .format(new Date())).append(".mp4").toString();
            }
            else
            {
                videoUri = Environment.getExternalStoragePublicDirectory(directory)
                        + new StringBuilder(name).append(".mp4").toString();
            }

            mediaRecorder.setOutputFile(videoUri);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH,DISPLAY_HEIGHT);
            Log.d("initRecorder: ", "WIDTH"+DISPLAY_WIDTH);
            Log.d("initRecorder: ", "WIDTH"+DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512*1000);
            mediaRecorder.setVideoFrameRate(FPS);

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (isRec)
            {
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            mediaProjection = null;
            stopRecordScreen();
            super.onStop();
        }
    }

    private void stopRecordScreen() {
        if(virtualDisplay == null){
            return;
        }
        virtualDisplay.release();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if(mediaProjection != null)
        {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_CODE);
        }
    }

    private boolean mergeMediaFiles(boolean isAudio, ArrayList<String> sourceFiles, String targetFile) {
        try {
            String mediaKey = isAudio ? "soun" : "vide";
            List<Movie> listMovies = new ArrayList<>();
            for (String filename : sourceFiles) {
                listMovies.add(MovieCreator.build(filename));
            }
            List<Track> listTracks = new LinkedList<>();
            for (Movie movie : listMovies) {
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals(mediaKey)) {
                        listTracks.add(track);
                    }
                }
            }
            Movie outputMovie = new Movie();
            if (!listTracks.isEmpty()) {
                outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
            }
            Container container = new DefaultMp4Builder().build(outputMovie);
            FileChannel fileChannel = new RandomAccessFile(String.format(targetFile), "rw").getChannel();
            container.writeContainer(fileChannel);
            fileChannel.close();
            return true;
        }
        catch (IOException e) {
            Log.e("mergeMediaFiles", "Error merging media files. exception: "+e.getMessage());
            return false;
        }
    }

    private void deleteFiles(String path) {
//        File file = new File(path);
//
//        if (file.exists()) {
//            String deleteCmd = "rm -r " + path;
//            Runtime runtime = Runtime.getRuntime();
//            try {
//                runtime.exec(deleteCmd);
//            } catch (IOException e) {
//
//            }
//        }

        try
        {
            File file = new File(path);
            if(file.exists())
                file.delete();
        }
        catch (Exception e)
        {
            Log.e("App", "Exception while deleting file " + e.getMessage());
        }

    }
}
