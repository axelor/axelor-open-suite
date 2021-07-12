package com.axelor.apps.sale.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.axelor.apps.sale.xml.models.ExportedMetaJsonField;
import com.axelor.meta.db.MetaJsonField;

public class MetaJsonFieldXmlAdapter extends XmlAdapter<ExportedMetaJsonField, MetaJsonField> {

	@Override
	public ExportedMetaJsonField marshal(MetaJsonField m) throws Exception {
		ExportedMetaJsonField  em = new ExportedMetaJsonField();
		
		em.setName(m.getName());
		em.setTitle(m.getTitle());
		em.setType(m.getType());
		em.setDefaultValue(m.getDefaultValue());
		em.setModel(m.getModel());
		em.setModelField(m.getModelField());
		em.setJsonModel(m.getJsonModel().c);
		return em;
	}

	@Override
	public MetaJsonField unmarshal(ExportedMetaJsonField arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
