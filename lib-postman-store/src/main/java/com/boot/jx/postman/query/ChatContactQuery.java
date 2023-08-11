package com.boot.jx.postman.query;

import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.utils.ArgUtil;

public class ChatContactQuery extends DocQueryBuilder<ChatContactDoc> {

	public ChatContactQuery(ChatContactDoc doc) {
		super(doc);
	}

	public ChatContactQuery(String contactId) {
		super(contactId);
	}

	@Override
	public String getId(ChatContactDoc doc) {
		return doc.getContactId();
	}

	@Override
	public ChatContactDoc newDoc(String id) {
		ChatContactDoc doc = new ChatContactDoc();
		doc.setContactId(id);
		return doc;
	}

	public String getName() {
		return this.getDoc().getName();
	}

	public String getPhone() {
		return this.getDoc().getPhone();
	}

	public String getEmail() {
		return this.getDoc().getEmail();
	}

	public String getLang() {
		return this.getDoc().prefs().getLang();
	}

	public Object get(String key) {
		return this.doc.store().get(key);
	}

	public ChatContactQuery put(String key, String object) {
		this.doc.store().put(key, object);
		this.set("store." + key, object);
		return this;
	}

	public ChatContactQuery remove(String key) {
		this.doc.store().remove(key);
		this.unset("store." + key);
		return this;
	}

	public ChatContactQuery setLang(String lang) {
		this.doc.prefs().setLang(lang);
		this.set("prefs.lang", lang);
		return this;
	}

	public ChatContactQuery setLastInBoundStamp(long timestamp) {
		this.doc.setLastInBoundStamp(timestamp);
		this.set("lastInBoundStamp", timestamp);
		return this;
	}

	public ChatContactQuery setLastOutBoundStamp(long timestamp) {
		this.doc.setLastOutBoundStamp(timestamp);
		this.set("lastOutBoundStamp", timestamp);
		return this;
	}

	public ChatContactQuery setContactId(String contactId) {
		this.doc.setContactId(contactId);
		this.set("contactId", contactId);
		return this;
	}

	public ChatContactQuery setContactType(String contactType) {
		this.doc.setContactType(contactType);
		this.set("contactType", contactType);
		return this;
	}

	public ChatContactQuery setChannelType(String channelType) {
		this.doc.setChannelType(channelType);
		this.set("channelType", channelType);
		return this;
	}

	public ChatContactQuery setCsid(String csid) {
		this.doc.setCsid(csid);
		this.set("csid", csid);
		return this;
	}

	public ChatContactQuery setLane(String lane) {
		this.doc.setLane(lane);
		this.set("lane", lane);
		return this;
	}

	public ChatContactQuery setSessionId(String sessionId) {
		this.doc.setSessionId(sessionId);
		this.set("sessionId", sessionId);
		return this;
	}

	public ChatContactQuery setLastOptInStamp(long lastOptInStamp) {
		this.doc.setLastOptInStamp(lastOptInStamp);
		this.set("lastOptInStamp", lastOptInStamp);
		return this;
	}

	public ChatContactQuery setChannel(String channel) {
		this.doc.setChannelType(channel);
		this.set("channel", channel);
		return this;
	}

	public ChatContactQuery setLastPushStamp(long lastPushStamp) {
		this.doc.setLastPushStamp(lastPushStamp);
		this.set("lastPushStamp", lastPushStamp);
		return this;
	}

	public ChatContactQuery setLastReplyStamp(long lastReplyStamp) {
		this.doc.setLastReplyStamp(lastReplyStamp);
		this.set("lastReplyStamp", lastReplyStamp);
		return this;
	}

	public ChatContactQuery setFirstOutBoundStamp(long firstOutBoundStamp) {
		this.doc.setFirstOutBoundStamp(firstOutBoundStamp);
		this.set("firstOutBoundStamp", firstOutBoundStamp);
		return this;
	}

	public ChatContactQuery setFirstInBoundStamp(long firstInBoundStamp) {
		this.doc.setFirstInBoundStamp(firstInBoundStamp);
		this.set("firstInBoundStamp", firstInBoundStamp);
		return this;
	}

	public ChatContactQuery setName(String name) {
		this.doc.setName(name);
		this.set("name", name);
		return this;
	}

	public ChatContactQuery setEmail(String email) {
		this.doc.setEmail(email);
		this.set("email", email);
		return this;
	}

	public ChatContactQuery setPhone(String phone) {
		this.doc.setPhone(phone);
		this.set("phone", phone);
		return this;
	}

	public ChatContactQuery setPhoneVerified(boolean phoneVerified) {
		this.doc.setPhoneVerified(true);
		this.set("phoneVerified", phoneVerified);
		return this;
	}

	public ChatContactQuery setEmailVerified(boolean emailVerified) {
		this.doc.setEmailVerified(true);
		this.set("emailVerified", emailVerified);
		return this;
	}

	public ChatContactQuery setProfilePic(String profilePic) {
		this.doc.setProfilePic(profilePic);
		this.set("profilePic", profilePic);
		return this;
	}

	public void updateLastOptInStamp() {
		long optinStamp = System.currentTimeMillis();
		this.doc.setLastOptInStamp(optinStamp);
		this.set("lastOptInStamp", optinStamp);
	}

	public void updateCreatedStamp() {
		long optinStamp = System.currentTimeMillis();
		this.doc.setCreatedStamp(optinStamp);
		update().setOnInsert("createdStamp", optinStamp);
	}

	public void updateFirstInBoundStamp() {
		long optinStamp = System.currentTimeMillis();
		this.doc.setFirstInBoundStamp(optinStamp);
		update().setOnInsert("firstInBoundStamp", optinStamp);
	}

	public ChatContactQuery update(Contactable contactable) {
		if (ArgUtil.is(contactable.getContactId())) {
			this.setContactId(ArgUtil.nonEmpty(contactable.getContactId(), this.doc.getContactId()));
		}
		if (ArgUtil.is(contactable.getContactType())) {
			this.setContactType(ArgUtil.nonEmpty(contactable.getContactType(), this.doc.getContactType()));
		}
		if (ArgUtil.is(contactable.getChannelType())) {
			this.setChannel(ArgUtil.nonEmpty(contactable.getChannelType(), this.doc.getChannelType()));
		}

		if (ArgUtil.is(contactable.getContactType()) || ArgUtil.is(contactable.getChannelType())) {
			this.setChannelType(ArgUtil.nonEmpty(
					PMConstants.CHANNEL_TYPE(contactable.getContactType(), contactable.getChannelType()),
					this.doc.getChannelType()));
		}

		if (ArgUtil.is(contactable.getCsid())) {
			this.setCsid(ArgUtil.nonEmpty(contactable.getCsid(), this.doc.getCsid()));
		}
		if (ArgUtil.is(contactable.getLane())) {
			this.setLane(ArgUtil.nonEmpty(contactable.getLane(), this.doc.getLane()));
		}
		if (ArgUtil.is(contactable.getName())) {
			this.setName(ArgUtil.nonEmpty(contactable.getName(), this.doc.getName()));
		}

		if (ArgUtil.is(contactable.getEmail())) {
			this.setEmail(ArgUtil.nonEmpty(contactable.getEmail(), this.doc.getEmail()));
		}

		if (ArgUtil.is(contactable.getPhone())) {
			this.setPhone(ArgUtil.nonEmpty(contactable.getPhone(), this.doc.getPhone()));
		}

		return this;
	}

}
