import java.io.*;

public class Server {
    private static final int SERVER_PORT = 6789;
    
    public static void main(String argv[]) throws IOException {
        EmailServer server = new EmailServer(SERVER_PORT);
        server.listen();
    }
}
