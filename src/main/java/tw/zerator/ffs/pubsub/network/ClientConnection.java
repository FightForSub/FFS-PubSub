package tw.zerator.ffs.pubsub.network;

import io.netty.channel.Channel;
import tv.zerator.ffs.pubsub.Server;
import tw.zerator.ffs.pubsub.network.packet.Packet;
import tw.zerator.ffs.pubsub.network.packet.inbound.SubscribeUnsubscribePacket;
import tw.zerator.ffs.pubsub.network.packet.outbound.PongPacket;
import tw.zerator.ffs.pubsub.network.packet.outbound.ResponsePacket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.client.ClientProtocolException;

public class ClientConnection {
    private final Channel mChannel;
    private int mClientId;
    private final Server mServer;
    private final List<String> mSubscribedTopics = new CopyOnWriteArrayList<>();

    public ClientConnection(Server server, Channel channel) {
        this.mChannel = channel;
        mServer = server;
    }
    
    public List<String> getSubscribedTopics() {
    	return mSubscribedTopics;
    }
    
    private void subscribeToTopic(String topic) {
    	topic = topic.toLowerCase();
    	if (mSubscribedTopics.contains(topic)) return;
    	mSubscribedTopics.add(topic);
    	mServer.subscribeToTopic(topic, this);
    }
    
    private void unsubscribeFromTopic(String topic) {
    	topic = topic.toLowerCase();
    	mSubscribedTopics.remove(topic);
    	mServer.unsubscribeFromTopic(topic, this);
    }
    
    public int getClientId() {
    	return mClientId;
    }
    
    public SocketAddress getRemoteAddress() {
        return mChannel.remoteAddress();
    }

    public void sendPacket(Packet packet) {
        mChannel.writeAndFlush(packet);
    }
    
    public void handle(Packet packet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClientProtocolException, URISyntaxException, IOException, InterruptedException {
    	if (packet.type == Packet.Type.PING) sendPacket(new PongPacket());
    	else if (packet.type == Packet.Type.SUBSCRIBE) {
    		SubscribeUnsubscribePacket p = (SubscribeUnsubscribePacket) packet;
    		for (String topic : p.topics) {
    			if (!mServer.checkTopicExistence(topic)) {
    				sendPacket(new ResponsePacket(p.nonce, ResponsePacket.Error.ERR_BADTOPIC));
    				return;
    			}
    		}
    		
    		for (String topic : p.topics) subscribeToTopic(topic);
    		sendPacket(new ResponsePacket(p.nonce, null));
    	} else if (packet.type == Packet.Type.UNSUBSCRIBE) {
    		SubscribeUnsubscribePacket p = (SubscribeUnsubscribePacket) packet;
    		for (String topic : p.topics) {
    			if (!mServer.checkTopicExistence(topic)) {
    				sendPacket(new ResponsePacket(p.nonce, ResponsePacket.Error.ERR_BADTOPIC));
    				return;
    			}
    		}
    		
    		for (String topic : p.topics) unsubscribeFromTopic(topic);
    		sendPacket(new ResponsePacket(p.nonce, null));
    	}
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(mChannel);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClientConnection other = (ClientConnection) obj;
        if (!Objects.equals(mChannel, other.mChannel)) {
            return false;
        }
        return true;
    }

	public Channel getChannel() {
		return mChannel;
	}
}
