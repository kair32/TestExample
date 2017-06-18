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
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.RemoteConnection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.kirill.testexample.R;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
import java.util.Collections;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.protocol.RequestExpectContinue;
import cz.msebera.android.httpclient.entity.StringEntity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import support.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String mUserName, mPassword, mToken, v1,v2, videoOutput;
    private MediaController mediaControllerOne, mediaControllerTwo;
    public String salt = "";
    private VideoView vVideoViewOne, vVideoViewTwo;
    private static final int REQUEST_ONE = 1888;
    private static final int REQUEST_TWO = 1889;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
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
        ffmpeg();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.SETTING_NAME, MODE_PRIVATE);
        mUserName =mSharedPreferences.getString("Username","");
        mPassword = mSharedPreferences.getString("Password","");
        mToken = mSharedPreferences.getString("Token","");
        if(mUserName.equals("") && mPassword.equals("") && mToken.equals(""));
            //startActivity(new Intent(getBaseContext(), LoginActivity.class));
        vVideoViewOne = (VideoView)findViewById(R.id.videoView_one);
        vVideoViewTwo = (VideoView)findViewById(R.id.videoView_two);
        mediaControllerOne = (MediaController)findViewById(R.id.mediaControllerOne);
        mediaControllerTwo = (MediaController)findViewById(R.id.mediaControllerTwo);

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
        zaprosone();
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
                Intent intents = new Intent(Intent.ACTION_VIEW );
                intents.setDataAndType(Uri.parse(String.valueOf(videoOutput)), "video/*");
                startActivity(intents);
                break;
            case R.id.button_send:
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
    public void zaprosone(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Content-Type", "application/json");
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("login", "test");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(this, "https://api.smiber.com/v4.005/salt",entity,null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("TAP Sucsess ONE", " " + response);
                try {
                    JSONObject jsonObjectData = response.getJSONObject("data");
                    salt = jsonObjectData.getString("salt");
                    try {
                        zaprostwo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    private final OkHttpClient client = new OkHttpClient();

    public void zaprostwo() throws Exception {
        RequestBody  formBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", "test")
                .add("password", convert(convert("123456") + salt))
                .build();
        Request request = new Request.Builder()
                .url("https://api.smiber.com/v4.005/oauth/token")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {e.printStackTrace();}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAP", "TWO " + response.body().string());
            }
        });
    }
    public String convert(String password)throws Exception {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<byteData.length;i++) {
                String hex=Integer.toHexString(0xff & byteData[i]);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }
            return  hexString.toString();
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
            String s = getFileNameByUri(this, Uri.parse(v1));
            fFmpeg.execute(new String[]{"-i", s, "-qscale:v", "1", getApplicationInfo().dataDir + "/0.mpg"}, new FFmpegExecuteResponseHandler() {
                @Override public void onSuccess(String message) {}
                @Override public void onProgress(String message) {}
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG","onFailure " + message);
                }
                @Override public void onStart() {Log.d("FFMPEG","onStart ");}
                @Override public void onFinish() {
                    Log.d("FFMPEG","onFinish ");
                    ffmpegTwo();
                }
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
                @Override public void onSuccess(String message) {}
                @Override public void onProgress(String message) {}
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG2","onFailure " + message);
                }
                @Override public void onStart() { Log.d("FFMPEG2","onStart ");}
                @Override public void onFinish() {
                    Log.d("FFMPEG2","onFinish ");ffmpegThree();
                }
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
                @Override public void onSuccess(String message) {}
                @Override public void onProgress(String message) {}
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG3","onFailure " + message);
                }
                @Override public void onStart() {Log.d("FFMPEG3","onStart ");}
                @Override public void onFinish() {
                    Log.d("FFMPEG3","onFinish ");ffmpegFour();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
    public  void ffmpegFour() {
        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        try {
            videoOutput = getApplicationInfo().dataDir + String.valueOf(System.currentTimeMillis()) + ".mp4";
            fFmpeg.execute(new String[]{"-i", getApplicationInfo().dataDir + "/2.mpg", "-qscale:v", "2", videoOutput}, new FFmpegExecuteResponseHandler() {
                @Override public void onSuccess(String message) {}
                @Override public void onProgress(String message) {}
                @Override public void onFailure(String message) {
                    Log.d("FFMPEG4","onFailure " + message);
                }
                @Override public void onStart() {Log.d("FFMPEG4","onStart ");}
                @Override public void onFinish() {
                    dellFile();
                    Log.d("FFMPEG4","onFinish ");
                }
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

}