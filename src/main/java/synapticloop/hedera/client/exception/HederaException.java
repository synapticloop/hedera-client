package synapticloop.hedera.client.exception;

public class HederaException extends Exception {
	private static final long serialVersionUID = 844000590397055164L;

	public HederaException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public HederaException(String message) {
		super(message);
	}
}
