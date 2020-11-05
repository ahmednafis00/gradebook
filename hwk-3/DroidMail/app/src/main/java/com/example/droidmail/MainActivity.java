package com.example.droidmail;

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
        Log.i("DroidMail", "Logging in");

        EditText username = (EditText) findViewById(R.id.username);
        String userID = username.getText().toString().trim();
        EditText password = (EditText) findViewById((R.id.password));

        ServerCommunicator message = new ServerCommunicator();
        String response = "";

        try {
            response = message.execute(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN +
                    EmailProtocolMessage.LOGIN_COMMAND + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.LOGIN_USERNAME +
                    EmailProtocolMessage.PARAM_ASSIGN + userID + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.LOGIN_PASSWORD +
                    EmailProtocolMessage.PARAM_ASSIGN + password.getText().toString()).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TextView log_in_ack = (TextView) findViewById(R.id.log_in_ack);
        EmailProtocolMessage server_ack = new EmailProtocolMessage(response);

        if (server_ack.getParam(EmailProtocolMessage.STATUS).equals(EmailProtocolMessage.ACK)) {
            log_in_ack.setText(R.string.log_in_success);
            String token = server_ack.getParam(EmailProtocolMessage.TOKEN);
            System.out.println("Username: " + userID + "Token: " + token);
            Intent intent = new Intent(this, ReadEmailActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("token", token);
            startActivity(intent);
        } else {
            log_in_ack.setText(R.string.log_in_fail);
        }
    }
}
