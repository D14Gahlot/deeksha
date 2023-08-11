package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditCreateEntity;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.dto.ContactPrefsDTO;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "CHAT_CONTACT")
@TypeAlias("ChatContactDoc")
public class ChatContactDoc implements Serializable, Contactable, AuditCreateEntity {
	private static final long serialVersionUID = 1L;

	@Id
	@ApiMockModelProperty(example = "wa919930104050_918828218374", required = false,
			value = "format like {{ContactType.getShortCode}}{{csid}}_{{lane}}")
	@JsonProperty("contactId")
	private String contactId;

	@Indexed
	private String csid;

	private String contactType;

	private String channelType;
	@Deprecated
	private String channel;

	@Indexed
	private String lane;

	private long firstInBoundStamp;
	private long firstOutBoundStamp;
	private long lastInBoundStamp;
	private long lastOutBoundStamp;
	private long lastPushStamp;
	private long lastReplyStamp;

	private long lastOptInStamp;
	private long lastSentXStamp;

	private String sessionId;

	// @TextIndexed(weight = 10)
	private String name;

	// @TextIndexed(weight = 1)
	@Indexed
	private String email;
	private Boolean emailVerified;

	// @TextIndexed(weight = 5)
	@Indexed
	private String phone;
	private Boolean phoneVerified;

	private String profilePic;
	@Indexed
	private List<String> labelId;
	private ChatUserProfileDTO profile;
	private ContactPrefsDTO prefs;
	private Map<String, Object> store;

	@Indexed
	private String profileId;

	private Long createdStamp;
	private String createdBy;

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public long getLastInBoundStamp() {
		return lastInBoundStamp;
	}

	public void setLastInBoundStamp(long lastInBoundStamp) {
		this.lastInBoundStamp = lastInBoundStamp;
	}

	public long getLastOutBoundStamp() {
		return lastOutBoundStamp;
	}

	public void setLastOutBoundStamp(long lastOutBoundStamp) {
		this.lastOutBoundStamp = lastOutBoundStamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCsid() {
		return csid;
	}

	public void setCsid(String csid) {
		this.csid = csid;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public String getChannel() {
		return ArgUtil.nonEmpty(this.channelType, this.channel);
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<String> labelId() {
		if (ArgUtil.isEmpty(this.labelId))
			this.labelId = new ArrayList<String>();
		return labelId;
	}

	public List<String> getLabelId() {
		return labelId;
	}

	public void setLabelId(List<String> labelId) {
		this.labelId = labelId;
	}

	public ChatUserProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(ChatUserProfileDTO profile) {
		this.profile = profile;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public long getLastOptInStamp() {
		return lastOptInStamp;
	}

	public void setLastOptInStamp(long lastOptInStamp) {
		this.lastOptInStamp = lastOptInStamp;
	}

	@Override
	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	@Override
	public String getChannelType() {
		return ArgUtil.nonEmpty(this.channelType, this.channel);
	}

	public long getLastPushStamp() {
		return lastPushStamp;
	}

	public void setLastPushStamp(long lastPushStamp) {
		this.lastPushStamp = lastPushStamp;
	}

	public long getLastReplyStamp() {
		return lastReplyStamp;
	}

	public void setLastReplyStamp(long lastReplyStamp) {
		this.lastReplyStamp = lastReplyStamp;
	}

	@Override
	public String toString() {
		return String.format("[contactId:%s]", this.contactId);
	}

	public long getFirstInBoundStamp() {
		return firstInBoundStamp;
	}

	public void setFirstInBoundStamp(long firstInBoundStamp) {
		this.firstInBoundStamp = firstInBoundStamp;
	}

	public long getFirstOutBoundStamp() {
		return firstOutBoundStamp;
	}

	public void setFirstOutBoundStamp(long firstOutBoundStamp) {
		this.firstOutBoundStamp = firstOutBoundStamp;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public long getLastSentXStamp() {
		return lastSentXStamp;
	}

	public void setLastSentXStamp(long lastSentXStamp) {
		this.lastSentXStamp = lastSentXStamp;
	}

	public ContactPrefsDTO getPrefs() {
		return prefs;
	}

	public void setPrefs(ContactPrefsDTO prefs) {
		this.prefs = prefs;
	}

	public ContactPrefsDTO prefs() {
		if (this.prefs == null) {
			this.prefs = new ContactPrefsDTO();
		}
		return prefs;
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

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public Boolean getPhoneVerified() {
		return phoneVerified;
	}

	public void setPhoneVerified(Boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

}
