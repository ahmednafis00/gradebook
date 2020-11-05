package com.example.droidmail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class ReadEmailActivity extends AppCompatActivity {
    private HashMap<String, String> inboxMap = new HashMap<>();
    private static String userID;
    private static String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_main);

        Intent thisIntent = getIntent();
        userID = thisIntent.getStringExtra("userID");
        token = thisIntent.getStringExtra("token");

        ListView resultsListView = (ListView) findViewById(R.id.results_listview);

        Log.i("DroidMail", "Reading emails");
        ServerCommunicator message = new ServerCommunicator();
        String response = "";

        try {
            response = message.execute(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN +
                    EmailProtocolMessage.RETRIEVE_EMAIL_COMMAND + EmailProtocolMessage.PARAM_SEPERATOR +
                    EmailProtocolMessage.TOKEN + EmailProtocolMessage.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String userInbox = response.substring(EmailProtocolMessage.TYPE_KEY.length() +
                (EmailProtocolMessage.PARAM_ASSIGN.length() * 2) + (EmailProtocolMessage.EMAILS_INBOX.length() * 2)
                + EmailProtocolMessage.PARAM_SEPERATOR.length());

        List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.activity_read,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        if (!userInbox.equals(EmailProtocolMessage.EMAIL_END)) {
            String[] arrInbox = userInbox.split(EmailProtocolMessage.EMAIL_END, 0);
            int count = 0;
            for (int i = 0; i < arrInbox.length; ++i) {
                String[] arrMsg = arrInbox[i].split(EmailProtocolMessage.EMAIL_SEPARATOR, 0);
                String sender = "";
                String body = "";
                for (int j = 0; j < arrMsg.length; ++j) {
                    if (arrMsg[j].substring(0, arrMsg[j].indexOf(EmailProtocolMessage.EMAIL_CONTENT)).equals(EmailProtocolMessage.EMAIL_SENDER)) {
                        sender = arrMsg[j].substring(arrMsg[j].indexOf(EmailProtocolMessage.EMAIL_CONTENT) + EmailProtocolMessage.EMAIL_CONTENT.length());
                    } else if (arrMsg[j].substring(0, arrMsg[j].indexOf(EmailProtocolMessage.EMAIL_CONTENT)).equals(EmailProtocolMessage.EMAIL_BODY)) {
                        body = arrMsg[j].substring(arrMsg[j].indexOf(EmailProtocolMessage.EMAIL_CONTENT) + EmailProtocolMessage.EMAIL_CONTENT.length());
                    }
                }
                ++count;
                inboxMap.put(count + ". " + sender, body);
            }

            Iterator it = inboxMap.entrySet().iterator();
            while (it.hasNext()) {
                HashMap<String, String> resultsMap = new HashMap<>();
                Map.Entry pair = (Map.Entry) it.next();
                resultsMap.put("First Line", pair.getKey().toString());
                resultsMap.put("Second Line", pair.getValue().toString());
                listItems.add(resultsMap);
            }
        } else {
            HashMap<String, String> resultsMap = new HashMap<>();
            resultsMap.put("First Line", "No messages");
            resultsMap.put("Second Line", "Your inbox is empty.");
            listItems.add(resultsMap);
        }

        resultsListView.setAdapter(adapter);
    }

    public void retrieveEmails(View view) {
        Intent intent = new Intent(this, ReadEmailActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    public void sendEmail(View view) {
        Intent intent = new Intent(this, SendEmailActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    public void logOut(View view) {
        Log.i("DroidMail", "Logging out");
        ServerCommunicator message = new ServerCommunicator();
        String response = "";

        try {
            response = message.execute(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN +
                    EmailProtocolMessage.LOGOUT_COMMAND + EmailProtocolMessage.PARAM_SEPERATOR +
                    EmailProtocolMessage.TOKEN + EmailProtocolMessage.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EmailProtocolMessage logout_ack = new EmailProtocolMessage(response);
        if (logout_ack.getParam(EmailProtocolMessage.STATUS).equals(EmailProtocolMessage.ACK)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            System.out.println(userID + " has successfully logged out.");
        } else {
            System.out.println("Log out unsuccessful.");
        }
    }
}
