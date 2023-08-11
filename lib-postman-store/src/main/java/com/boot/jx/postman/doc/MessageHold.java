package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.UpdatedTimeStampDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;

@Document(collection = MessageHold.COLLECTION_NAME)
@TypeAlias("MessageHold")
public class MessageHold extends UpdatedTimeStampDoc implements Serializable {

	private static final long serialVersionUID = -1916969779141145310L;

	public static final String COLLECTION_ORIGINAL = "MESSAGE_ORIGINAL";
	public static final String COLLECTION_NAME = "MESSAGE_HOLD";
	public static final String COLLECTION_REJECTED = "MESSAGE_REJECTED";
	public static final String COLLECTION_QUEUED = "MESSAGE_QUEUED";

	@Id
	private String tempId;

	@Indexed
	private String contactId;

	@Indexed
	private String sessionId;

	@Indexed
	private String appType;

	private long timestamp;

	private InboxMessage inboxMessage;

	private InBoundEvent event;

	private PMArgs pmArgs;

	public String getTempId() {
		return tempId;
	}

	public void setTempId(String tempId) {
		this.tempId = tempId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public InboxMessage getInboxMessage() {
		return inboxMessage;
	}

	public void setInboxMessage(InboxMessage inboxMessage) {
		this.inboxMessage = inboxMessage;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public InBoundEvent getEvent() {
		return event;
	}

	public void setEvent(InBoundEvent event) {
		this.event = event;
	}

	public PMArgs getPmArgs() {
		return pmArgs;
	}

	public void setPmArgs(PMArgs pmArgs) {
		this.pmArgs = pmArgs;
	}

	public MessageHold event(InBoundEvent event) {
		this.event = event;
		return this;
	}

	public MessageHold pmArgs(PMArgs pmArgs) {
		this.pmArgs = pmArgs;
		return this;
	}

	public MessageHold inboxMessage(InboxMessage inboxMessage) {
		this.inboxMessage = inboxMessage;
		return this;
	}
}
