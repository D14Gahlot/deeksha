package com.boot.jx.postman.store;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMConstants.DEFAULT_VALUES;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ChatUserProfileDoc;
import com.boot.jx.postman.doc.ContactDetailDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.MessageDefinitions.SessionInfo;
import com.boot.jx.postman.model.MessageDefinitions.SessionMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.TimeUtils;

@Component
public class SessionStore extends CommonMongoTemplateAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionStore.class);

	@Autowired
	public PMClientConfig pmClientConfig;

	@Autowired
	private MessageContext messageContext;

	@Autowired
	private PMDomainConfig pmDomainConfig;

	public ChatContactDoc getContact(IMessage inboxMessage) {
		String contactId = PostManUtil.createContactId(inboxMessage);
		ChatContactDoc chatContactDoc = super.findById(contactId, ChatContactDoc.class);
		return chatContactDoc;
	}

	public ChatContactDoc getContact(String contactId) {
		return super.findById(contactId, ChatContactDoc.class);
	}

	public ChatContactDoc save(ChatContactDoc chatContactDoc) {
		super.save(chatContactDoc);
		return chatContactDoc;
	}

	public ChatSessionDoc getSession(String sessionId) {
		return super.findById(sessionId, ChatSessionDoc.class);
	}

	public ChatSessionDoc getSessionPrimeByTicketHash(String contactId, String ticketHash) {
		CommonMongoQueryBuilder cmqb = new CommonMongoQueryBuilder().where(
				Criteria.where("contactId").is(contactId).and("ticketHash").is(ticketHash).and("primary").is(true));
		return super.findOne(cmqb.getQuery(), ChatSessionDoc.class);
	}

	public boolean isSessionValid(ChatSessionDoc chatSessionDoc) {
		if ((ArgUtil.isEmpty(chatSessionDoc) || !chatSessionDoc.isActive()) || chatSessionDoc.isExpired()) {
			return false;
		}

		chatSessionDoc.refreshStamps();

		if (!ArgUtil.isEmptyValue(chatSessionDoc.getLastInComingStamp())
				&& (chatSessionDoc.getLastResponseStamp() > chatSessionDoc.getLastInComingStamp())) {
			return !TimeUtils.isExpired(chatSessionDoc.getLastInComingStamp(), pmClientConfig.getChatSessionTimeout());
		}

		if (ArgUtil.is(chatSessionDoc.getUpdated())) {
			return !TimeUtils.isExpired(chatSessionDoc.getUpdated().getStamp(), pmClientConfig.getChatSessionTimeout());
		}

		return true;
	}

	public ChatSessionDoc getValidSession(String sessionId) {
		ChatSessionDoc chatSessionDoc = getSession(sessionId);
		if (isSessionValid(chatSessionDoc)) {
			return chatSessionDoc;
		}
		return null;
	}

	/**
	 * 
	 * This method will take messages and returns session, session sbhould be
	 * created if there is not present session against this message or return if its
	 * there, this method should return null only in case there is nothing can be
	 * done for message.
	 * 
	 * Additionally this message is responsible for updating ContactDoc and Session
	 * doc for stamps and entry points
	 * 
	 * @param sessionMessage
	 * @return
	 */
	@Deprecated
	public ChatSessionDoc createSession(SessionMessage sessionMessage) {
		return createSessionOld(sessionMessage);
	}

	@Deprecated
	public ChatSessionDoc createSessionOld(SessionMessage sessionMessage) {
		Contactable contact = PostManUtil.getContactMeta(sessionMessage.contact());

		String sessionId = sessionMessage.getSessionId();
		String contactId = contact.getContactId();

		ChatSessionDoc chatSessionDoc = null;
		ChatContactDoc chatContactDoc = null;

		if (ArgUtil.isEmpty(contactId)) {
			if (ArgUtil.isEmpty(sessionId)) {
				// If these conact & session are not present there is nothing we can do about
				// this message
				return null;
			}
			chatSessionDoc = getSession(sessionId);

			if (ArgUtil.isEmpty(chatSessionDoc)) {
				// Session Not found
				return null;
			}

			if (!isSessionValid(chatSessionDoc)) {
				// Session Found but invalid
				contactId = chatSessionDoc.getContactId();
				chatContactDoc = super.findById(contactId, ChatContactDoc.class);
				contact.copyFrom(chatContactDoc);
			}

		}

		// Find Out Chat Session
		if (ArgUtil.isEmpty(chatSessionDoc)) {
			if (ArgUtil.isEmpty(sessionId)) {
				chatContactDoc = super.findById(contactId, ChatContactDoc.class);
				if (ArgUtil.is(chatContactDoc)) {
					sessionId = chatContactDoc.getSessionId();
				}
			}
			if (ArgUtil.is(sessionId)) {
				chatSessionDoc = getValidSession(sessionId);
			}
		}

		ChatContactQuery chatContactQuery = ArgUtil.is(chatContactDoc) ? new ChatContactQuery(chatContactDoc)
				: new ChatContactQuery(contactId);

		if (!isSessionValid(chatSessionDoc)) {

			closeAllPreviousSessions(contactId);

			if (!ArgUtil.is(chatContactDoc)) {
				chatContactDoc = super.findById(contactId, ChatContactDoc.class);
			}

			// SESSION CREATION
			chatSessionDoc = new ChatSessionDoc();
			chatSessionDoc.setContactId(contactId);
			chatSessionDoc.setContactType(sessionMessage.contact().getContactType());
			chatSessionDoc.setChannel(sessionMessage.contact().getChannelType());
			chatSessionDoc.setLane(sessionMessage.contact().getLane());

			// SESSION UPDATE
			chatSessionDoc.setActive(true);
			chatSessionDoc.setPrimary(true);

			if (ArgUtil.is(chatContactDoc)) {
				chatSessionDoc.setContact(new ContactDetailDoc());
				if (ArgUtil.is(chatContactDoc.getName())) {
					chatSessionDoc.contact().setName(chatContactDoc.getName());
				}
				chatSessionDoc.getContact().copyFrom(chatContactDoc);
			}

			saveSession(chatSessionDoc);
			chatContactQuery.setSessionId(chatSessionDoc.getSessionId());

			chatContactQuery.update(contact);
			chatContactQuery.updateCreatedStamp();
			// CONTACT CREATION - needs creation or updation if
			if (ArgUtil.isEmpty(chatContactDoc)) {
				upsert(chatContactQuery);
			} else {
				// CONTACT UPDATE
				updateFirst(chatContactQuery);
			}
		} else {
			// SESSION UPDATE
			ChatSessionQuery chatSessionDocQuery = new ChatSessionQuery(chatSessionDoc);
			chatSessionDocQuery.setActive(true);
			if (!ArgUtil.is(chatSessionDoc.getContactName())) {
				chatSessionDocQuery.setContactName(chatSessionDoc.getContactName());
			}
			updateFirst(chatSessionDocQuery);
		}
		return chatSessionDoc;
	}

	public SessionInfo updateMessageFromSession(ChatSessionDoc chatSessionDoc, SessionInfo iMessage) {
		if (ArgUtil.isEmpty(iMessage.contact().getName())) {
			iMessage.contact().setName(chatSessionDoc.contact().getName());
		}
		iMessage.contact().setContactId(chatSessionDoc.getContactId());
		iMessage.setSessionId(chatSessionDoc.getSessionId());
		iMessage.session().setQueue(chatSessionDoc.getAssignedToQueue());
		iMessage.session().setAgent(chatSessionDoc.getAssignedToAgent());
		iMessage.session().setDept(chatSessionDoc.getAssignedToDept());
		iMessage.session().setMode(chatSessionDoc.getMode());
		iMessage.session().setResolved(chatSessionDoc.isResolved());
		iMessage.session().setRoutingId(chatSessionDoc.getRoutingId());

		iMessage.session().setSessionStamp(chatSessionDoc.getUpdated().getStamp());
		return iMessage;
	}

	@Deprecated
	public ChatSessionDoc linkSession(ChatSessionDoc chatSessionDoc, IMessage iMessage) {
		if (!ArgUtil.is(chatSessionDoc)) {
			return null;
		}

		if (PostManUtil.isInBound(iMessage)) {
			chatSessionDoc.setLastInComingStamp(iMessage.getTimestamp());
			// Query Update for Session
			ChatSessionQuery chatSessionDocQuery = new ChatSessionQuery(chatSessionDoc);
			chatSessionDocQuery.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());

			if (ArgUtil.isEmptyValue(chatSessionDoc.getFirstInComingStamp())) {
				chatSessionDocQuery.setFirstInComingStamp(iMessage.getTimestamp());
			}

			// Assign Queue
			if (ArgUtil.isEmptyValue(chatSessionDoc.getAssignedToQueue())) {

				String defaultQueue = iMessage.route().getQueueCode();
				if (!ArgUtil.is(defaultQueue)) {
					defaultQueue = pmDomainConfig.getDefaultInboundQueue(iMessage.contact());
				}
				if (ArgUtil.is(defaultQueue)) {
					chatSessionDocQuery.setQueue(defaultQueue);
				}
			}

			updateFirst(chatSessionDocQuery);

			// Query Update for Contact
			ChatContactQuery chatContactQuery = new ChatContactQuery(chatSessionDoc.getContactId());
			chatContactQuery.setLastInBoundStamp(iMessage.getTimestamp());
			chatContactQuery.update(iMessage.contact());
			updateFirst(chatContactQuery);
		} else if (PostManUtil.isOutBound(iMessage)) {

		}
		updateMessageFromSession(chatSessionDoc, iMessage);
		return chatSessionDoc;
	}

	@Deprecated
	public ChatSessionDoc linkSession(IMessage iMessage) {
		ChatSessionDoc chatSessionDoc = this.createSession(iMessage);
		chatSessionDoc = linkSession(chatSessionDoc, iMessage);
		return chatSessionDoc;
	}

	public IMessageExtended toSessionMessage(ChatSessionDoc session) {
		ChatContactDoc contact = getContact(session.getContactId());
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.contact().copyFrom(contact);
		inboxMessage.contact().setContactType(contact.getContactType());
		inboxMessage.contact().setChannelType(contact.getChannelType());
		inboxMessage.contact().setLane(ArgUtil.nonEmpty(session.getLane(), contact.getLane()));
		inboxMessage.setFrom(contact.getCsid());
		inboxMessage.setFromName(contact.getName());
		inboxMessage.setSessionId(contact.getSessionId());
		inboxMessage.contact().setContactId(contact.getContactId());

		inboxMessage.session().setQueue(session.getAssignedToQueue());
		inboxMessage.session().setMode(session.getMode());
		inboxMessage.session().setAgent(session.getAssignedToAgent());
		inboxMessage.session().setDept(session.getAssignedToDept());

		return inboxMessage;
	}

	public boolean inactiveAllPreviousSessions(String contactId, String ticketHash) {

		Criteria contactQ = Criteria.where("contactId").is(contactId);

		if (ArgUtil.is(ticketHash)) {
			contactQ.and("ticketHash").is(ticketHash);
		} else {
			contactQ.and("ticketHash").is(null);
		}

		Query query2 = new Query();
		query2.addCriteria(contactQ.orOperator(
				// is active
				Criteria.where("active").is(true),
				// or primary
				Criteria.where("primary").is(true)));
		Update update = new Update().set("active", false).set("primary", false).set("closeSessionStamp",
				System.currentTimeMillis());
		super.updateMulti(query2, update, ChatSessionDoc.class);
		return true;
	}

	@Deprecated
	public boolean closeAllPreviousSessions(String contactId) {
		return this.inactiveAllPreviousSessions(contactId, null);
	}

	public List<ChatSessionDoc> findChatSessionDocByAgent(String agentCode) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("assignedToAgent").is(agentCode).and("active").is(true));
		return super.find(query2, ChatSessionDoc.class);
	}

	public List<ChatSessionDoc> findChatSessionDocByQuery(Query query) {
		return super.find(query, ChatSessionDoc.class);
	}

	public void expireChatSession() {
		Calendar cal = Calendar.getInstance();
		int offsetOur = (int) ((cal.getTimeInMillis() / 3600)
				% (TimeUtils.toHours(pmClientConfig.getChatSessionTimeout()) / 2));
		if (offsetOur == 0) {
			cal.add(Calendar.HOUR, -1 * (int) TimeUtils.toHours(pmClientConfig.getChatSessionTimeout()));
			CommonMongoQueryBuilder cmqb = new CommonMongoQueryBuilder()
					.where(Criteria.where("active").is(true).and("lastInComingStamp").lt(cal.getTimeInMillis())
							.andOperator(new Criteria().orOperator(Criteria.where("resolved").exists(false),
									Criteria.where("resolved").is(false))))
					.set("expired", true).set("active", false).set("closeSessionStamp", System.currentTimeMillis());
			super.updateFirst(cmqb.getQuery(), cmqb.getUpdate(), ChatSessionDoc.class);
		}
	}

	public List<ChatSessionDoc> findSimilarChatSessionForContactId(String contactId, Long fromStamp, Long toStamp) {
		ChatContactDoc contact = getContact(contactId);

		List<ChatContactDoc> contacts = null;
		if (ArgUtil.is(contact) && !ArgUtil.areEmpty(contact.getPhone(), contact.getEmail())) {
			Query query1 = new Query();
			List<Criteria> orExpression = new ArrayList<Criteria>();
			if (ArgUtil.is(contact.getPhone())) {
				orExpression.add(Criteria.where("phone").is(contact.getPhone()));
			}
			if (ArgUtil.is(contact.getEmail())) {
				orExpression.add(Criteria.where("email").is(contact.getEmail()));
			}
			if (ArgUtil.is(contact.getProfileId())) {
				orExpression.add(Criteria.where("profileId").is(contact.getProfileId()));
			}
			query1.addCriteria(new Criteria().orOperator(orExpression.toArray(new Criteria[orExpression.size()])));
			contacts = super.find(query1, ChatContactDoc.class);
		}

		Calendar timeout = Calendar.getInstance();
		timeout.setTimeInMillis(timeout.getTimeInMillis() - DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD * 30);

		Query query2 = new Query();
		List<Criteria> orExpression = new ArrayList<Criteria>();

		if (ArgUtil.is(contacts)) {
			for (ChatContactDoc chatContactDoc : contacts) {
				orExpression.add(Criteria.where("contactId").is(chatContactDoc.getContactId()));
			}
		} else {
			orExpression.add(Criteria.where("contactId").is(contactId));
		}
		// Time Limit Criteria
		Criteria tymCriteria = Criteria.where("updatedStamp");
		if (fromStamp > 0L) {
			tymCriteria.gte(fromStamp);
		}
		if (toStamp > 0L) {
			tymCriteria.lt(toStamp);
		}

		if (fromStamp == 0L && toStamp == 0L) {
			tymCriteria.gte(timeout.getTimeInMillis());
		}

		query2.addCriteria(tymCriteria.orOperator(orExpression.toArray(new Criteria[orExpression.size()])));
		// LOGGER.info(query2.toString());
		removeMsgFields(query2);
		return super.find(query2, ChatSessionDoc.class);
	}

	private void removeMsgFields(Query query2) {
		query2.fields().exclude("lastInBoundMsg").exclude("lastBotReply").exclude("lastAgentReply")
				.exclude("lastOutBoundMsg").exclude("lastMsg");
	}

	public List<ChatSessionDoc> findActiveChatSessionForContactId(String contactId) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("contactId").is(contactId).and("active").is(true));
		return super.find(query2, ChatSessionDoc.class);
	}

	public ChatSessionDoc saveSession(ChatSessionDoc chatSessionDoc) {
		try {
			if (ArgUtil.isEmpty(chatSessionDoc.getStartSessionStamp()) || chatSessionDoc.getStartSessionStamp() == 0L) {
				chatSessionDoc.setStartSessionStamp(System.currentTimeMillis());
			}
			super.save(chatSessionDoc);
		} catch (Exception e) {
			ChatSessionDoc chatSessionDoc2 = super.findById(chatSessionDoc.getSessionId(), ChatSessionDoc.class);
			LOGGER.error(chatSessionDoc.getVersion() + " ~ " + chatSessionDoc2.getVersion(), e);
			if (chatSessionDoc.getVersion() == null) {
				// chatSessionDoc.setVersion(0);
				super.save(chatSessionDoc);
			} else {
				// chatSessionDoc.setVersion(chatSessionDoc2.getVersion()+1);
				super.save(chatSessionDoc);
			}
		}
		return chatSessionDoc;
	}

	public ChatSessionDoc initSession(ChatSessionDoc chatSessionDoc) {

		ChatContactDoc contactDoc = messageContext.contact().getDoc();

		chatSessionDoc.setInitd(true);
		chatSessionDoc.setContactName(contactDoc.getName());

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("initd", chatSessionDoc.isInitd());
		builder.set("contactName", ArgUtil.nonEmpty(chatSessionDoc.getContactName(), contactDoc.getName()));
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatSessionDoc changeStatus(ChatSessionDoc chatSessionDoc, PMConstants.CHAT_STATUS status) {
		// Old Way of Doing it
		chatSessionDoc.setStatus(status.toString());
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("status", status.toString());
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
		return chatSessionDoc;
	}

	public ChatSessionDoc resolveSession(ChatSessionDoc chatSessionDoc) {
		// Old Way of Doing it
		chatSessionDoc.setResolveSessionStamp(System.currentTimeMillis());
		chatSessionDoc.setResolved(true);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("resolveSessionStamp", chatSessionDoc.getResolveSessionStamp());
		builder.set("resolved", chatSessionDoc.isResolved());
		builder.set("status", PMConstants.CHAT_STATUS.RESOLVED);
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
		return chatSessionDoc;
	}

	public ChatSessionDoc closeSession(ChatSessionDoc chatSessionDoc) {
		chatSessionDoc.setCloseSessionStamp(System.currentTimeMillis());
		chatSessionDoc.setActive(false);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("closeSessionStamp", chatSessionDoc.getCloseSessionStamp());
		builder.set("active", chatSessionDoc.isActive());
		builder.set("status", PMConstants.CHAT_STATUS.CLOSED);
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatSessionDoc deleteSession(ChatSessionDoc chatSessionDoc) {
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder()
				.where(QueryCriteria.whereId(chatSessionDoc.getSessionId()).and("channel").is("IMPORT"));
		super.remove(builder.getQuery(), ChatSessionDoc.class);

		CommonMongoQueryBuilder builder2 = new CommonMongoQueryBuilder()
				.where(QueryCriteria.where("sessionId").is(chatSessionDoc.getSessionId()));
		super.remove(builder2.getQuery(), MessageDoc.class,
				MessageStore.getCollectionName(chatSessionDoc.getContactType()));
		return chatSessionDoc;
	}

	public ChatSessionDoc botScore(ChatSessionDoc chatSessionDoc, Integer botScore) {
		chatSessionDoc.setBotScore(botScore);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("botScore", chatSessionDoc.getBotScore());
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatSessionDoc agentScore(ChatSessionDoc chatSessionDoc, Integer agentScore) {
		chatSessionDoc.setAgentScore(agentScore);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("agentScore", chatSessionDoc.getAgentScore());
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatUserProfileDoc save(ChatUserProfileDoc doc) {
		super.save(doc);
		return doc;
	}

	public ChatUserProfileDoc save(ChatUserProfileDTO profile) {
		ChatUserProfileDoc doc = EntityDtoUtil.dtoToEntity(profile, new ChatUserProfileDoc());
		doc.setId(profile.getProfileId());
		return save(doc);
	}

	public void updateResponseTime(ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.isEmptyValue(chatSessionDoc.getFistResponseStamp())) {
			chatSessionDoc.setFistResponseStamp(System.currentTimeMillis());
		}
		chatSessionDoc.setLastResponseStamp(System.currentTimeMillis());
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("fistResponseStamp", chatSessionDoc.getFistResponseStamp());
		builder.set("lastResponseStamp", chatSessionDoc.getLastResponseStamp());
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
	}

	public void assignToAgent(ChatSessionDoc chatSessionDoc, String agentDept, String agentCode) {

		if (!ArgUtil.areEqual(chatSessionDoc.getAssignedToDept(), agentDept)) {
			chatSessionDoc.setAssignedDeptStamp(System.currentTimeMillis());
		}
		chatSessionDoc.setMode(PMConstants.CHAT_MODE.AGENT.toString());
		chatSessionDoc.setAssignedToDept(agentDept);
		chatSessionDoc.setAssignedAgentStamp(System.currentTimeMillis());
		chatSessionDoc.setAssignedToAgent(agentCode);
		// chatSessionDoc.setAssignedToQueue(PMConstants.DEFAULT.AGENT_QUEUE_CODE);
		if (chatSessionDoc.getAgentSessionStamp() == 0L) {
			chatSessionDoc.setAgentSessionStamp(chatSessionDoc.getAssignedAgentStamp());
		}

		ChatSessionQuery builder = new ChatSessionQuery(chatSessionDoc.getSessionId());
		// builder.set("mode", chatSessionDoc.getMode());
		builder.set("assignedToQueue", chatSessionDoc.getAssignedToQueue());
		builder.set("assignedToDept", chatSessionDoc.getAssignedToDept());
		builder.set("assignedDeptStamp", chatSessionDoc.getAssignedDeptStamp());
		builder.set("assignedToAgent", chatSessionDoc.getAssignedToAgent());
		builder.set("assignedAgentStamp", chatSessionDoc.getAssignedAgentStamp());
		builder.set("agentSessionStamp", chatSessionDoc.getAgentSessionStamp());
		updateFirst(builder);
	}

	public void assignToBot(ChatSessionDoc chatSessionDoc, String botName) {
		if (!ArgUtil.is(chatSessionDoc)) {
			LOGGER.error("Session Cannot Be Empty for bot {}", botName);
			return;
		}

		chatSessionDoc.setMode(PMConstants.CHAT_MODE.BOT.toString());
		chatSessionDoc.setAssignedToBot(botName);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("mode", chatSessionDoc.getMode());
		builder.set("assignedToAgent", chatSessionDoc.getAssignedToAgent());
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

	}

	public ChatSessionDoc updateQuickTags(ChatSessionDoc chatSessionDoc, List<String> tagIds) {
		chatSessionDoc.setTagId(tagIds);
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("tagId", tagIds);
		super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
		return chatSessionDoc;
	}

	/**
	 * search by status
	 * 
	 * @param status
	 * @return
	 */
	public List<ChatSessionDoc> findByStatus(CHAT_STATUS status) {
		Query query2 = new Query();
		if (ArgUtil.is(status)) {
			query2.addCriteria(Criteria.where("status").is(status.toString()));
		}
		return super.find(query2, ChatSessionDoc.class);
	}

	/**
	 * Search by category
	 * 
	 * @param tagCategory
	 * @return
	 */
	public List<ChatSessionDoc> findByQuickTag(String tagCategory) {
		Query query2 = new Query();
		if (ArgUtil.is(tagCategory)) {
			query2.addCriteria(Criteria.where("tagId").is(tagCategory));
		}
		return super.find(query2, ChatSessionDoc.class);
	}

	/**
	 * search by status or by tag category
	 * 
	 * @param status
	 * @param tagCategory
	 * @param fromStamp
	 * @param toStamp
	 * @return
	 */
	public List<ChatSessionDoc> findByStatusOrQuickTag(List<CHAT_STATUS> status, List<String> tagCategory,
			long fromStamp, long toStamp) {
		List<String> statusLst = new ArrayList<>();;
		if ((status == null || status.isEmpty() || status.contains(null)) && (tagCategory == null
				|| tagCategory.isEmpty() || tagCategory.contains(null) && tagCategory.contains(""))) {
			// status = new ArrayList<>();
			// status.add(CHAT_STATUS.OPEN);
			statusLst.add(CHAT_STATUS.OPEN.toString());
		} else {
			for (CHAT_STATUS chatSt : status) {
				statusLst.add(chatSt.toString());
			}
		}

		Query query = new Query();

		query.addCriteria(Criteria.where("assignedAgentStamp").gt(fromStamp).lt(toStamp));

		if (statusLst != null && !statusLst.isEmpty()) {
			query.addCriteria(Criteria.where("status").in(statusLst));
		}
		if (tagCategory != null && !tagCategory.isEmpty() && !tagCategory.contains(null) && !tagCategory.contains("")) {
			query.addCriteria(Criteria.where("tagId").in(tagCategory));
		}
		query.with(new Sort(new Order(Direction.DESC, "assignedAgentStamp")));
		removeMsgFields(query);
		LOGGER.debug("query {===}" + query);
		return super.find(query, ChatSessionDoc.class);
	}

	public String getLastAssignedAgent(Contactable contact) {
		CommonMongoQueryBuilder cmqb = new CommonMongoQueryBuilder().where(Criteria.where("contactId")
				.is(contact.getContactId()).and("assignedToAgent").exists(true).and("mode").is(CHAT_MODE.AGENT));
		cmqb.sortBy("startSessionStamp", Direction.DESC).limit(1).skipDBRef();
		ChatSessionDoc lastSession = super.findOne(cmqb.getQuery(), ChatSessionDoc.class);
		if (ArgUtil.is(lastSession)) {
			return lastSession.getAssignedToAgent();
		}
		return null;
	}

	public ChatSessionDoc getPreviousSession(Contactable contact) {
		CommonMongoQueryBuilder cmqb = new CommonMongoQueryBuilder()
				.where(Criteria.where("contactId").is(contact.getContactId()));
		cmqb.sortBy("startSessionStamp", Direction.DESC).limit(1).skip(1).skipDBRef();
		return super.findOne(cmqb.getQuery(), ChatSessionDoc.class);
	}

}
