package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;

import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.mongo.CommonDocInterfaces.Patchable;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageDefinitions.IMessageId;
import com.boot.jx.postman.model.MessageRouter;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.postman.pbook.PBVCard;
import com.boot.utils.ArgUtil;

@CompoundIndexes({ @CompoundIndex(name = "route_queueCode", def = "{ 'route.queueCode': 1 }"),
		@CompoundIndex(name = "route_sendMode", def = "{ 'route.sendMode': 1 }"),
		@CompoundIndex(name = "route_senderCode", def = "{ 'route.senderCode': 1 }") })
public abstract class MessageDocAbstract implements Serializable, Patchable<MessageDoc>, IMessageId {
	private static final long serialVersionUID = 3983687776318251181L;

	@Indexed
	private String messageIdExt;
	private String messageIdRef;
	private String traceId;

	@Indexed
	private String sessionId;

	@Indexed
	private String bulkSessionId;

	private String collapseId;
	private long timestamp;
	private String type;
	private String template;
	private String templateId;
	private CommonTemplateMeta hsm;

	private String action;
	private String handler;
	private String message;
	private String subject;

	private String formatType;
	private String formatSubType;

	private String status;
	private ContactDetailDoc contact;
	private MessageRouter route;

	@Indexed
	private String queue;
	private String agent;

	private TagDocument tags;
	private Map<String, Object> model;
	private Map<String, Object> meta;
	protected Map<String, Object> form;
	protected Map<String, Object> options;
	private List<Attachment> attachments;
	private List<PBVCard> vccards;

	@Indexed
	private String replyIdExt;
	private String replyId;
	private String quickReplyId;
	private String mediaReplyId;

	private Map<String, Long> stamps;
	public List<String> logs;
	private Map<String, Object> replyTo;

	@Indexed
	private String contactId;

	public String getMessageId() {
		return null;
	}

	public String getCollapseId() {
		return collapseId;
	}

	public void setCollapseId(String collapseId) {
		this.collapseId = collapseId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ContactDetailDoc getContact() {
		return contact;
	}

	public void setContact(ContactDetailDoc contact) {
		this.contact = contact;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
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

	public String getAgent() {
		return this.agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public TagDocument getTags() {
		return tags;
	}

	public void setTags(TagDocument tags) {
		this.tags = tags;
	}

	public String getMediaReplyId() {
		return mediaReplyId;
	}

	public void setMediaReplyId(String mediaReplyId) {
		this.mediaReplyId = mediaReplyId;
	}

	public String getQuickReplyId() {
		return quickReplyId;
	}

	public void setQuickReplyId(String quickReplyId) {
		this.quickReplyId = quickReplyId;
	}

	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public MessageDoc patch() {
		MessageDoc patch = new MessageDoc();
		patch.setMessageId(this.getMessageId());
		return patch;
	}

	public List<String> getLogs() {
		return logs;
	}

	public void setLogs(List<String> logs) {
		this.logs = logs;
	}

	public List<String> logs() {
		if (this.logs == null) {
			this.logs = new ArrayList<String>();
		}
		return this.logs;
	}

	public String getMessageIdExt() {
		return messageIdExt;
	}

	public void setMessageIdExt(String messageIdExt) {
		this.messageIdExt = messageIdExt;
	}

	public String getMessageIdRef() {
		return messageIdRef;
	}

	public void setMessageIdRef(String messageIdRef) {
		this.messageIdRef = messageIdRef;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, Long> getStamps() {
		return stamps;
	}

	public void setStamps(Map<String, Long> stamps) {
		this.stamps = stamps;
	}

	public Map<String, Long> stamps() {
		if (stamps == null)
			stamps = new HashMap<String, Long>();
		return stamps;
	}

	public void updateStatus(Status status) {
		String statusStr = ArgUtil.parseAsString(status);
		this.status = statusStr;
		this.stamps().put(statusStr, System.currentTimeMillis());
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getBulkSessionId() {
		return bulkSessionId;
	}

	public void setBulkSessionId(String bulkId) {
		this.bulkSessionId = bulkId;
	}

	public Map<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public Map<String, Object> meta() {
		if (this.meta == null) {
			this.meta = new HashMap<String, Object>();
		}
		return this.meta;
	}

	public String getReplyIdExt() {
		return replyIdExt;
	}

	public void setReplyIdExt(String replyIdExt) {
		this.replyIdExt = replyIdExt;
	}

	public String getReplyId() {
		return replyId;
	}

	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}

	public String getFormatType() {
		return formatType;
	}

	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}

	public String getFormatSubType() {
		return formatSubType;
	}

	public void setFormatSubType(String formatSubType) {
		this.formatSubType = formatSubType;
	}

	public CommonTemplateMeta getHsm() {
		return hsm;
	}

	public void setHsm(CommonTemplateMeta hsm) {
		this.hsm = hsm;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public void setReplyTo(Map<String, Object> replyTo) {
		this.replyTo = replyTo;
	}

	public Map<String, Object> getReplyTo() {
		return this.replyTo;
	}

	public Map<String, Object> replyTo() {
		if (this.replyTo == null) {
			this.replyTo = new HashMap<String, Object>();
		}
		return this.replyTo;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public MessageRouter getRoute() {
		return route;
	}

	public void setRoute(MessageRouter route) {
		this.route = route;
	}

	public MessageRouter route() {
		if (route == null) {
			this.route = new MessageRouter();
		}
		return this.route;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public List<PBVCard> getVccards() {
		return vccards;
	}

	public void setVccards(List<PBVCard> vccards) {
		this.vccards = vccards;
	}

	public List<PBVCard> vccards() {
		if (vccards == null) {
			this.vccards = new ArrayList<PBVCard>();
		}
		return this.vccards;
	}

	public Map<String, Object> getForm() {
		return form;
	}

	public void setForm(Map<String, Object> form) {
		this.form = form;
	}

	public Map<String, Object> form() {
		if (form == null) {
			this.form = new HashMap<String, Object>();
		}
		return this.form;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	public Map<String, Object> options() {
		if (options == null) {
			this.options = new HashMap<String, Object>();
		}
		return this.options;
	}

}
