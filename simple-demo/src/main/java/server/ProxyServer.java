package server;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class ProxyServer {

    private Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private Selector selector;
    private ServerSocketChannel server;
    private final ByteBuffer serverBuffer = ByteBuffer.allocate(1024);

    void startServer() throws IOException {
        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(6889));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT); // <2>

        while (true) {
            waitForNextIO();
        }
    }

    private void waitForNextIO() throws IOException {
        int count = selector.select();
        log.info(() -> "Received " + count + " IO requests.");
        Set<SelectionKey> selectedKeys = selector.selectedKeys();

        Iterator<SelectionKey> iter = selectedKeys.iterator();
        while (iter.hasNext()) {
            SelectionKey selectedKey = iter.next();
            if (selectedKey.isAcceptable()) {
                handleNewClientConnection(selectedKey);
            } else if (selectedKey.isReadable()) {
                handleDataReceivedFormClient(selectedKey);
            }
            iter.remove();
        }
    }

    private void handleNewClientConnection(SelectionKey selectedKey) throws IOException {
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    private void handleDataReceivedFormClient(SelectionKey selectedKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectedKey.channel();
        serverBuffer.clear();
        channel.read(serverBuffer);
        serverBuffer.flip();
        byte[] bytes = new byte[serverBuffer.remaining()];
        serverBuffer.get(bytes);
        String received = new String(bytes, StandardCharsets.UTF_8);
        log.info("received: " + received);

        serverBuffer.clear();
        serverBuffer.put(("echo: " + received).getBytes(StandardCharsets.UTF_8));
        serverBuffer.flip();
        channel.write(serverBuffer);

        if ("exit".equals(received.trim())) {
            channel.close();
        }
    }

    public static void main(String[] args) throws IOException {
        new ProxyServer().startServer();
    }
}
