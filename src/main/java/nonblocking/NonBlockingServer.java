package nonblocking;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by seokangchun on 2016. 6. 13..
 */
@Slf4j
public class NonBlockingServer {
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2*1024);

    public static void main(String[] args) throws Exception {
        NonBlockingServer server = new NonBlockingServer();
        server.startEchoServer();
    }

    private void startEchoServer() {
        try (
                Selector selector = Selector.open();
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            ) {

            if((serverSocketChannel.isOpen()) && (selector.isOpen())) {
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.bind(new InetSocketAddress(8888));

                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                log.info("접속 대기중..");


                while (true) {
                    selector.select();
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        keys.remove();

                        if(!key.isValid()) {
                            continue;
                        }

                        if(key.isAcceptable()) {
                            this.acceptOP(key, selector);
                        } else if(key.isReadable()) {
                            this.readOP(key);
                        } else if(key.isWritable()) {
                            this.writeOP(key);
                        } else {
                            log.info("서비스 소켓을 생성하지 못했습니다..");
                        }

                    }


                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();

        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void readOP(SelectionKey key) {

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;

            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                log.error("데이터 읽기오류 : {}", e.getMessage());
            }

            if(numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                log.info("클라이언트 연결종료 : {}", socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }

            byte[] data = new byte[numRead];

            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            String message = StringUtils.newStringUtf8(data);

            message = message.replaceAll("\\r", "").replaceAll("\\n", "");

            if(message.equals("^c")) {
                this.keepDataTrack.remove(socketChannel);
                log.info("클라이언트 연결종료 : {}", socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }

            log.info("{} from {}", message, socketChannel.getRemoteAddress());

            doEchoJob(key,data);

        } catch (IOException e) {
            log.error("read Error : {}", e);
        }
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData    = keepDataTrack.get(socketChannel);
        channelData.add(data);

        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();

        socketChannel.configureBlocking(false);

        log.info("클라이언트 연결됨 : {}", socketChannel.getRemoteAddress());

        keepDataTrack.put(socketChannel, new ArrayList<byte[]>());

        socketChannel.register(selector, SelectionKey.OP_READ);

    }

}
