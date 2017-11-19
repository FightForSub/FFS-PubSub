package tw.zerator.ffs.pubsub.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import alexmog.apilib.rabbitmq.RabbitMQDecoder;
import alexmog.apilib.rabbitmq.packets.Packet;

import com.rabbitmq.client.AMQP.BasicProperties;

import io.netty.buffer.Unpooled;
import tv.zerator.ffs.pubsub.Server;

public class RabbitMQConsumer extends DefaultConsumer {
	private final RabbitMQDecoder mDecoder;
	private final Server mServer;
	
	public RabbitMQConsumer(Server server, Channel channel) {
		super(channel);
		mDecoder = new RabbitMQDecoder();
		mDecoder.registerPacket(new TopicPacket().packetId, TopicPacket.class);
		mServer = server;
	}
	
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		Packet p = mDecoder.decode(Unpooled.wrappedBuffer(body));
		if (p != null) {
			TopicPacket packet = (TopicPacket) p;
			mServer.sendToSubscribers(packet.topic, packet.message);
		}
	}
}
