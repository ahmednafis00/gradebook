import java.io.*;
import java.net.*;
import java.util.*;

public class TcpConnectionListenerThread extends Thread {
    private ServerSocket mServerSocket;
    private HashMap<String, TcpUser> mTcpUsers;
    private Random mRandom = new Random();

    public TcpConnectionListenerThread(ServerSocket serverSocket, HashMap<String, TcpUser> tcpUsers)
            throws IOException {
        this("TcpConnectionListenerThread");
        mServerSocket = serverSocket;
        mTcpUsers = tcpUsers;
    }
    
    public TcpConnectionListenerThread(String name) throws IOException {
        super(name);
    }

    public void run() {
        while (!mServerSocket.isClosed()) {
            try {
                waitForConnection();
            } catch (IOException exception) {
                System.out.println("exception in TcpConnectionListenerThread#run");
            }
        }
    }

    private void waitForConnection() throws IOException {
        System.out.println("Waiting for connection ...");
        Socket clientSocket = mServerSocket.accept();
        System.out.println("Connected!");
        InetAddress clientAddress = clientSocket.getInetAddress();
        System.out.println("client at: " + clientAddress.toString() + ":" + clientSocket.getPort());

        String token = generateToken();
        TcpUser user = new TcpUser(clientSocket);
        synchronized(mTcpUsers) {
            mTcpUsers.put(token, user);
        }
    }

    private String generateToken() {
        return "" + System.currentTimeMillis() + mRandom.nextInt();
    }
}
