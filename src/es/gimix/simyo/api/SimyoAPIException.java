package es.gimix.simyo.api;

/** Serves as base class for other exceptions coming from the API */
public class SimyoAPIException extends Exception {
	private static final long serialVersionUID = 1L;

	SimyoAPIException(String message) {
		super(message);
	}
}
