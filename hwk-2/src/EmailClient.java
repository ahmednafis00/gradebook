import java.io.*;
import java.net.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class EmailClient {
    private static final String HOST_ADDRESS = "127.0.0.1";
    private static final int PORT = 6789;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(HOST_ADDRESS, PORT);
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        BufferedReader serverBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader userBufferedReader = new BufferedReader(new InputStreamReader(System.in));
        
        // log in
        System.out.println("What is your username?"); 
        String username = userBufferedReader.readLine().trim();
        outToServer.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.LOGIN_COMMAND + 
            EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.LOGIN_USERNAME + EmailProtocolMessage.PARAM_ASSIGN + username + "\n");
        EmailProtocolMessage login_ack = new EmailProtocolMessage(serverBufferedReader.readLine());
        if (login_ack.getParam(EmailProtocolMessage.STATUS).equals(EmailProtocolMessage.ACK)) {
            System.out.println("\nLog in successful. Hello, " + username + "!");
        } else {
            System.out.println("\nLog in unsuccessful.");
            System.exit(0);
        }
        
        Scanner scnr = new Scanner(System.in);        
        boolean quit = false;
        while (!quit) {
            displayMenu(); 
            int choice = getInput(scnr);

            if (choice == 1) {
                // send email
                System.out.println("\nEnter the recipient's name: ");
                String recipient = userBufferedReader.readLine().trim();
                System.out.println("Enter your message: ");
                String message = userBufferedReader.readLine();
                outToServer.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN +
                    EmailProtocolMessage.SEND_EMAIL_COMMAND + EmailProtocolMessage.PARAM_SEPERATOR + EmailProtocolMessage.EMAIL +
                    EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.EMAIL_RECEIVER + EmailProtocolMessage.EMAIL_CONTENT +
                    recipient + EmailProtocolMessage.EMAIL_SEPARATOR + EmailProtocolMessage.EMAIL_SENDER + EmailProtocolMessage.EMAIL_CONTENT + 
                    username + EmailProtocolMessage.EMAIL_SEPARATOR + EmailProtocolMessage.EMAIL_BODY + EmailProtocolMessage.EMAIL_CONTENT + message + "\n");
                EmailProtocolMessage send_ack = new EmailProtocolMessage(serverBufferedReader.readLine());
                if (send_ack.getParam(EmailProtocolMessage.STATUS).equals(EmailProtocolMessage.ACK)) {
                    System.out.println("\nEmail successfully sent to " + recipient + ".");
                } else {
                    System.out.println("\nThe email wasn't sent.");
                }
            } else if (choice == 2) {
                // retrieve emails
                outToServer.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.RETRIEVE_EMAIL_COMMAND + "\n");
                String userInbox = serverBufferedReader.readLine();
                userInbox = userInbox.substring(EmailProtocolMessage.TYPE_KEY.length() + (EmailProtocolMessage.PARAM_ASSIGN.length() * 2) + 
                    (EmailProtocolMessage.EMAILS_INBOX.length() * 2) + EmailProtocolMessage.PARAM_SEPERATOR.length()); // change
                if (userInbox.substring(0).equals(EmailProtocolMessage.EMAIL_END)) {
                    System.out.println("\nYou have no messages in your inbox.");
                } else {
                    String[] arrInbox = userInbox.split(EmailProtocolMessage.EMAIL_END, 0);
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
                        System.out.println("\nFrom: " + sender + "\nBody: " + body);
                    }
                }
            } else {
                // log out
                outToServer.writeBytes(EmailProtocolMessage.TYPE_KEY + EmailProtocolMessage.PARAM_ASSIGN + EmailProtocolMessage.LOGOUT_COMMAND + "\n");
                EmailProtocolMessage logout_ack = new EmailProtocolMessage(serverBufferedReader.readLine());
                if (logout_ack.getParam(EmailProtocolMessage.STATUS).equals(EmailProtocolMessage.ACK)) {
                    System.out.println("You have successfully logged out.");
                } else {
                    System.out.println("Log out unsuccessful.");
                }
                quit = true;
            }
        }
       socket.close();
    }

    private static void displayMenu() {
        System.out.println("\nMAIN MENU:\n" +
        "1 Send Email\n" +
        "2 Read Email\n" +
        "3 Exit\n");
    }

    public static int getInput(Scanner scnr)  {
        System.out.println("Choose an option from 1, 2, and 3: ");
        int input = 0;
        
        try {
            input = scnr.nextInt();
            while (input < 1 || input > 3) {
                System.out.println("Please enter a valid number: ");
                input = scnr.nextInt();
            } 
        } catch (InputMismatchException err) {
            System.out.println("Invalid input. Please reconnect to the server.");
        }
        
        return input;
    }
}
