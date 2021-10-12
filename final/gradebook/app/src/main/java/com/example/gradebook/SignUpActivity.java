package com.example.gradebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void signUp(View view) {
        Log.i("gradebook", "Signing up");

        EditText name = findViewById(R.id.user_name);
        String user_name = name.getText().toString().trim();
        EditText email = findViewById(R.id.new_user_email);
        String user_email = email.getText().toString().trim();
        EditText password = findViewById((R.id.new_user_password));
        EditText institution = findViewById(R.id.new_user_institution);
        String user_institution = institution.getText().toString().trim();
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch type = (Switch) findViewById(R.id.user_type);
        String user_type = GradebookProtocol.STUDENT;
        ;
        if (type.isChecked()) {
            user_type = GradebookProtocol.INSTRUCTOR;
        }

        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.SIGNUP_COMMAND + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.NAME +
                    GradebookProtocol.PARAM_ASSIGN + user_name + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.LOGIN_EMAIL +
                    GradebookProtocol.PARAM_ASSIGN + user_email + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.LOGIN_PASSWORD +
                    GradebookProtocol.PARAM_ASSIGN + password.getText().toString() + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.INSTITUTION + GradebookProtocol.PARAM_ASSIGN + user_institution +
                    GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.USER_TYPE + GradebookProtocol.PARAM_ASSIGN + user_type).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TextView sign_up_ack = findViewById(R.id.sign_up_ack);
        GradebookProtocol server_ack = new GradebookProtocol(response);
        if (server_ack.getParam(GradebookProtocol.STATUS).equals(GradebookProtocol.ACK)) {
            sign_up_ack.setText(R.string.sign_up_success);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            sign_up_ack.setText(R.string.sign_up_fail);
        }
    }
}
