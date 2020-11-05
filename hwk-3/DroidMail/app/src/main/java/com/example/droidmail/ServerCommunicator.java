package com.example.droidmail;

import android.os.AsyncTask;

import java.io.*;
import java.net.Socket;

public class ServerCommunicator extends AsyncTask<String, Integer, String> {
    private static final String SERVER_ADDRESS = "10.0.2.2";
    private static final int PORT = 6789;

    @Override
    protected String doInBackground(String... strings) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
            BufferedReader serverBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToServer.writeBytes(strings[0] + "\n");
            String responseFromServer = serverBufferedReader.readLine();
            socket.close();
            return responseFromServer;
        } catch (IOException e) {
            return "error";
        }
    }
}
