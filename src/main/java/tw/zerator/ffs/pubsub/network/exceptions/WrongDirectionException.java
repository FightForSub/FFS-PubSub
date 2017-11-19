package tw.zerator.ffs.pubsub.network.exceptions;

/**
 * Thrown when attempting to read/write packet when the direction is not
 * applicable for said packet.
 */
@SuppressWarnings("serial")
public class WrongDirectionException extends RuntimeException {

}
