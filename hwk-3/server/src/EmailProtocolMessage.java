import java.util.HashMap;

public class EmailProtocolMessage {
    // String constants
    public static final String PARAM_SEPERATOR = "&";
    public static final String PARAM_ASSIGN = "=";
    public static final String TYPE_KEY = "type";
    public static final String LOGIN_COMMAND = "log_in";
    public static final String LOGIN_USERNAME = "username";
    public static final String LOGIN_PASSWORD = "password";
    public static final String LOGIN_ACK = "log_in_ack";
    public static final String STATUS = "status";
    public static final String ACK = "ok";
    public static final String INVALID_TOKEN = "failed";
    public static final String SEND_EMAIL_COMMAND = "send_email";
    public static final String SEND_EMAIL_ACK = "send_email_ack";
    public static final String RETRIEVE_EMAIL_COMMAND = "retrieve_emails";
    public static final String EMAIL = "email";
    public static final String EMAILS_INBOX = "emails";
    public static final String EMAIL_SENDER = "from";
    public static final String EMAIL_RECEIVER = "to";
    public static final String EMAIL_BODY = "body";
    public static final String EMAIL_SEPARATOR = ";";
    public static final String EMAIL_CONTENT = ">";
    public static final String EMAIL_END = "ZZZ";
    public static final String LOGOUT_COMMAND = "log_out";
    public static final String LOGOUT_ACK = "log_out_ack";
    public static final String TOKEN = "token";

    private static HashMap<String, String> emailMap = new HashMap<String, String>();

    public EmailProtocolMessage(String clientMessage) {
        parseStr(clientMessage); 
    }
    
    public void putParam(String key, String val) {
        emailMap.put(key, val);
    }

    public String getParam(String key) {
        return emailMap.get(key);
    }

    public void parseStr(String clientMessage) {
        String[] arr = clientMessage.split(PARAM_SEPERATOR, 0);
        for (int i = 0; i < arr.length; ++i) {
            emailMap.put(arr[i].substring(0, arr[i].indexOf(PARAM_ASSIGN)), 
            arr[i].substring(arr[i].indexOf(PARAM_ASSIGN) + PARAM_ASSIGN.length()));
        }
    }
}
