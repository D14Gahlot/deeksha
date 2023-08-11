package com.boot.jx.postman.doc.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.AppContextUtil;
import com.boot.jx.mongo.CommonDocInterfaces.AuditableByIdEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "CONFIG_CLIENT_KEY")
@TypeAlias("ClientAppConfigDoc")
public class ClientAppConfigDoc implements IDocument, AuditableByIdEntity, ClientApp, JsonIgnoreUnknown {

	private static final long serialVersionUID = -3070718912315245729L;

	@Id
	private String id;

	@JsonProperty("name")
	@Indexed(unique = true)
	private String keyName;

	@JsonProperty("code")
	@Indexed(unique = true)
	private String queue;

	private String createdBy;
	private Long createdStamp;

	private String updatedBy;
	private Long updatedStamp;

	private String key;
	private String keyVersion;

	private String appMode;
	private String appType;
	private String webhook;
	private String forward;

	private Map<String, Object> secret;
	private Map<String, Object> props;

	private String domain;
	private boolean isShared;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	@Override
	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	@Override
	public String getKeyVersion() {
		return keyVersion;
	}

	public void setKeyVersion(String keyVersion) {
		this.keyVersion = keyVersion;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void setKey(String apiKey) {
		this.key = apiKey;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getWebhook() {
		return webhook;
	}

	public void setWebhook(String webhook) {
		this.webhook = webhook;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Long getUpdatedStamp() {
		return updatedStamp;
	}

	public void setUpdatedStamp(Long updatedStamp) {
		this.updatedStamp = updatedStamp;
	}

	public String getForward() {
		return forward;
	}

	public void setForward(String forward) {
		this.forward = forward;
	}

	public Map<String, Object> getSecret() {
		return secret;
	}

	public void setSecret(Map<String, Object> secret) {
		this.secret = secret;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public Map<String, Object> props() {
		if (this.props == null) {
			this.props = new HashMap<String, Object>();
		}
		return props;
	}

	public Map<String, Object> secret() {
		if (this.secret == null) {
			this.secret = new HashMap<String, Object>();
		}
		return secret;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public boolean isShared() {
		return isShared;
	}

	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}

	@Override
	public boolean isReadOnly() {
		return (this.isShared()) && !ArgUtil.areEqual(domain, AppContextUtil.getTenant());
	}

	@Override
	public boolean isAgentApp() {
		return CHAT_MODE.AGENT.toString().equals(getAppMode());
	}

	@Override
	public boolean equals(CHAT_MODE mode) {
		return mode.toString().equals(getAppMode());
	}

	@Override
	public boolean equals(APP_TYPE appType) {
		return appType.toString().equals(getAppType());
	}

	@Override
	public String getAppMode() {
		if (appMode == null) {
			this.appMode = APP_TYPE.from(getAppType()).getMode().toString();
		}
		return appMode;
	}

	public void setAppMode(String appMode) {
		this.appMode = appMode;
	}
}
