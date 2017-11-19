package tw.zerator.ffs.pubsub.network;

import com.google.common.net.HttpHeaders;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

@SuppressWarnings("rawtypes")
public class WebSocketHandler extends SimpleChannelInboundHandler {

    private WebSocketServerHandshaker mHandshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object req) throws Exception {
        if (req instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) req;
            // ----- Client authenticity check code -----
            /*String origin = request.headers().get(HttpHeaders.ORIGIN);
            if (origin != null) {
                switch (origin) {
                    case "http://localhost":
                    case "https://localhost":
                    case "http://127.0.0.1":
                    case "https://127.0.0.1":
                        break;
                    default:
                        ctx.channel().close();
                        return;
                }
            }*/
            // -----/Client authenticity check code -----

            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HttpHeaders.HOST) + "/", null, true);
            mHandshaker = wsFactory.newHandshaker(request);
            if (mHandshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                mHandshaker.handshake(ctx.channel(), request);
            }
        } else if (req instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) req;

            if (req instanceof CloseWebSocketFrame) {
                if (mHandshaker != null) {
                    mHandshaker.close(ctx.channel(), ((CloseWebSocketFrame) req).retain());
                }
            } else if (req instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            } else {
                ctx.fireChannelRead(frame.retain());
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
