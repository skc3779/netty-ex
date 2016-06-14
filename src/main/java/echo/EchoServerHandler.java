package echo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * Created by seokangchun on 2016. 6. 10..
 */
@Slf4j
public class EchoServerHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 내용없음
        String readMessage = ((ByteBuf)msg).toString(Charset.defaultCharset());

        log.info("수신문자열 : {}", readMessage);

        //ctx.write(msg);
        ctx.writeAndFlush(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.flush();
    }
}
