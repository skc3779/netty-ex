package blocking;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by seokangchun on 2016. 6. 13..
 */
@Slf4j
public class BlockingServer {
    public static void main(String[] args) throws Exception {
        BlockingServer server = new BlockingServer();
        server.run();

    }

    private void run() throws IOException {
        ServerSocket server = new ServerSocket(8888);
        log.info("접속 대기중..");

        while (true) {
            Socket sock = server.accept();
            log.info(" 클라이언트 연결됨..");

            try(
                OutputStream out = sock.getOutputStream();
                InputStream in = sock.getInputStream(); )
            {

                while (true) {
                    try {
                        int request = in.read();
                        String message = String.valueOf((char)request);
                        log.info("client message : {}", message);
                        if(message.equals("c")) {
                            log.info("client disconnect..");
                            break;
                        }
                        out.write(request);
                    } catch (IOException e) {
                        break;
                    }
                }
            }
        }
    }


}
