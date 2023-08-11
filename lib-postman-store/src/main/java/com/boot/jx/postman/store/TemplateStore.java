package com.boot.jx.postman.store;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PostmanPackages.TemplateResolver;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.model.ITemplates.BasicTemplate;
import com.boot.jx.postman.model.ITemplates.TemplateGeneric;
import com.boot.utils.ArgUtil;

@Component
public class TemplateStore implements TemplateResolver {

	@Autowired
	protected QuickStore commonMongoTemplate;

	@Override
	public BasicTemplate get(String templateId) {
		HSMTemplateDoc x = commonMongoTemplate.findById(templateId, HSMTemplateDoc.class);
		return x;
	}

	public BasicTemplate resolve(CommonTemplateMeta template) {

		if (ArgUtil.is(template.getId())) {
			if (template.getId().startsWith("QR=")) {
				QuickReply qr = commonMongoTemplate.findById(template.getId().split("QR=")[1], QuickReply.class);
				return createTemplateDoc(template, qr);
			} else {
				return get(template.getId());
			}
		} else if (ArgUtil.is(template.getCode())) {

			if (template.getCode().startsWith("QR=")) {
				QuickReply qr = commonMongoTemplate.findByCode(template.getCode().split("QR=")[1], QuickReply.class);
				return createTemplateDoc(template, qr);
			} else {
				List<HSMTemplateDoc> temps = commonMongoTemplate.find(
						CommonMongoQueryBuilder.collection(HSMTemplateDoc.class).where("code", template.getCode()));
				if (ArgUtil.is(temps)) {
					HSMTemplateDoc resolvedTemplate = null;
					if (temps.size() > 1) {
						for (HSMTemplateDoc hsmTemplate3rdParty : temps) {
							if (ArgUtil.areEqual(hsmTemplate3rdParty.getLang(), template.getLang())) {
								resolvedTemplate = hsmTemplate3rdParty;
								break;
							} else if (ArgUtil.is(hsmTemplate3rdParty.getLang())) {
								resolvedTemplate = hsmTemplate3rdParty;
							}
						}
					} else {
						resolvedTemplate = temps.get(0);
					}
					return resolvedTemplate;
				}
			}

		}
		return null;
	}

	private HSMTemplateDoc createTemplateDoc(CommonTemplateMeta template, QuickReply qr) {
		if(ArgUtil.is(qr)) {
			HSMTemplateDoc tmpl = new HSMTemplateDoc();
			tmpl.setId(template.getId());
			tmpl.setCode(qr.getCode());
			tmpl.setCategory(qr.getCategory());
			tmpl.setTemplate(qr.getTemplate());
			return tmpl;
		}
		return null;
	}

	@Override
	public BasicTemplate get(CommonTemplateMeta template) {
		BasicTemplate basicTemplate = resolve(template);
		if (ArgUtil.is(basicTemplate)) {
			template.setCode(basicTemplate.getCode());
			template.setId(basicTemplate.getId());
		}
		return basicTemplate;
	}

}
