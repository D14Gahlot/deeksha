package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.UpdatedTimeStampDoc;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.UpdatedTimeStampIndexSupport;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Document(collection = "CHAT_SESSION")
@TypeAlias("ChatSessionDoc")
@CompoundIndexes({
		// route indexs
		@CompoundIndex(name = "lastMsg_Stamp", def = "{ 'msg.lastMsg.timestamp': 1 }"),
		@CompoundIndex(name = "route_sendMode", def = "{ 'msg.lastMsg.route.sendMode': 1 }"),
		@CompoundIndex(name = "route_senderType", def = "{ 'msg.lastMsg.route.senderType': 1 }"),
		@CompoundIndex(name = "lastOutBoundMsg_Stamp", def = "{ 'msg.lastOutBoundMsg.timestamp': 1 }"), })
public class ChatSessionDoc extends UpdatedTimeStampDoc implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String FIRST_INBOUND_STAMP = "stamps.firstInBound";
	public static final String LAST_INBOUND_STAMP = "stamps.lastInBound";
	public static final String FIRST_OUTBOUND_STAMP = "stamps.firstOutBound";
	public static final String LAST_OUTBOUND_STAMP = "stamps.lastOutBound";

	@Id
	private String sessionId;

	@Indexed
	private String ticketHash;

	private String routingId;

	@Version
	private Long version;

	@ApiMockModelProperty(example = "wa919930104050_918828218374", required = false,
			value = "format like {{ContactType.getShortCode}}{{csid}}_{{lane}}")
	@Indexed
	private String contactId;

	@Deprecated
	private String contactType;
	@Deprecated
	private String channel;
	@Deprecated
	private String lane;

	@Deprecated
	@Indexed
	private String contactName;

	private ContactDetailDoc contact;

	private String subject;

	private String assignedToDept;
	private String assignedToAgent;
	private String assignedToBot;
	private String assignedToQueue;

	@Indexed
	private boolean active;
	private boolean initd;
	@Indexed
	private boolean resolved;
	private boolean expired;
	@Indexed
	private boolean primary;

	private long startSessionStamp;
	private long fistResponseStamp;

	@Indexed
	private long agentSessionStamp;

	private long firstInComingStamp;
	private long firstOutGoingStamp;

	@Indexed
	private long lastInComingStamp;
	@Indexed
	private long lastOutGoingStamp;

	private long assignedDeptStamp;
	@Indexed
	private long assignedAgentStamp;

	private long lastResponseStamp;
	private long resolveSessionStamp;
	private long closeSessionStamp;

	/**
	 * @deprecated Use {@link UpdatedTimeStampIndexSupport#getUpdated()}
	 */
	@Deprecated
	private long updatedStamp;

	private Integer agentScore;
	private Integer botScore;

	@Indexed
	private String mode;
	private String status;
	@Deprecated
	private String tagCategory;

	@Indexed
	private List<String> tagId;

	private Map<String, Object> store;
	private Map<String, Object> meta;

	private Map<String, ChatMessageDTO> msg;
	private Map<String, Long> stamps;
	private Map<String, Long> read;
	private Map<String, Object> feedback;

	// MessageStats
	@DBRef
	private MessageDoc lastInBoundMsg;

	@DBRef
	private MessageDoc lastOutBoundMsg;

	// @DBRef
	private MessageDoc lastMsg;
	private String expiration_timestamp;

	public String getExpiration_timestamp() {
		return expiration_timestamp;
	}

	public void setExpiration_timestamp(String expiration_timestamp) {
		this.expiration_timestamp = expiration_timestamp;
	}

	public long getLastInComingStamp() {
		return lastInComingStamp;
	}

	public void setLastInComingStamp(long lastInComingStamp) {
		this.lastInComingStamp = lastInComingStamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getAssignedToDept() {
		return assignedToDept;
	}

	public void setAssignedToDept(String assignedToDept) {
		this.assignedToDept = assignedToDept;
	}

	public String getAssignedToAgent() {
		return assignedToAgent;
	}

	public void setAssignedToAgent(String assignedToAgent) {
		this.assignedToAgent = assignedToAgent;
	}

	public boolean isInitd() {
		return initd;
	}

	public void setInitd(boolean initd) {
		this.initd = initd;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public long getStartSessionStamp() {
		return startSessionStamp;
	}

	public void setStartSessionStamp(long startSessionStamp) {
		this.startSessionStamp = startSessionStamp;
	}

	public long getCloseSessionStamp() {
		return closeSessionStamp;
	}

	public void setCloseSessionStamp(long closeSessionStamp) {
		this.closeSessionStamp = closeSessionStamp;
	}

	public long getAssignedDeptStamp() {
		return assignedDeptStamp;
	}

	public void setAssignedDeptStamp(long assignedDeptStamp) {
		this.assignedDeptStamp = assignedDeptStamp;
	}

	public long getAssignedAgentStamp() {
		return assignedAgentStamp;
	}

	public void setAssignedAgentStamp(long assignedAgentStamp) {
		this.assignedAgentStamp = assignedAgentStamp;
	}

	public long getFistResponseStamp() {
		return fistResponseStamp;
	}

	public void setFistResponseStamp(long fistResponseStamp) {
		this.fistResponseStamp = fistResponseStamp;
	}

	public long getLastResponseStamp() {
		return lastResponseStamp;
	}

	public void setLastResponseStamp(long lastResponseStamp) {
		this.lastResponseStamp = lastResponseStamp;
	}

	public Integer getAgentScore() {
		return agentScore;
	}

	public void setAgentScore(Integer agentScore) {
		this.agentScore = agentScore;
	}

	public Integer getBotScore() {
		return botScore;
	}

	public void setBotScore(Integer botScore) {
		this.botScore = botScore;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public long getResolveSessionStamp() {
		return resolveSessionStamp;
	}

	public void setResolveSessionStamp(long resolveSessionStamp) {
		this.resolveSessionStamp = resolveSessionStamp;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	@Deprecated
	public String getContactType() {
		if (!ArgUtil.is(contactType)) {
			return this.contact().getContactType();
		}
		return contactType;
	}

	@Deprecated
	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	@Deprecated
	public String getChannel() {
		if (!ArgUtil.is(channel)) {
			return this.contact().getChannelType();
		}
		return channel;
	}

	@Deprecated
	public void setChannel(String channel) {
		this.channel = channel;
	}

	@Deprecated
	public String getLane() {
		if (!ArgUtil.is(lane)) {
			return this.contact().getLane();
		}
		return lane;
	}

	@Deprecated
	public void setLane(String lane) {
		this.lane = lane;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getAgentSessionStamp() {
		return agentSessionStamp;
	}

	public void setAgentSessionStamp(long agentSessionStamp) {
		this.agentSessionStamp = agentSessionStamp;
	}

	public long getLastOutGoingStamp() {
		return lastOutGoingStamp;
	}

	public void setLastOutGoingStamp(long lastOutGoingStamp) {
		this.lastOutGoingStamp = lastOutGoingStamp;
	}

	@Override
	public String toString() {
		return String.format("[sessionId:%s]", this.sessionId);
	}

	public MessageDoc getLastInBoundMsg() {
		return lastInBoundMsg;
	}

	public void setLastInBoundMsg(MessageDoc lastInBoundMsg) {
		this.lastInBoundMsg = lastInBoundMsg;
	}

	public MessageDoc getLastOutBoundMsg() {
		return lastOutBoundMsg;
	}

	public void setLastOutBoundMsg(MessageDoc lastOutBoundMsg) {
		this.lastOutBoundMsg = lastOutBoundMsg;
	}

	public MessageDoc getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(MessageDoc lastMsg) {
		this.lastMsg = lastMsg;
	}

	/**
	 * @deprecated Use {@link UpdatedTimeStampSupport#getUpdated())}
	 */
	@Deprecated
	public long getUpdatedStamp() {
		return updatedStamp;
	}

	/**
	 * @deprecated Use
	 *             {@link UpdatedTimeStampIndexSupport#setUpdated(com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex)}
	 */
	@Deprecated
	public void setUpdatedStamp(long updatedStamp) {
		this.updatedStamp = updatedStamp;
	}

	public String getTagCategory() {
		return tagCategory;
	}

	public void setTagCategory(String tagCategory) {
		this.tagCategory = tagCategory;
	}

	public List<String> getTagId() {
		return tagId;
	}

	public void setTagId(List<String> tagId) {
		this.tagId = tagId;
	}

	public List<String> tagId() {
		if (ArgUtil.isEmpty(this.tagId))
			this.tagId = new ArrayList<String>();
		return tagId;
	}

	public long getFirstInComingStamp() {
		return firstInComingStamp;
	}

	public void setFirstInComingStamp(long firstInComingStamp) {
		this.firstInComingStamp = firstInComingStamp;
	}

	public long getFirstOutGoingStamp() {
		return firstOutGoingStamp;
	}

	public void setFirstOutGoingStamp(long firstOutGoingStamp) {
		this.firstOutGoingStamp = firstOutGoingStamp;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public ContactDetailDoc getContact() {
		return contact;
	}

	public void setContact(ContactDetailDoc contact) {
		this.contact = contact;
	}

	public Contactable contact() {
		if (this.contact == null) {
			this.contact = new ContactDetailDoc();
		}
		return this.contact;
	}

	public String getAssignedToQueue() {
		return assignedToQueue;
	}

	public void setAssignedToQueue(String assignedToQueue) {
		this.assignedToQueue = assignedToQueue;
	}

	public Map<String, Object> getStore() {
		return store;
	}

	public void setStore(Map<String, Object> store) {
		this.store = store;
	}

	public Map<String, Object> store() {
		if (this.store == null) {
			this.store = new HashMap<String, Object>();
		}
		return store;
	}

	public Map<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public String getAssignedToBot() {
		return assignedToBot;
	}

	public void setAssignedToBot(String assignedToBot) {
		this.assignedToBot = assignedToBot;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTicketHash() {
		return ticketHash;
	}

	public void setTicketHash(String ticketHash) {
		this.ticketHash = ticketHash;
	}

	public Map<String, ChatMessageDTO> getMsg() {
		return msg;
	}

	public void setMsg(Map<String, ChatMessageDTO> msg) {
		this.msg = msg;
	}

	public Map<String, ChatMessageDTO> msg() {
		if (this.msg == null) {
			this.msg = new HashMap<String, ChatMessageDTO>();
		}
		return this.msg;
	}

	public Map<String, Long> getStamps() {
		return stamps;
	}

	public void setStamps(Map<String, Long> stamps) {
		this.stamps = stamps;
	}

	public Map<String, Long> stamps() {
		if (this.stamps == null) {
			this.stamps = new HashMap<String, Long>();
		}
		return this.stamps;
	}

	public Map<String, Long> getRead() {
		return read;
	}

	public void setRead(Map<String, Long> read) {
		this.read = read;
	}

	public Map<String, Long> read() {
		if (this.read == null) {
			this.read = new HashMap<String, Long>();
		}
		return this.read;
	}

	public Map<String, Object> getFeedback() {
		return feedback;
	}

	public void setFeedback(Map<String, Object> feedback) {
		this.feedback = feedback;
	}

	public String getRoutingId() {
		return routingId;
	}

	public void setRoutingId(String routingId) {
		this.routingId = routingId;
	}

	public void refreshStamps() {
		if (!ArgUtil.is(this.lastOutGoingStamp)) {
			if (this.lastOutBoundMsg != null && PostManUtil.isOutBound(lastOutBoundMsg.getType())) {
				this.lastOutGoingStamp = lastOutBoundMsg.getTimestamp();
			} else if (this.lastMsg != null && PostManUtil.isOutBound(lastMsg.getType())) {
				this.lastOutGoingStamp = lastMsg.getTimestamp();
			} else if (this.msg != null && this.msg.containsKey("lastOutBoundMsg")) {
				ChatMessageDTO m = this.msg.get("lastOutBoundMsg");
				this.lastOutGoingStamp = m.getTimestamp();
			} else if (this.msg != null && this.msg.containsKey("lastMsg")) {
				ChatMessageDTO m = this.msg.get("lastMsg");
				if (PostManUtil.isOutBound(m.getType())) {
					this.lastOutGoingStamp = m.getTimestamp();
				}
			}
		}

		if (!ArgUtil.is(this.lastInComingStamp)) {
			if (this.lastInBoundMsg != null && PostManUtil.isInBound(lastInBoundMsg.getType())) {
				this.lastInComingStamp = this.lastInBoundMsg.getTimestamp();
			} else if (this.lastMsg != null && PostManUtil.isInBound(lastMsg.getType())) {
				this.lastInComingStamp = lastMsg.getTimestamp();
			} else if (this.msg != null && this.msg.containsKey("lastInBoundMsg")) {
				ChatMessageDTO m = this.msg.get("lastInBoundMsg");
				this.lastInComingStamp = m.getTimestamp();
			} else if (this.msg != null && this.msg.containsKey("lastMsg")) {
				ChatMessageDTO m = this.msg.get("lastMsg");
				if (PostManUtil.isInBound(m.getType())) {
					this.lastInComingStamp = m.getTimestamp();
				}
			}
		}
	}
}
