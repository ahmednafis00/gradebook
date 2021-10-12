import java.io.*;
import java.net.*;
import java.util.Vector;

public class TcpUser {
    private Socket mClientSocket;
    private DataOutputStream mClientWriter;
    private BufferedReader mClientReader;
    private String mName;
    private String mEmail;
    private String mInstitution;
    private String mType;
    private String mToken;
    private Vector<Course> mCourses;

    public TcpUser(Socket clientSocket) throws IOException {
        mClientSocket = clientSocket;
        mClientWriter = new DataOutputStream(mClientSocket.getOutputStream()); 
        mClientReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
        mCourses = new Vector<>();
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

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getInstitution() {
        return mInstitution;
    }

    public void setInstitution(String institution) {
        mInstitution = institution;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    
    public void setEmail(String email) {
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }


    public void setToken(String token) {
        mToken = token;
    }

    public String getToken() {
        return mToken;
    }

    public void addCourse(Course newCourse) {
        mCourses.addElement(newCourse);
    }

    public Vector<Course> getCourses() {
        return mCourses;
    }
}
