package activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.kirill.testexample.R;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.util.VLCUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.protocol.RequestExpectContinue;
import cz.msebera.android.httpclient.entity.StringEntity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import support.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String mToken, v1,v2, videoOutput= null;
    private final OkHttpClient client = new OkHttpClient();
    private ProgressBar progressBar;
    private MediaController mediaControllerOne, mediaControllerTwo;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private VideoView vVideoViewOne, vVideoViewTwo;
    private static final int REQUEST_ONE = 1888;
    private static final int REQUEST_TWO = 1889;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);

        SharedPreferences mSettings = getSharedPreferences(Constants.SETTING_NAME, Context.MODE_PRIVATE);//фрунзе 66 кабинет 102 книжка и копия страницы
        LocalDateTime dLastVisit = LocalDateTime.parse(mSettings.getString("Data_Last_Connect", String.valueOf(LocalDateTime.now().minusDays(1))));
        mToken = mSettings.getString("Token","");
        if (LocalDateTime.now().minusMinutes(Constants.mUpdateFrequency).compareTo(dLastVisit)>=0) {
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }else {
            int dStartingTimer =Constants.mUpdateFrequency + dLastVisit.getMinuteOfHour() - LocalDateTime.now().getMinuteOfHour();
            Log.d("TAP"," " + dStartingTimer);
            if (mTimer != null) {
                mTimer.cancel();
            }
            mTimer = new Timer();
            mMyTimerTask = new MyTimerTask();
            mTimer.schedule(mMyTimerTask, dStartingTimer * 60000, dStartingTimer * 60000);
        }
        ffmpeg();
        vVideoViewOne = (VideoView)findViewById(R.id.videoView_one);
        vVideoViewTwo = (VideoView)findViewById(R.id.videoView_two);
        mediaControllerOne = (MediaController)findViewById(R.id.mediaControllerOne);
        mediaControllerTwo = (MediaController)findViewById(R.id.mediaControllerTwo);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        Button buttonOpenStorageOne = (Button)findViewById(R.id.button_from_storage);
        Button buttonOpenStorageTwo = (Button)findViewById(R.id.button_from_storage_2);
        Button buttonOpenCameraOne = (Button)findViewById(R.id.button_open_camera);
        Button buttonOpenCameraTwo = (Button)findViewById(R.id.button_open_camera_2);
        Button buttonAssotiation = (Button)findViewById(R.id.button_assotiation);
        Button buttonSee = (Button)findViewById(R.id.button_see);
        Button buttonSend = (Button)findViewById(R.id.button_send);

        buttonOpenStorageOne.setOnClickListener(this);
        buttonOpenStorageTwo.setOnClickListener(this);
        buttonOpenCameraOne.setOnClickListener(this);
        buttonOpenCameraTwo.setOnClickListener(this);
        buttonAssotiation.setOnClickListener(this);
        buttonSee.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
        String s = getApplicationInfo().dataDir + "/" + String.valueOf(System.currentTimeMillis()) + ".mp4";
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_from_storage:
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_ONE);
                break;
            case R.id.button_from_storage_2:
                Intent intent2 = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent2, REQUEST_TWO);
                break;
            case R.id.button_open_camera:
                Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_ONE);
                break;
            case R.id.button_open_camera_2:
                Intent cameraIntent2 = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(cameraIntent2, REQUEST_TWO);
                break;
            case R.id.button_assotiation:
                if(v1==null|| v2==null){Toast.makeText(MainActivity.this," Выберите файл ",Toast.LENGTH_LONG).show();}
                else {
                    dellFile();
                    ffmpegOne();
                }
                break;
            case R.id.button_see:
                if(videoOutput==null){Toast.makeText(MainActivity.this,"Для просмотра файла, его нужно создать",Toast.LENGTH_LONG).show();}
                else {
                    Intent intents = new Intent(Intent.ACTION_VIEW);
                    intents.setDataAndType(Uri.parse(String.valueOf(videoOutput)), "video/*");
                    startActivity(intents);
                }
                break;
            case R.id.button_send:
                if(videoOutput==null){Toast.makeText(MainActivity.this,"Чтобы поделиться, нужно создать видео!",Toast.LENGTH_LONG).show();}
                else {
                    DownloadVideoServer();
                }
                break;
        }
    }
    @Override
    protected void onPostResume() {
        vVideoViewOne.seekTo(100);
        vVideoViewTwo.seekTo(100);
        super.onPostResume();
    }
    @Override
    protected void onPause() {
        vVideoViewOne.pause();
        vVideoViewTwo.pause();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) switch (requestCode) {

            case REQUEST_ONE:
                mediaControllerOne = new MediaController(this);
                vVideoViewOne.setMediaController(mediaControllerOne);
                vVideoViewOne.setVideoPath(String.valueOf(data.getData()));
                vVideoViewOne.requestFocus();
                vVideoViewOne.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Intent intent = new Intent(Intent.ACTION_VIEW );
                        intent.setDataAndType(Uri.parse(String.valueOf(data.getData())), "video/*");
                        startActivity(intent);
                        return false;
                    }
                });
                v1=data.getData().toString();
                break;
            case REQUEST_TWO:
                mediaControllerTwo = new MediaController(this);
                vVideoViewTwo.setMediaController(mediaControllerTwo);
                vVideoViewTwo.setVideoPath(String.valueOf(data.getData()));
                vVideoViewTwo.requestFocus();
                vVideoViewTwo.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Intent intent = new Intent(Intent.ACTION_VIEW );
                        intent.setDataAndType(Uri.parse(String.valueOf(data.getData())), "video/*");
                        startActivity(intent);
                        return false;
                    }
                });
                v2=data.getData().toString();
                break;
        }
    }

    public void DownloadVideoServer(){
        File file = new File(videoOutput);
            RequestBody  formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            RequestBody.create(MediaType.parse("video/mp4"), file))
                    .addFormDataPart("type_file", "VIDEO")
                    .build();
            Request request = new Request.Builder()
                    .header("Content-Type", "multipart/form-data")
                    .header("Authorization","Bearer {" + mToken + "}")
                    .url("https://api.smiber.com/v4.005/file/upload")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    Log.d("TAP", "ERROR " + e);
                    e.printStackTrace();}
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    Log.d("TAP"," " + res);
                    try{
                        JSONObject jsonObject = new JSONObject(res);
                        String data = jsonObject.getString("data");
                        String id  = jsonObject.getString("id");
                        AddContentServer(data, id);
                    }
                    catch (JSONException e){}
                }
            });
    }
    public void AddContentServer(String data, String id){
        RequestBody  formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("nameContent", "test_video10914994612345.mp4")
                .addFormDataPart("typeContent", "VIDEO")
                .addFormDataPart("params ", data)
                .addFormDataPart("video ", id)
                .build();
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer {" + mToken + "}")
                .url("https://api.smiber.com/v4.005/content/add")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.d("TAP", "ERROR " + e);
                e.printStackTrace();}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
            }
        });
    }

    public void ffmpeg(){
        final FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override public void onStart() {Log.d("FFMPEG","onStart 0");}
                @Override public void onFailure() {}
                @Override public void onSuccess() {Log.d("FFMPEG","onSuccess 0");}
                @Override public void onFinish() {Log.d("FFMPEG","onFinish 0");}
            });
        } catch (FFmpegNotSupportedException e) {
        }
    }
    public  void ffmpegOne() {
        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        try {
            String ss = getFileNameByUri(this, Uri.parse(v1));
            fFmpeg.execute(new String[]{"-i", ss, "-qscale:v", "1", getApplicationInfo().dataDir + "/0.mpg"}, new FFmpegExecuteResponseHandler() {
                @Override public void onSuccess(String message) {ffmpegTwo();}
                @Override public void onProgress(String message) {
                }
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG","onFailure " + message);
                    Toast.makeText(MainActivity.this,"Ошибка!",Toast.LENGTH_LONG).show();
                }
                @Override public void onStart() {progressBar.setVisibility(View.VISIBLE);}
                @Override public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.d("FFMPEG", " ERROR" + e.getMessage());
            e.printStackTrace();
        }
    }
    public  void ffmpegTwo() {
        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        try {
            String s = getFileNameByUri(this, Uri.parse(v2));
            fFmpeg.execute(new String[]{"-i", s, "-qscale:v", "1", getApplicationInfo().dataDir + "/1.mpg"}, new FFmpegExecuteResponseHandler() {
                @Override public void onSuccess(String message) {ffmpegThree();}
                @Override public void onProgress(String message) {}
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG2","onFailure " + message);
                    Toast.makeText(MainActivity.this,"Ошибка!",Toast.LENGTH_LONG).show();
                }
                @Override public void onStart() { Log.d("FFMPEG2","onStart ");}
                @Override public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
    public  void ffmpegThree() {
        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        try {
            fFmpeg.execute(new String[]{"-i", "concat:" + getApplicationInfo().dataDir + "/0.mpg|" + getApplicationInfo().dataDir + "/1.mpg", "-c", "copy", getApplicationInfo().dataDir + "/2.mpg"}, new FFmpegExecuteResponseHandler() {
                //3 fFmpeg.execute(new String[]{"-i", "/storage/emulated/0/all.mpg", "-qscale:v", "2", "/storage/emulated/0/output.mp4"}, new FFmpegExecuteResponseHandler() {
                @Override public void onSuccess(String message) {ffmpegFour();}
                @Override public void onProgress(String message) {}
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG3","onFailure " + message);
                    Toast.makeText(MainActivity.this,"Ошибка!",Toast.LENGTH_LONG).show();
                }
                @Override public void onStart() {Log.d("FFMPEG3","onStart ");}
                @Override public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
    public  void ffmpegFour() {
        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        try {
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestExample/");
            root.mkdirs();
            File file = new File(root, "vid_" + String.valueOf(System.currentTimeMillis()) + ".mp4");
            videoOutput = file.getPath();
            fFmpeg.execute(new String[]{"-i", getApplicationInfo().dataDir + "/2.mpg", "-qscale:v", "1", file.getPath()}, new FFmpegExecuteResponseHandler() {
                @Override public void onSuccess(String message) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this,"Выполнено!",Toast.LENGTH_LONG).show();
                    dellFile();
                    Log.d("FFMPEG4","onSuccess ");

                }
                @Override public void onProgress(String message) {}
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG4","onFailure " + message);
                    Toast.makeText(MainActivity.this,"Ошибка!",Toast.LENGTH_LONG).show();
                }
                @Override public void onStart() {Log.d("FFMPEG4","onStart ");}
                @Override public void onFinish() {              }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
    public void dellFile(){
        File f = new File(getApplicationInfo().dataDir + "/0.mpg");
        f.delete();
        f = new File(getApplicationInfo().dataDir + "/1.mpg");
        f.delete();
        f = new File(getApplicationInfo().dataDir + "/2.mpg");
        f.delete();
    }

    public static String getFileNameByUri(Context context, Uri uri) {
        String fileName="unknown";
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content")==0)
        {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                fileName = filePathUri.getPath().toString();
            }
        }
        else if (uri.getScheme().compareTo("file")==0)
        {
            fileName = filePathUri.getLastPathSegment().toString();
        }
        else
        {
            fileName = fileName+"_"+filePathUri.getLastPathSegment();
        }
        return fileName;
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }
    }
}