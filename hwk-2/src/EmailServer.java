import java.io.*;
import java.net.*;

public class EmailServer {
    private ServerSocket mServerSocket;
    private Socket mClientSocket;

    public EmailServer(int port) throws IOException {
        mServerSocket = new ServerSocket(port);
    }

    public void listen() throws IOException {
        waitforConnection(); 
        BufferedReader clientReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
        DataOutputStream clientWriter = new DataOutputStream(mClientSocket.getOutputStream()); 
        Inbox serverInbox = new Inbox();

        boolean quit = false;
        while(!quit) {
            EmailProtocolMessage message = new EmailProtocolMessage(clientReader.readLine());

            switch(message.getParam(EmailProtocolMessage.TYPE_KEY)) {
                case (EmailProtocolMessage.LOGIN_COMMAND):
                    // log in
                    clientWriter.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.LOGIN_ACK + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.ACK + "\n");
                    break;
                case (EmailProtocolMessage.SEND_EMAIL_COMMAND):
                    //sendEmail();
                    clientWriter.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.SEND_EMAIL_ACK + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.ACK + "\n");
                    serverInbox.addEmail(message.getParam(EmailProtocolMessage.EMAIL));
                    break;
                case (EmailProtocolMessage.RETRIEVE_EMAIL_COMMAND):
                    // retrieve email
                    String userInbox = serverInbox.getParam(message.getParam(EmailProtocolMessage.LOGIN_USERNAME));
                    if (userInbox == null) {
                        clientWriter.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.EMAIL_END + "\n");
                    } else {
                        clientWriter.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_ASSIGN + userInbox + "\n");
                    }
                    break;
                case (EmailProtocolMessage.LOGOUT_COMMAND):
                    // log out
                    clientWriter.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.LOGOUT_ACK + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.ACK + "\n");
                    System.out.println();
                    listen();
                    break;
                default:
                    System.out.println("Server shut down due to invalid command type.");
                    quit = true;
            }
        }
    }

    public void waitforConnection() throws IOException {
        System.out.println("Waiting for a connection ...");
        mClientSocket = mServerSocket.accept();
        System.out.println("Connected");
        InetAddress clientAddress = mClientSocket.getInetAddress();
        System.out.println("Client at: " + clientAddress.toString() + ":" + mClientSocket.getPort());
    }  
}
