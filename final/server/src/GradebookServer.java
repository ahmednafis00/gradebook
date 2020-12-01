import java.io.*;

public class GradebookServer {
    private static final int SERVER_PORT = 6789;
    
    public static void main(String argv[]) throws IOException {
        Server server = new Server(SERVER_PORT);
        server.listen();
    }
}