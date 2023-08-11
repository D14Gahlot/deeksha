package com.boot.jx.postman.manager;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoQB;
import com.boot.jx.mongo.CommonMongoQB.CommonMongoQBimpl;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Client;
import com.boot.jx.postman.wa360.WA360Template;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;

@Component
public class ThirdPartyTemplateManager {

	@Autowired
	private WA360Client wa360Client;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	public void refreshWA360Templates(ChannelConfig channelConfig) {
		MapModel resp = wa360Client.fetchTemplates(channelConfig);

		List<WA360Template> wabaTemplates = resp.keyEntry("waba_templates").asList(WA360Template.class);

		CommonMongoQBimpl<HSMTemplate3rdParty> cmqb = CommonMongoQueryBuilder.collection(HSMTemplate3rdParty.class)
				.where(Criteria.where("channelId").is(channelConfig.getChannelId())).set("template.status", "deleted");

		commonMongoTemplate.update(cmqb);

		for (WA360Template wa360Template : wabaTemplates) {
			HSMTemplate3rdParty thirdPartyTemplate = toHSM3rdParty(channelConfig, wa360Template);
			commonMongoTemplate.save(thirdPartyTemplate);
			linkRefresh(thirdPartyTemplate, thirdPartyTemplate.getHsmTemplateId(),
					ArgUtil.parseAsString(thirdPartyTemplate.getTemplate().get("status"), Constants.BLANK));
		}
	}

	private HSMTemplate3rdParty toHSM3rdParty(ChannelConfig channelConfig, WA360Template wa360Template) {
		String id = String.format("%s/%s/%s", channelConfig.getChannelId(), wa360Template.getName(),
				wa360Template.getLanguage());
		HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(id, HSMTemplate3rdParty.class);
		if (!ArgUtil.is(thirdPartyTemplate)) {
			thirdPartyTemplate = new HSMTemplate3rdParty();
			thirdPartyTemplate.setId(id);
		}

		thirdPartyTemplate.setChannelId(channelConfig.getChannelId());
		thirdPartyTemplate.setCode(wa360Template.getName());
		thirdPartyTemplate.setLang(wa360Template.getLanguage());

		thirdPartyTemplate.setCategory(wa360Template.getCategory());
		thirdPartyTemplate.setContactType(ArgUtil.parseAsString(channelConfig.getContactType()));
		thirdPartyTemplate.setChannelType(channelConfig.getChannelType());

		thirdPartyTemplate.setTemplate(JsonUtil.toMap(wa360Template));
		return thirdPartyTemplate;
	}

	public HSMTemplate3rdParty createhWA360Templates(ChannelConfig channelConfig,
			Map<String, Object> templateStructure) {
		MapModel resp = wa360Client.createTemplates(channelConfig, MapModel.from(templateStructure));
		return toHSM3rdParty(channelConfig, resp.as(WA360Template.class));
	}

	public HSMTemplate3rdParty deleteWA360Templates(ChannelConfig channelConfig, HSMTemplate3rdParty temp) {
		WA360Template x = JsonUtil.toObject(temp.getTemplate(), WA360Template.class);
		if (ArgUtil.is(x)) {
			if (!"deleted".equalsIgnoreCase(x.getStatus())) {
				wa360Client.deleteTemplates(channelConfig, x.getName());
			}
			commonMongoTemplate.remove(temp);
		}
		return temp;
	}

	public List<HSMTemplate3rdParty> getTemplates(ChannelConfig channelConfig, String code) {
		CommonMongoQBimpl<HSMTemplate3rdParty> q = CommonMongoQB.collection(HSMTemplate3rdParty.class)
				.where(Criteria.where("channelId").is(channelConfig.getChannelId()));

		if (ArgUtil.is(code)) {
			q.where("code", code);
		}

		return commonMongoTemplate.find(q);
	}

	public List<HSMTemplate3rdParty> getTemplates(ChannelConfig channelConfig) {
		return this.getTemplates(channelConfig, null);
	}

	public HSMTemplate3rdParty link(String thirdPartyTemplateId, String hsmTemplateId) {
		HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(thirdPartyTemplateId,
				HSMTemplate3rdParty.class);
		String hsmTemplateIdOld = thirdPartyTemplate.getHsmTemplateId();
		thirdPartyTemplate.setHsmTemplateId(hsmTemplateId);
		commonMongoTemplate.save(thirdPartyTemplate);

		this.linkRefresh(thirdPartyTemplate, hsmTemplateIdOld, null);
		this.linkRefresh(thirdPartyTemplate, hsmTemplateId,
				ArgUtil.parseAsString(thirdPartyTemplate.getTemplate().get("status"), Constants.BLANK));
		return thirdPartyTemplate;
	}

	public HSMTemplate3rdParty linkRefresh(HSMTemplate3rdParty thirdPartyTemplate, String hsmTemplateId,
			String status) {
		if (ArgUtil.is(hsmTemplateId)) {
			HSMTemplateDoc hsmTemplateDoc = commonMongoTemplate.findById(hsmTemplateId, HSMTemplateDoc.class);
			if (ArgUtil.is(hsmTemplateDoc)) {
				hsmTemplateDoc.approved(thirdPartyTemplate.getChannelId(), thirdPartyTemplate.getHsmTemplateId(),
						status);
				commonMongoTemplate.save(hsmTemplateDoc);
			}
		}
		return thirdPartyTemplate;
	}

	public HSMTemplate3rdParty varMap(String thirdPartyTemplateId, Map<String, Object> varMap) {
		HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(thirdPartyTemplateId,
				HSMTemplate3rdParty.class);
		thirdPartyTemplate.setVarMap(varMap);
		commonMongoTemplate.save(thirdPartyTemplate);
		return thirdPartyTemplate;
	}

}
