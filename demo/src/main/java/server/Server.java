package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    void start() throws IOException {
        ServerSocket server = new ServerSocket(6868);
        for (;;) {
            try (Socket request = server.accept()) {
                log.info("Accepting request.");
                new Thread(() -> serve(request)).start();
            }
        }
    }

    private void serve(Socket request) {
        if (request.isInputShutdown()) {
            log.warning("input shut down");
            return;
        }
        try (InputStream is = request.getInputStream()) {
            int c;
            while ((c = is.read()) != -1) {
                if (c < ' ') {
                    System.out.print("<" + c + ">");
                }
                System.out.print((char)c);
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Cannot serve", ex);
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }
}
