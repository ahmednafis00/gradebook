import java.util.HashMap;

public class Inbox {
    private static HashMap<String, String> inboxMap = new HashMap<String, String>();

    public Inbox() {
    }

    public void addEmail(String message) {
        String receiver = "";
        String[] arr = message.split(EmailProtocolMessage.EMAIL_SEPARATOR, 0);
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i].substring(0, arr[i].indexOf(EmailProtocolMessage.EMAIL_CONTENT))
                    .equals(EmailProtocolMessage.EMAIL_RECEIVER)) {
                receiver = arr[i].substring(arr[i].indexOf(EmailProtocolMessage.EMAIL_CONTENT)
                        + EmailProtocolMessage.EMAIL_CONTENT.length());
                break;
            }
        }

        if (inboxMap.get(receiver) == null) {
            inboxMap.put(receiver, message);
        } else {
            inboxMap.put(receiver, inboxMap.get(receiver) + EmailProtocolMessage.EMAIL_END + message);
        }
    }

    public void putParam(String key, String val) {
        inboxMap.put(key, val);
    }

    public String getParam(String key) {
        return inboxMap.get(key);
    }    
}
