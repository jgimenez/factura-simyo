package es.gimix.simyo.api;

/** The provided credentials were invalid. The message coming from the server is the message attribute */
public class SimyoCredentialsException extends SimyoAPIException {
	private static final long serialVersionUID = 1L;

	public SimyoCredentialsException(String message) {
		super(message);
	}
}
