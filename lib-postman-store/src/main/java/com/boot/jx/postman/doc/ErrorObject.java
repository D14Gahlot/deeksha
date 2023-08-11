package com.boot.jx.postman.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CHAT_ERRORS")
@TypeAlias("ErrorObject")
public class ErrorObject {

	@Id
	private String errorId;

	private String errorType;

	private Long timestamp;

	private String message;

	private Object incomingMessage;

	public Object getIncomingMessage() {
		return incomingMessage;
	}

	public void setIncomingMessage(Object incomingMessage) {
		this.incomingMessage = incomingMessage;
	}

	public String getErrorId() {
		return errorId;
	}

	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

}
