import java.io.*;
import java.net.*;
import java.util.*;

public class EmailServer {
    private ServerSocket mServerSocket;
    private HashMap<String, String> mPasswordMap;
    private HashMap<String, TcpUser> mTcpUsers;
    private Inbox mInbox;

    public EmailServer(int port) throws IOException {
        mServerSocket = new ServerSocket(port);
        mPasswordMap = new HashMap<String, String>();
        mTcpUsers = new HashMap<String, TcpUser>();
        mInbox = new Inbox();
    }

    public void listen() throws IOException {
        boolean quit = false;
        TcpConnectionListenerThread listenerThread = new TcpConnectionListenerThread(mServerSocket, mTcpUsers);
        listenerThread.start();

        while (!quit) {
            synchronized (mTcpUsers) {
                String[] tokens = mTcpUsers.keySet().toArray(new String[0]);
                for (String token : tokens) {
                    TcpUser user = mTcpUsers.get(token);

                    if (user.hasMessage()) {
                        String rawMessage = user.readMessage();
                        if (rawMessage == null || !rawMessage.contains("type=")) {
                            rawMessage = "";
                        }

                        EmailProtocolMessage message = new EmailProtocolMessage(rawMessage);
                        switch (message.getParam(EmailProtocolMessage.TYPE_KEY)) {
                            case (EmailProtocolMessage.LOGIN_COMMAND):
                                user.setToken(token);
                                if (logIn(user, message) != 0) {
                                    System.out.println("Duplicate login attempt by "
                                            + message.getParam(EmailProtocolMessage.LOGIN_USERNAME));
                                    user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                                        + EmailProtocolMessage.LOGIN_ACK + EmailProtocolMessage.PARAM_SEPERATOR
                                        + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN
                                        + EmailProtocolMessage.INVALID_TOKEN);
                                    mTcpUsers.remove(token);
                                } else {
                                    System.out.println(user.getUsername() + " logged in");
                                }
                                break;
                            case (EmailProtocolMessage.SEND_EMAIL_COMMAND):
                                if (sendEmail(user, message) != 0) {
                                    user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                                        + EmailProtocolMessage.SEND_EMAIL_ACK + EmailProtocolMessage.PARAM_SEPERATOR
                                        + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN + 
                                        EmailProtocolMessage.INVALID_TOKEN);
                                }
                                break;
                            case (EmailProtocolMessage.RETRIEVE_EMAIL_COMMAND):
                                retrieveEmails(user, message); 
                                break;
                            case (EmailProtocolMessage.LOGOUT_COMMAND):
                                if (logOut(user, message) != 0) {
                                    user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                                        + EmailProtocolMessage.LOGOUT_ACK + EmailProtocolMessage.PARAM_SEPERATOR
                                        + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN 
                                        + EmailProtocolMessage.INVALID_TOKEN);
                                }
                                mTcpUsers.remove(token);
                                break;
                            default:
                                mTcpUsers.remove(token);
                        }
                    }
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            listenerThread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private int logIn(TcpUser user, EmailProtocolMessage message) {
        if (mPasswordMap.get(message.getParam(EmailProtocolMessage.LOGIN_USERNAME)) == null) {
            mPasswordMap.put(message.getParam(EmailProtocolMessage.LOGIN_USERNAME),
                    message.getParam(EmailProtocolMessage.LOGIN_PASSWORD));
        } else {
            if (!message.getParam(EmailProtocolMessage.LOGIN_PASSWORD)
                    .equals(mPasswordMap.get(message.getParam(EmailProtocolMessage.LOGIN_USERNAME)))) {
                return -1;
            }
        }

        user.setUsername(message.getParam(EmailProtocolMessage.LOGIN_USERNAME));
        try {
            user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                    + EmailProtocolMessage.LOGIN_ACK + EmailProtocolMessage.PARAM_SEPERATOR
                    + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN 
                    + EmailProtocolMessage.ACK + EmailProtocolMessage.PARAM_SEPERATOR + 
                    EmailProtocolMessage.TOKEN + EmailProtocolMessage.PARAM_ASSIGN + user.getToken());
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    private int sendEmail(TcpUser user, EmailProtocolMessage message) {
        String token = message.getParam(EmailProtocolMessage.TOKEN);
        TcpUser destUser = mTcpUsers.get(token);
        
        if (token == null || destUser.getUsername() == null) {
            return -1;
        }
        
        try {
            user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                    + EmailProtocolMessage.SEND_EMAIL_ACK + EmailProtocolMessage.PARAM_SEPERATOR
                    + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN 
                    + EmailProtocolMessage.ACK);
        } catch (IOException e) {
            return -1;
        }
        mInbox.addEmail(message.getParam(EmailProtocolMessage.EMAIL));
        return 0;
    }

    private int retrieveEmails(TcpUser user, EmailProtocolMessage message) {
        String token = message.getParam(EmailProtocolMessage.TOKEN);
        TcpUser destUser = mTcpUsers.get(token);

        if (token == null || destUser.getUsername() == null) {
            return -1;
        }

        String userInbox = mInbox.getParam(destUser.getUsername());
        if (userInbox == null) {
            try {
                user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                        + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_SEPERATOR
                        + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_ASSIGN
                        + EmailProtocolMessage.EMAIL_END);
            } catch (IOException e) {
                return -1;
            }
        } else {
            try {
                user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                        + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_SEPERATOR
                        + EmailProtocolMessage.EMAILS_INBOX + EmailProtocolMessage.PARAM_ASSIGN + userInbox);
            } catch (IOException e) {
                return -1;
            }
        }
        return 0;
    }

    private int logOut(TcpUser user, EmailProtocolMessage message) {
        String token = message.getParam(EmailProtocolMessage.TOKEN);
        TcpUser destUser = mTcpUsers.get(token);

        if (token == null || destUser.getUsername() == null) {
            return -1;
        }
        
        try {
            user.sendMessage(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN
                    + EmailProtocolMessage.LOGOUT_ACK + EmailProtocolMessage.PARAM_SEPERATOR
                    + EmailProtocolMessage.STATUS + EmailProtocolMessage.PARAM_ASSIGN 
                    + EmailProtocolMessage.ACK);
        } catch (IOException e) {
            return -1;
        }

        System.out.println(destUser.getUsername() + " logged out" + "\n");
        return 0;
    }
}
