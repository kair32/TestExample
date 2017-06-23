package activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kirill.testexample.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import support.Constants;

/**
 * Created by Kirill on 06.06.2017.
 */

public class LoginActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private String salt = "";
    private String mToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button vExitButton, vEntrButton;
        final EditText vLoginEditText, vPasswordEditText;
        //Toast toast = Toast.makeText(getApplicationContext(), "Неверный логин или пароль", Toast.LENGTH_LONG);
        vLoginEditText = (EditText)findViewById(R.id.editText_login);
        vPasswordEditText = (EditText)findViewById(R.id.editText_password);
        vEntrButton = (Button)findViewById(R.id.button_entr);
        vEntrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vLoginEditText.getText().toString()!="" && vPasswordEditText.getText().toString()!=""){
                    zaprosone();
                }
            }
        });
        vExitButton = (Button)findViewById(R.id.button_exit);
        vExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
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
                Log.d("TAP","ERROR " + errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public void zaprostwo() throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", "test")
                .add("password", convert(convert("123456") + salt))
                .build();
        final Request request = new Request.Builder()
                .url("https://api.smiber.com/v4.005/oauth/token")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {e.printStackTrace();}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try{
                    JSONObject jsonObject = new JSONObject(res);
                    mToken = jsonObject.getString("access_token");
                    Log.d("TAP", "access_token - " + mToken);
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.SETTING_NAME, MODE_PRIVATE).edit();
                    editor.putString("Data_Last_Connect", String.valueOf(LocalDateTime.now()));
                    editor.putString("Token",mToken);
                    editor.commit();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                catch (JSONException e){}
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
}
