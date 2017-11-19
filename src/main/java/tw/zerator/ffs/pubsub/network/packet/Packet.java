package tw.zerator.ffs.pubsub.network.packet;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Packet {
	public Type type;
	
	public enum Type {
		SUBSCRIBE,
		UNSUBSCRIBE,
		MESSAGE,
		RESPONSE,
		PING,
		PONG
	}
}
