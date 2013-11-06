package es.gimix.util;

public class ResultOrException<T> {
	private Exception exception;
	private T result;

	public ResultOrException(Exception e) { this.exception = e;}
	public ResultOrException(T result) { this.result = result;}
	
	public T get() throws Exception {
		if(exception != null)
			throw exception;
		return result;
	}
}
