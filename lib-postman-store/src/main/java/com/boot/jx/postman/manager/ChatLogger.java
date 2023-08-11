package com.boot.jx.postman.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpServerException;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.MessageDoc.MessageDocLogs;
import com.boot.jx.postman.doc.MessageDocAbstract;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.MessageDefinitions.LogMessage;
import com.boot.jx.postman.model.MessageDefinitions.LoggableEntity;
import com.boot.jx.postman.model.MessageDefinitions.SessionMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
public class ChatLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatLogger.class);

	@Autowired(required = false)
	private AuditDetailProvider auditDetailProvider;

	public String getCurrenUser() {
		return ArgUtil.is(auditDetailProvider) ? auditDetailProvider.getAuditUser() : "_SYSTEM_";
	}

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private MessageContext messageContext;

	public MessageDoc note(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		outboxMessage.contact().setContactType(sessionDoc.getContactType());
		outboxMessage.contact().setChannelType(sessionDoc.getChannel());
		outboxMessage.contact().setLane(sessionDoc.getLane());
		outboxMessage.contact().setContactId(sessionDoc.getContactId());
		outboxMessage.contact().copyFrom(sessionDoc.getContact());

		outboxMessage.setSessionId(sessionDoc.getSessionId());
		outboxMessage.setType("N");
		return messageStore.note(outboxMessage, getCurrenUser());
	}

	public MessageDoc event(SessionMessage inboxMessage, String actorAgent, EVENTS eventName, String... logMessage) {
		MessageDoc doc = new MessageDoc();
		doc.setContactId(PostManUtil.createContactId(inboxMessage.contact()));
		doc.setType("L");
		doc.setTimestamp(System.currentTimeMillis());
		if (ArgUtil.is(logMessage)) {
			for (String string : logMessage) {
				doc.logs().add(string);
			}
		}
		doc.setAction(ArgUtil.parseAsString(eventName));
		doc.setSessionId(inboxMessage.getSessionId());
		doc.setAgent(ArgUtil.nonEmpty(actorAgent, AppContextUtil.getActorId()));
		messageStore.save(doc, inboxMessage.contact().type());
		return doc;
	}

	public MessageDoc event(SessionMessage inboxMessage, EVENTS event, String... logs) {
		return event(inboxMessage, inboxMessage.session().getAgent(), event, logs);
	}

	public MessageDoc event(ChatSessionDoc sessionDoc, String auditAgent, EVENTS event, String... logs) {
		IMessageExtended inboxMessage = sessionStore.toSessionMessage(sessionDoc);
		return event(inboxMessage, auditAgent, event, logs);
	}

	public MessageDoc event(ChatSessionDoc sessionDoc, EVENTS event, String... logs) {
		return event(sessionDoc, getCurrenUser(), event, logs);
	}

	public void error(LogMessage inboxMessage, Throwable e) {
		MessageDocLogs doc = new MessageDocLogs();
		doc.setSessionId(inboxMessage.getSessionId());
		doc.setMessageId(inboxMessage.getMessageId());
		doc.setMessageIdExt(inboxMessage.getMessageIdExt());
		doc.setMessageIdRef(inboxMessage.getMessageIdRef());
		doc.setContactId(PostManUtil.createContactId(inboxMessage.contact()));
		doc.setType("E");
		doc.setTimestamp(System.currentTimeMillis());
		doc.setTraceId(AppContextUtil.getTraceId());
		doc.setMessage(e.getMessage());

		toLogs(e, doc);

		messageStore.save(doc);
		inboxMessage.logs().add(e.getMessage());
		inboxMessage.logs().add("trail:" + doc.getMessageId());
	}

	public void error(InBoundEvent inBoundEvent, Throwable e) {
		MessageDocLogs doc = new MessageDocLogs();
		doc.setSessionId(inBoundEvent.sessionId);
		doc.setContactId(inBoundEvent.contactId);
		doc.setType("E");
		doc.setTimestamp(System.currentTimeMillis());
		doc.setTraceId(AppContextUtil.getTraceId());
		doc.setMessage(e.getMessage());

		toLogs(e, doc);
		messageStore.save(doc);
	}

	private void toLogs(Throwable e, MessageDocLogs doc) {
		StackTraceElement[] traces = e.getStackTrace();

		if (traces.length > 0 && traces[0].toString().length() > 0) {
			for (StackTraceElement trace : traces) {
				doc.logs().add(trace.toString());
			}
		}

		if (e instanceof ApiHttpServerException || e instanceof ApiHttpException) {
			doc.setHttpResp(MapModel.from(((ApiHttpException) e).getResponse().getBody()).toMap());
		}

	}

	public void error(Throwable e) {
		if (ArgUtil.is(messageContext.getMessage())) {
			this.error(messageContext.getMessage(), e);
		} else if (ArgUtil.is(messageContext.getInBoundEvent())) {
			this.error(messageContext.getInBoundEvent(), e);
		} else {
			MessageDocLogs doc = new MessageDocLogs();
			doc.setType("E");
			doc.setTimestamp(System.currentTimeMillis());
			doc.setTraceId(AppContextUtil.getTraceId());
			doc.setMessage(e.getMessage());
			messageStore.save(doc);
		}
	}

	private void log(String type, MessageDocAbstract doc, String message, Object[] debugMessage) {
		doc.setTimestamp(System.currentTimeMillis());
		doc.setTraceId(AppContextUtil.getTraceId());
		doc.setMessage(message);
		if (ArgUtil.is(debugMessage)) {
			for (int i = 0; i < debugMessage.length; i++) {
				doc.logs().add(ArgUtil.parseAsString(debugMessage[i]));
			}
		}
		messageStore.save(doc);
	}

	private MessageDocAbstract messageDoc(LoggableEntity inBoundEvent) {
		MessageDocLogs doc = new MessageDocLogs();
		if (ArgUtil.is(inBoundEvent)) {
			doc.setSessionId(inBoundEvent.getSessionId());
			doc.setContactId(inBoundEvent.getContactId());
		}
		return doc;
	}

	private MessageDocAbstract messageDoc(LogMessage message) {
		MessageDocAbstract doc = new MessageDocLogs();
		if (ArgUtil.is(message)) {
			doc.setSessionId(message.getSessionId());
			doc.setMessageId(message.getMessageId());
			doc.setMessageIdExt(message.getMessageIdExt());
			doc.setMessageIdRef(message.getMessageIdRef());
			doc.setContactId(PostManUtil.createContactId(message.contact()));
		}
		return doc;
	}

	public void debug(String message, Object... debugMessage) {
		if (!LOGGER.isDebugEnabled())
			return;
		if (ArgUtil.is(messageContext.getMessage())) {
			this.log("D", messageDoc(messageContext.getMessage()), message, debugMessage);
		} else if (ArgUtil.is(messageContext.getInBoundEvent())) {
			this.log("D", messageDoc(messageContext.getInBoundEvent()), message, debugMessage);
		} else {
			this.log("D", messageDoc(new InBoundEvent()), message, debugMessage);
		}
	}

	public void debug(InBoundEvent assignEvent, String message, Object... debugMessage) {
		if (!LOGGER.isDebugEnabled())
			return;
		this.log("D", messageDoc(assignEvent), message, debugMessage);
	}

	public void warn(String message, Object... debugMessage) {
		if (!LOGGER.isWarnEnabled())
			return;
		if (ArgUtil.is(messageContext.getMessage())) {
			this.log("W", messageDoc(messageContext.getMessage()), message, debugMessage);
		} else if (ArgUtil.is(messageContext.getInBoundEvent())) {
			this.log("W", messageDoc(messageContext.getInBoundEvent()), message, debugMessage);
		} else {
			this.log("W", messageDoc(new InBoundEvent()), message, debugMessage);
		}
	}

}
