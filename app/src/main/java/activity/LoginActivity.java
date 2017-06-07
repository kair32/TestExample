package activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kirill.testexample.R;

/**
 * Created by Kirill on 06.06.2017.
 */

public class LoginActivity extends AppCompatActivity {
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
}
