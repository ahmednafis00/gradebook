package com.example.droidmail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.util.concurrent.ExecutionException;

public class SendEmailActivity extends AppCompatActivity {
    private static String userID;
    private static String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Intent thisIntent = getIntent();
        userID = thisIntent.getStringExtra("userID");
        token = thisIntent.getStringExtra("token");
    }

    public void sendEmail(View view) throws IOException {
        Log.i("DroidMail", "Sending email");

        EditText recipient = (EditText) findViewById(R.id.recipient);
        EditText message = (EditText) findViewById((R.id.body));

        ServerCommunicator serverMessage = new ServerCommunicator();
        String response = "";

        try {
            response = serverMessage.execute(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN +
                    EmailProtocolMessage.SEND_EMAIL_COMMAND + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.EMAIL +
                    EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.EMAIL_RECEIVER + EmailProtocolMessage.EMAIL_CONTENT +
                    recipient.getText().toString().trim() + EmailProtocolMessage.EMAIL_SEPARATOR + EmailProtocolMessage.EMAIL_SENDER +
                    EmailProtocolMessage.EMAIL_CONTENT + userID + EmailProtocolMessage.EMAIL_SEPARATOR +
                    EmailProtocolMessage.EMAIL_BODY + EmailProtocolMessage.EMAIL_CONTENT + message.getText().toString() +
                    EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.TOKEN + EmailProtocolMessage.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TextView send_ack = (TextView) findViewById(R.id.send_ack);
        EmailProtocolMessage server_ack = new EmailProtocolMessage(response);
        if (server_ack.getParam(EmailProtocolMessage.STATUS).equals(EmailProtocolMessage.ACK)) {
            send_ack.setText(R.string.send_success);
        } else {
            send_ack.setText(R.string.send_fail);
        }

        Intent intent = new Intent(this, ReadEmailActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("token", token);
        startActivity(intent);
    }
}
