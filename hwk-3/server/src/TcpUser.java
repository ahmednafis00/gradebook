import java.io.*;
import java.net.*;

public class TcpUser {
    private Socket mClientSocket;
    private String mUsername;
    private String mToken;
    private DataOutputStream mClientWriter;
    private BufferedReader mClientReader;

    public TcpUser(Socket clientSocket) throws IOException {
        mClientSocket = clientSocket;
        mClientWriter = new DataOutputStream(mClientSocket.getOutputStream()); 
        mClientReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
    }

    public void sendMessage(String serverMessage) throws IOException {
        mClientWriter.writeBytes(serverMessage + "\n");
    }

    public String readMessage() throws IOException {
        return mClientReader.readLine();
    }

    public boolean hasMessage() throws IOException {
        return mClientReader.ready();
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }


    public void setToken(String token) {
        mToken = token;
    }

    public String getToken() {
        return mToken;
    }
}
