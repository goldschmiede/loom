package server;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloServer {

    private Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    void start(ExecutorService executorService) throws IOException {
        ServerSocket server = new ServerSocket(6868);
        while (true) {
            Socket connection = server.accept();
            executorService.submit(() -> serve(connection));
        }
    }

    private void serve(Socket connection) {
        try (Socket con = connection) {
            InputStream in = con.getInputStream();
            OutputStream out = con.getOutputStream();
            StringBuilder request = new StringBuilder(512);
            int count = 0;
            int c;
            while (count < 2 && (c = in.read()) != -1) {
                if (c >= ' ') {
                    request.append((char) c);
                    count=0;
                } else if (c == '\n') {
                    count++;
                    request.append("<10>\n");
                } else {
                    request.append("<" + c + ">");
                }
            }
            log.log(Level.FINE, () -> "Serving...\n" + request.toString());
            String response = """
                HTTP/1.1 200
                Content-Type: text/html;charset=UTF-8
                Content-Length: 12
                                
                Hello World!
                """.replace("\n", "\r\n");
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Cannot serve", ex);
        }
    }

    public static void main(String[] args) throws IOException {
        ExecutorService executorService = switch (5) {
            case 1 -> Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory());
            case 2 -> Executors.newFixedThreadPool(10);
            case 3 -> Executors.newVirtualThreadPerTaskExecutor();
            case 4 -> Executors.newWorkStealingPool();
            case 5 -> Executors.newCachedThreadPool();
            default -> Executors.newSingleThreadExecutor();
        };
        new HelloServer().start(executorService);
    }
}
