package com.example.gradebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void logIn(View view) {
        Log.i("gradebook", "Logging in");

        EditText email = findViewById(R.id.email);
        String emailID = email.getText().toString().trim();
        EditText password = findViewById((R.id.password));

        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.LOGIN_COMMAND + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.LOGIN_EMAIL +
                    GradebookProtocol.PARAM_ASSIGN + emailID + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.LOGIN_PASSWORD + GradebookProtocol.PARAM_ASSIGN + password.getText().toString()).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TextView log_in_ack = findViewById(R.id.log_in_ack);
        GradebookProtocol server_ack = new GradebookProtocol(response);

        if (server_ack.getParam(GradebookProtocol.STATUS).equals(GradebookProtocol.ACK)) {
            log_in_ack.setText(R.string.log_in_success);
            String token = server_ack.getParam(GradebookProtocol.TOKEN);
            String user_type = server_ack.getParam(GradebookProtocol.USER_TYPE);
            System.out.println("Email: " + email + "Token: " + token);

            Intent intent;
            if (user_type.equals(GradebookProtocol.STUDENT)) {
                intent = new Intent(this, StudentWelcomeActivity.class);
            } else {
                intent = new Intent(this, InstructorWelcomeActivity.class);
            }
            intent.putExtra("token", token);
            startActivity(intent);
        } else {
            log_in_ack.setText(R.string.log_in_fail);
        }
    }

    public void newAcc(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}