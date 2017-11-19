package tv.zerator.ffs.pubsub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import alexmog.apilib.managers.Managers.Manager;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import tw.zerator.ffs.pubsub.network.ClientConnection;
import tw.zerator.ffs.pubsub.network.NetworkManager;
import tw.zerator.ffs.pubsub.network.packet.outbound.MessagePacket;
import tw.zerator.ffs.pubsub.rabbitmq.RabbitMQConsumer;
import alexmog.apilib.managers.RabbitMQManager;

public class Server extends alexmog.apilib.Server {
	public static final int PROTOCOL_VERSION = 1;
	
	@Manager
	private static RabbitMQManager mRabbitMQManager;
	private Map<String, ChannelGroup> mTopicsSubscribers = new HashMap<>();
	private final NetworkManager mNetworkManager = new NetworkManager(this);
	private final List<Pattern> mValidTopicsRegex = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		new Server().start();
	}
	
	public boolean checkTopicExistence(String topic) {
		for (Pattern p : mValidTopicsRegex) if (p.matcher(topic).matches()) return true;
		return false;
	}
	
	public void subscribeToTopic(String topic, ClientConnection client) {
		synchronized(mTopicsSubscribers) {
			ChannelGroup group = mTopicsSubscribers.get(topic);
			if (group == null) {
				group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
				mTopicsSubscribers.put(topic, group);
			}
			group.add(client.getChannel());
		}
	}
	
	public void unsubscribeFromTopic(String topic, ClientConnection client) {
		synchronized(mTopicsSubscribers) {
			ChannelGroup group = mTopicsSubscribers.get(topic);
			if (group != null) group.remove(client.getChannel());
		}
	}

	public void start() throws Exception {
		super.start();
		String[] topicsRegex = getConfig().getProperty("topics.valid_topics").split(",");
		for (String topic : topicsRegex) mValidTopicsRegex.add(Pattern.compile(topic));
		initRabbitMQ();
		mNetworkManager.start();
	}
	
	private void initRabbitMQ() throws IOException {
		Server.LOGGER.info("Initialization of the Notifications channel");
		mRabbitMQManager.getChannel().addShutdownListener(new ShutdownListener() {
			
			@Override
			public void shutdownCompleted(ShutdownSignalException cause) {
				cause.printStackTrace();
			}
		});
		mRabbitMQManager.getChannel().exchangeDeclare("Pub", BuiltinExchangeType.FANOUT, true);
		String queueName = mRabbitMQManager.getChannel().queueDeclare().getQueue();
		mRabbitMQManager.getChannel().queueBind(queueName, "Pub", "");
		
		mRabbitMQManager.getChannel().basicConsume(queueName, true, new RabbitMQConsumer(this, mRabbitMQManager.getChannel()));
		Server.LOGGER.info("Initialization of the Pub channel done.");
	}

	public void sendToSubscribers(String topic, String message) {
		ChannelGroup connections = mTopicsSubscribers.get(topic);
		if (connections == null) return;
		try {
			connections.writeAndFlush(new MessagePacket(topic, message, System.currentTimeMillis()));
		} catch (Exception e) {}
	}
}
