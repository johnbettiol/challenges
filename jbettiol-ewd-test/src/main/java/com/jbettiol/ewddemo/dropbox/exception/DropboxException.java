package com.jbettiol.ewddemo.dropbox.exception;

public class DropboxException extends RuntimeException {

	private static final long serialVersionUID = 676518396646538929L;

	public DropboxException(String message) {
        super(message);
    }

    public DropboxException(String message, Exception cause) {
        super(message, cause);
    }

}
