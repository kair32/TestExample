package activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.RemoteConnection;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.kirill.testexample.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.util.VLCUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import support.Constants;

public class MainActivity extends AppCompatActivity {

    private String mUserName, mPassword, mToken;
    public String salt = "";
    private VideoView vVideoViewOne, vVideoViewTwo;
    private static final int CAMERA_REQUEST_ONE = 1888;
    private static final int CAMERA_REQUEST_TWO = 1889;
    private static final int RQS_VIDEO1 = 183;
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
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.SETTING_NAME, MODE_PRIVATE);
        mUserName =mSharedPreferences.getString("Username","");
        mPassword = mSharedPreferences.getString("Password","");
        mToken = mSharedPreferences.getString("Token","");
        if(mUserName.equals("") && mPassword.equals("") && mToken.equals(""));
            //startActivity(new Intent(getBaseContext(), LoginActivity.class));
        vVideoViewOne = (VideoView)findViewById(R.id.videoView_one);
        vVideoViewTwo = (VideoView)findViewById(R.id.videoView_two);

        Button buttonOpenStorageOne = (Button)findViewById(R.id.button_from_storage);
        buttonOpenStorageOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_VIDEO1);
            }
        });
        Button buttonOpenCameraOne = (Button)findViewById(R.id.button_open_camera);
        buttonOpenCameraOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_ONE);
            }
        });
        Button buttonOpenCameraTwo = (Button)findViewById(R.id.button_open_camera_2);
        buttonOpenCameraTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_TWO);
            }
        });


        zaprosone();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) switch (requestCode) {

            case CAMERA_REQUEST_ONE:
                //vVideoViewOne.setVideoPath(data.getDataString());
                Log.d("TAP", " good one");
                break;
            case CAMERA_REQUEST_TWO:
                videoFromCamera(resultCode, data);
                Log.d("TAP", " good two");
                break;
            case RQS_VIDEO1:
                Log.d("TAP", " good two" + data.getData());
                vVideoViewOne.setVideoPath(String.valueOf(data.getData()));
                break;
        }
    }
    private void videoFromCamera(int resultCode, Intent data) {
        Uri uriVideo = data.getData();
        String uploadedFileName="";
        //Constant.IS_FILE_ATTACH = true;

        Toast.makeText(MainActivity.this, uriVideo.getPath(), Toast.LENGTH_LONG)
                .show();
        String[] filePathColumn = { MediaStore.Video.Media.DATA };

        Cursor cursor = getContentResolver().query(uriVideo, filePathColumn,
                null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        //Videofilepath = filePath;
        //System.out.println("Videofilepath filepath from camera : " + Videofilepath);
        cursor.close();
        File f = new File(filePath);
        vVideoViewTwo.setVideoPath(filePath);
        /*Bitmap bMap = null;
        do {
            try {
                // Simulate network access.
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!f.exists());
        bMap = ThumbnailUtils.createVideoThumbnail(filePath,
                MediaStore.Video.Thumbnails.MICRO_KIND);
        do {
            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (bMap == null);*/
        //imageOrVideo = "video";
        //attachmentvalue.setImageBitmap(bMap);
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
                    zaprostwo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("TAP ERRor ONE", " " + errorResponse);
            }
        });
    }
    public void zaprostwo(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Content-Type", "application/x-www-form-urlencoded");
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("grant_type", bin2hex(getHash("123456" + salt)));
            jsonParams.put("username", "test");
            jsonParams.put("password", bin2hex(getHash("123456" + salt)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(this, "https://api.smiber.com/v4.005/oauth/token",entity,null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("TAP Sucsess", " " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("TAP ERROR", " " + errorResponse);
            }
        });
    }
    public byte[] getHash(String password) {
        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }
}