package com.boot.jx.postman.doc.config;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.utils.ArgUtil;

@Document(collection = "CONFIG_CHANNEL")
@TypeAlias("ConfigChannel")
public class ChannelConfigDoc extends ChannelConfig {

	private static final long serialVersionUID = -6368905475787041196L;

	@Id
	private String id;

	private String domain;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isReadOnly() {
		return (this.isSandbox() || this.isShared()) && !ArgUtil.areEqual(domain, AppContextUtil.getTenant());
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}
