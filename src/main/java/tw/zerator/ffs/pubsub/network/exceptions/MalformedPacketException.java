package tw.zerator.ffs.pubsub.network.exceptions;

/**
 * Thrown when a packet is malformed.
 */
@SuppressWarnings("serial")
public class MalformedPacketException extends RuntimeException {

    public MalformedPacketException() {
    }

    public MalformedPacketException(String message) {
        super(message);
    }

    public MalformedPacketException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedPacketException(Throwable cause) {
        super(cause);
    }

    public MalformedPacketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
