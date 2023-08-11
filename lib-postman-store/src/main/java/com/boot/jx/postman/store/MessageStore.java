package com.boot.jx.postman.store;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.ContactDetailDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.MessageHold;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.TimeUtils;
import com.google.common.collect.Lists;
import com.mongodb.WriteResult;

@Component
public class MessageStore extends CommonMongoTemplateAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageStore.class);

	public static enum EVENTS {
		ASGND_TO_DEPT, ASGND_TO_AGENT, ASGND_TO_QUEUE, UNASGND, PICKED_BY_AGENT, CLOSED_BY_AGENT, LABEL_ADDED,
		LABEL_REMOVED, STATUS_CHANGED, TAG_ADDED, TAG_REMOVED,

		// OTHER ERROS
		INBOUND_FORWARD_ERROR,

		// ENDS
		DEFAULT;
	}

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@Value("${postman.chat.session.timeout}")
	String chatSessionTimeout;

	@Autowired
	AppConfig appConfig;

	public static String getCollectionName(Object contactType) {
		return (MessageDoc.COLLECTION_NAME + "_" + ArgUtil.parseAsString(contactType, "OTHERS"));
	}

	private MessageDoc updateMessageDoc(InboxMessage inboxMessage, MessageDoc doc) {
		doc.setSubject(inboxMessage.getSubject());
		doc.setMessage(inboxMessage.getMessage());
		doc.setSessionId(inboxMessage.getSessionId());

		doc.setRoute(inboxMessage.getRoute());
		doc.setQueue(ArgUtil.nonEmpty(inboxMessage.route().getQueueCode(), inboxMessage.session().getQueue()));

		doc.setTags(inboxMessage.getTags());
		doc.setMessageIdExt(inboxMessage.getMessageIdExt());
		doc.setReplyTo(inboxMessage.getReplyTo());

		if (ArgUtil.is(inboxMessage.getReplyId())) {
			doc.setReplyId(inboxMessage.getReplyId());
		}

		doc.setReplyIdExt(inboxMessage.getReplyIdExt());

		// Additonals
		doc.setAttachments(inboxMessage.getAttachments());
		doc.setVccards(inboxMessage.getVccards());

		doc.form().putAll(inboxMessage.form());
		doc.stamps().put("session", ArgUtil.parseAsLong(inboxMessage.session().getSessionStamp(), 0L));

		return doc;
	}

	private MessageDoc createMessageDoc(InboxMessage inboxMessage) {
		ContactType contactType = inboxMessage.contact().type();
		MessageDoc doc = MessageDoc.instance(contactType);
		doc.setTraceId(AppContextUtil.getTraceId());
		doc.setContactId(PostManUtil.createContactId(inboxMessage));
		doc.setType("I");
		doc.setTimestamp(System.currentTimeMillis());

		doc.setFormatType(inboxMessage.getFormatType());
		doc.setFormatSubType(inboxMessage.getFormatSubType());

		ContactDetailDoc contact = new ContactDetailDoc();
		contact.setPhone(inboxMessage.getFrom());
		contact.setContactType(ArgUtil.parseAsString(contactType));
		doc.setContact(contact);

		updateMessageDoc(inboxMessage, doc);

		return doc;
	}

	public MessageDoc findByMessageId(String messageId, Object contactType) {
		return mongoTemplate.findById(messageId, MessageDoc.class, getCollectionName(contactType));
	}

	public MessageDoc findOneByMessageIdExt(String messageIdExt, String contactType) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("messageIdExt").is(messageIdExt)).with(new Sort(Direction.ASC, "timestamp"));
		MessageDoc messages = mongoTemplate.findOne(query2, MessageDoc.class, getCollectionName(contactType));
		return messages;
	}

	public List<MessageDoc> findAllByMessageIdExt(String messageIdExt, String contactType) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("messageIdExt").is(messageIdExt)).with(new Sort(Direction.ASC, "timestamp"));
		return mongoTemplate.find(query2, MessageDoc.class, getCollectionName(contactType));
	}

	private MessageDoc findMessageDoc(InboxMessage inboxMessage) {
		if (ArgUtil.is(inboxMessage.getMessageId())) {
			return mongoTemplate.findById(inboxMessage.getMessageId(), MessageDoc.class,
					getCollectionName(inboxMessage.contact().type()));
		} else if (ArgUtil.is(inboxMessage.getMessageIdExt())) {
			CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();
			builder.where("messageIdExt", inboxMessage.getMessageIdExt());
			return mongoTemplate.findOne(builder.getQuery(), MessageDoc.class,
					getCollectionName(inboxMessage.contact().type()));
		} else {
			return null;
		}
	}

	public MessageDoc findOrCreateMessageDoc(InboxMessage inboxMessage) {
		MessageDoc doc = findMessageDoc(inboxMessage);
		if (!ArgUtil.is(doc)) {
			doc = createMessageDoc(inboxMessage);
			if (ArgUtil.is(doc) && ArgUtil.is(doc.getReplyIdExt()) && !ArgUtil.is(doc.getReplyId())) {
				MessageDoc replyTo = findOneByMessageIdExt(doc.getReplyIdExt(),
						inboxMessage.contact().getContactType());
				if (ArgUtil.is(replyTo) && ArgUtil.is(replyTo.getMessageId())) {
					doc.setReplyId(replyTo.getMessageId());
				}
			}
		}
		return doc;
	}

	public MessageDoc findAndUpdateMessageDoc(InboxMessage inboxMessage) {
		MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
		if (ArgUtil.is(doc.getMessageId())) {
			doc = updateMessageDoc(inboxMessage, doc);
		}
		mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
		inboxMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc createOrUpdate(InboxMessage inboxMessage) {
		MessageDoc doc = findAndUpdateMessageDoc(inboxMessage);
		inboxMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public void setTemplate(InboxMessage inboxMessage, String template) {
		MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
		doc.setTemplate(template);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
		inboxMessage.setMessageId(doc.getMessageId());
	}

	public void setTags(InboxMessage inboxMessage, TagDocument tags) {
		MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
		doc.setTags(tags);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
		inboxMessage.setMessageId(doc.getMessageId());
	}

	public void setHandler(InboxMessage inboxMessage, String handler) {
		MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
		doc.setHandler(handler);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
		inboxMessage.setMessageId(doc.getMessageId());
	}

	// Out Going Messages
	private MessageDoc updateMessageDoc(OutboxMessage outMessage, MessageDoc doc) {

		doc.setRoute(outMessage.getRoute());
		doc.setQueue(ArgUtil.nonEmpty(outMessage.route().getQueueCode(), outMessage.session().getQueue()));
		doc.setAgent(ArgUtil.nonEmpty(outMessage.route().getSenderCode(), outMessage.session().getAgent()));
		// if (ArgUtil.is(outMessage.getTemplate())) {
		doc.setTemplate(outMessage.templateCode());
		doc.setTemplateId(outMessage.templateId());
		doc.setHsm(outMessage.getHsm());
		doc.setModel(outMessage.getModel());
		// } else {
		doc.setSubject(outMessage.getSubject());
		doc.setMessage(outMessage.getMessage());
		// }
		doc.setAttachments(outMessage.getAttachments());
		doc.setVccards(outMessage.getVccards());

		doc.setSessionId(outMessage.getSessionId());
		doc.setMessageIdRef(outMessage.getMessageIdRef());

		doc.setLogs(outMessage.getLogs());
		doc.setMessageIdExt(outMessage.getMessageIdExt());
		doc.setStatus(ArgUtil.parseAsString(outMessage.getStatus()));

		doc.stamps().putAll(outMessage.stamps());
		doc.stamps().put("session", ArgUtil.parseAsLong(outMessage.session().getSessionStamp(), 0L));
		doc.meta().putAll(outMessage.meta());
		doc.options().putAll(outMessage.options());

		return doc;
	}

	public MessageDoc createMessageDoc(OutboxMessage outMessage) {
		ContactType contactType = outMessage.contact().type();
		MessageDoc doc = MessageDoc.instance(contactType);
		doc.setTraceId(AppContextUtil.getTraceId());

		if (ArgUtil.is(outMessage.getAction())) {
			doc.setType(ArgUtil.nonEmpty(outMessage.getType(), "A"));
			doc.setAction(outMessage.getAction());
		} else {
			doc.setType(ArgUtil.nonEmpty(outMessage.getType(), "O"));
		}
		doc.setTimestamp(System.currentTimeMillis());

		String to = CollectionUtil.getOne(outMessage.getTo());
		doc.setContactId(PostManUtil.createContactId(outMessage));

		ContactDetailDoc contact = new ContactDetailDoc();
		contact.setPhone(to);
		contact.setMobile(to);
		contact.setContactType(ArgUtil.parseAsString(outMessage.contact().getContactType()));
		doc.setContact(contact);

		updateMessageDoc(outMessage, doc);
		return doc;
	}

	public MessageDoc findMessageDoc(OutboxMessage outMessage) {
		if (ArgUtil.is(outMessage.getMessageId())) {
			return mongoTemplate.findById(outMessage.getMessageId(), MessageDoc.class,
					getCollectionName(outMessage.contact().type()));
		}
		return null;
	}

	private MessageDoc findOrCreateMessageDoc(OutboxMessage outMessage) {
		MessageDoc doc = findMessageDoc(outMessage);
		if (!ArgUtil.is(doc)) {
			doc = createMessageDoc(outMessage);
		}
		return doc;
	}

	public MessageDoc findAndUpdateMessageDoc(OutboxMessage outMessage) {
		MessageDoc doc = findOrCreateMessageDoc(outMessage);
		if (ArgUtil.is(doc.getMessageId())) {
			doc = updateMessageDoc(outMessage, doc);
		}
		mongoTemplate.save(doc, getCollectionName(outMessage.contact().type()));
		outMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc createOrUpdate(OutboxMessage outMessage) {
		MessageDoc doc = findAndUpdateMessageDoc(outMessage);
		outMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc note(OutboxMessage outboxMessage, String agent) {
		MessageDoc doc = createMessageDoc(outboxMessage);
		doc.setType("N");
		doc.setAgent(agent);
		mongoTemplate.save(doc, getCollectionName(outboxMessage.contact().type()));
		return doc;
	}

	public List<MessageDoc> findBySessionId(String sessionId, String contactType) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("sessionId").is(sessionId)).with(new Sort(Direction.ASC, "timestamp"));
		List<MessageDoc> messages = mongoTemplate.find(query2, MessageDoc.class, getCollectionName(contactType));
		return messages;
	}

	public List<MessageDoc> findByBulkSessionId(String bulkSessionId, ContactType contactType) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("bulkSessionId").is(bulkSessionId))
				.with(new Sort(Direction.ASC, "timestamp"));
		List<MessageDoc> messages = mongoTemplate.find(query2, MessageDoc.class, getCollectionName(contactType));
		return messages;
	}

	public void updateStatus(ContactType contactType, MessageDoc messageDoc, Status status, String reason) {
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

		if (ArgUtil.is(messageDoc.getMessageId())) {
			builder.whereIdSafe(messageDoc.getMessageId());
		} else if (ArgUtil.is(messageDoc.getMessageIdExt())) {
			builder.where("messageIdExt", messageDoc.getMessageIdExt());
		} else if (ArgUtil.is(messageDoc.getMessageIdRef())) {
			builder.where("messageIdRef", messageDoc.getMessageIdRef());
		} else {
			return;
		}

		builder.set("status", status);
		builder.set("stamps." + status.toString(), System.currentTimeMillis());
		if (ArgUtil.is(reason)) {
			builder.update().push("logs", reason);
		}
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
				getCollectionName(contactType));
	}

	public void updateStatus(MessageReport messageReport) {

		LOGGER.debug("updateStatus {} {} {}", messageReport.getMessageId(), messageReport.contact().getChannelType(),
				messageReport.getStatus());

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

		boolean multi = false;
		if (ArgUtil.is(messageReport.getMessageId())) {
			builder.whereIdSafe(messageReport.getMessageId());
		} else if (ArgUtil.is(messageReport.getMessageIdExt())) {
			builder.where("messageIdExt", messageReport.getMessageIdExt());
		} else if (ArgUtil.is(messageReport.getMessageIdRef())) {
			builder.where("messageIdRef", messageReport.getMessageIdRef());
		} else if (ArgUtil.is(messageReport.contact().getCsid()) && messageReport.getWatermarkStamp() > 0L) {
			String contactId = PostManUtil.CONTACT_ID(messageReport.contact());
			builder.where(
					// Main Condition
					Criteria.where("contactId").is(contactId).and("stamps." + messageReport.getStatus().toString())
							.exists(false).andOperator(
									// Range
									Criteria.where("timestamp").lt(messageReport.getWatermarkStamp()),
									Criteria.where("timestamp").gt(TimeUtils.beforeTimeMillis("24hr"))));
			multi = true;
		} else {
			return;
		}

		if (ArgUtil.is(messageReport.getStatus())) {
			builder.set("status", messageReport.getStatus());
			builder.set("stamps." + messageReport.getStatus().toString(), messageReport.getChangeStamp());

			if (ArgUtil.is(messageReport.getReason())) {
				builder.update().push("logs", messageReport.getReason());
			}

			if (ArgUtil.is(messageReport.getStatus()) && messageReport.getStatus() == Status.DELTD) {
				builder.set("message", null);
			}

			String collectionName = getCollectionName(messageReport.contact().getContactType());

			WriteResult result;

			if (multi) {
				result = mongoTemplate.updateMulti(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
						collectionName);
			} else {
				result = mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
						collectionName);
			}

			if (result.getN() > 1) {
				builder.limit(result.getN());
				List<MessageDoc> messsages = mongoTemplate.find(builder.getQuery(), MessageDoc.class, collectionName);
				if (ArgUtil.is(messsages) && ArgUtil.is(messsages.get(0))) {
					updateMessageReport(messageReport, messsages.get(0));
				}
			} else {
				MessageDoc m = mongoTemplate.findOne(builder.getQuery(), MessageDoc.class, collectionName);
				if (ArgUtil.is(m)) {
					updateMessageReport(messageReport, m);
				}
			}

			// LOGGER.info(JsonUtil.toJson(builder));
		}
	}

	private void updateMessageReport(MessageReport messageReport, MessageDoc m) {
		messageReport.from(m);
		messageReport.session().setQueue(m.getQueue());
	}

	public void insert(List<MessageDoc> messages, ContactType contactType) {
		/**
		 * for (MessageDoc messageDoc : messages) { mongoTemplate.save(messageDoc,
		 * MessageStore.getCollectionName(contactType)); //System.out.println("phone:
		 * "+messageDoc.getContact().getPhone()); } return;
		 **/
		int n = 500;
		// Calculate the total number of partitions of size `n` each
		int m = messages.size() / n;
		if (messages.size() % n != 0) {
			m++;
		}
		// partition the list into sublists of size `n` each
		List<List<MessageDoc>> itr = Lists.partition(messages, n);
		for (int i = 0; i < m; i++) {
			mongoTemplate.insert(itr.get(i), MessageStore.getCollectionName(contactType));
		}

	}

	public List<MessageDoc> find(Query query, ContactType contactType) {
		return mongoTemplate.find(query, MessageDoc.class, MessageStore.getCollectionName(contactType));
	}

	public MessageDoc save(MessageDoc msg, ContactType contactType) {
		mongoTemplate.save(msg, MessageStore.getCollectionName(contactType));
		return msg;
	}

	public void reject(InboxMessage inboxMessageOriginal) {
		String contactId = PostManUtil.CONTACT_ID(inboxMessageOriginal.contact());
		MessageHold hold = new MessageHold();
		hold.setInboxMessage(inboxMessageOriginal);
		hold.setContactId(contactId);
		hold.setTimestamp(System.currentTimeMillis());
		hold.setAppType(appConfig.getAppType());
		mongoTemplate.save(hold, MessageHold.COLLECTION_REJECTED);
	}

	public void original(InboxMessage inboxMessageOriginal) {
		String contactId = PostManUtil.CONTACT_ID(inboxMessageOriginal.contact());
		MessageHold hold = new MessageHold();
		hold.setInboxMessage(inboxMessageOriginal);
		hold.setContactId(contactId);
		hold.setTimestamp(System.currentTimeMillis());
		hold.setAppType(appConfig.getAppType());
		commonMongoTemplate.save(hold, MessageHold.COLLECTION_ORIGINAL);
	}

	public void hold(InboxMessage inboxMessageOriginal) {
		String contactId = PostManUtil.CONTACT_ID(inboxMessageOriginal.contact());
		MessageHold hold = new MessageHold();
		hold.setInboxMessage(inboxMessageOriginal);
		hold.setContactId(contactId);
		hold.setTimestamp(System.currentTimeMillis());
		hold.setAppType(appConfig.getAppType());
		mongoTemplate.save(hold);
	}

	public List<InboxMessage> releaseBySession(InboxMessage inboxMessageOriginal) {
		String contactId = PostManUtil.CONTACT_ID(inboxMessageOriginal.contact());
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();
		builder.where(Criteria.where("contactId").is(contactId).and("appType").is(appConfig.getAppType()));
		builder.set("sessionId", inboxMessageOriginal.getSessionId());
		mongoTemplate.updateMulti(builder.getQuery(), builder.getUpdate(), MessageHold.class);

		CommonMongoQueryBuilder builder2 = new CommonMongoQueryBuilder();
		builder2.where(Criteria.where("contactId").is(contactId).and("sessionId").is(inboxMessageOriginal.getSessionId())
				.and("appType").is(appConfig.getAppType())).sortBy("timestamp");;
		List<MessageHold> docs = mongoTemplate.findAllAndRemove(builder2.getQuery(), MessageHold.class);
		List<InboxMessage> x = docs.stream().map(d -> d.getInboxMessage()).collect(Collectors.toList());
		return x;
	}

}
