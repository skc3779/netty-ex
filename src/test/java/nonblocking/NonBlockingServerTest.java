package nonblocking;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by seokangchun on 2016. 6. 13..
 */
@Slf4j
public class NonBlockingServerTest {
    @Test
    public void ControlPlusStringTest() {
        char ctrlC = 0x3;
        String controlC = Character.toString(ctrlC);

        if(controlC.equals(ctrlC)) {
            log.info("control + c is true");
        } else {
            log.info("control + c is false");
        }

        log.info("control + c : {}", controlC);

    }
}