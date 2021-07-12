package com.axelor.apps.sale.xml.adapters;

import com.axelor.apps.sale.xml.models.AdaptedMetaJsonField;
import com.axelor.meta.db.MetaJsonField;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class MetaJsonFieldXmlAdapter extends XmlAdapter<AdaptedMetaJsonField, MetaJsonField> {

  @Override
  public AdaptedMetaJsonField marshal(MetaJsonField m) throws Exception {
    AdaptedMetaJsonField em = new AdaptedMetaJsonField();

    em.setId(m.getId());
    em.setName(m.getName());
    em.setTitle(m.getTitle());
    em.setType(m.getType());
    em.setDefaultValue(m.getDefaultValue());
    em.setModel(m.getModel());
    em.setModelField(m.getModelField());
    // TODO: MetaJsonModel
    em.setSelection(m.getSelection());
    em.setWidget(m.getWidget());
    em.setHelp(m.getHelp());
    em.setShowIf(m.getShowIf());
    em.setHideIf(m.getHideIf());
    em.setRequiredIf(m.getRequiredIf());
    em.setIncludeIf(m.getIncludeIf());
    em.setContextField(m.getContextField());
    em.setContextFieldTarget(m.getContextFieldTarget());
    em.setContextFieldTargetName(m.getContextFieldTargetName());
    em.setContextFieldValue(m.getContextFieldValue());
    em.setContextFieldTitle(m.getContextFieldTitle());
    em.setHidden(m.getHidden());
    em.setRequired(m.getRequired());
    em.setReadonly(m.getReadonly());
    em.setNameField(m.getNameField());
    em.setVisibleInGrid(m.getVisibleInGrid());
    em.setMinSize(m.getMinSize());
    em.setMaxSize(m.getMaxSize());
    em.setPrecision(m.getPrecision());
    em.setScale(m.getScale());
    em.setSequence(m.getSequence());
    em.setColumnSequence(m.getColumnSequence());
    em.setRegex(m.getRegex());
    em.setValueExpr(m.getValueExpr());
    em.setTargetModel(m.getTargetModel());
    em.setEnumType(m.getEnumType());
    em.setFormView(m.getFormView());
    em.setGridView(m.getGridView());
    em.setDomain(m.getDomain());

    return em;
  }

  @Override
  public MetaJsonField unmarshal(AdaptedMetaJsonField adaptedMetaJsonField) throws Exception {
    MetaJsonField metaJsonField = new MetaJsonField();
    metaJsonField.setId(adaptedMetaJsonField.getId());
    metaJsonField.setName(adaptedMetaJsonField.getName());
    metaJsonField.setTitle(adaptedMetaJsonField.getTitle());
    metaJsonField.setType(adaptedMetaJsonField.getType());
    metaJsonField.setDefaultValue(adaptedMetaJsonField.getDefaultValue());
    metaJsonField.setModel(adaptedMetaJsonField.getModel());
    metaJsonField.setModelField(adaptedMetaJsonField.getModelField());
    // TODO: MetaJsonModel
    metaJsonField.setSelection(adaptedMetaJsonField.getSelection());
    metaJsonField.setWidget(adaptedMetaJsonField.getWidget());
    metaJsonField.setHelp(adaptedMetaJsonField.getHelp());
    metaJsonField.setShowIf(adaptedMetaJsonField.getShowIf());
    metaJsonField.setHideIf(adaptedMetaJsonField.getHideIf());
    metaJsonField.setRequiredIf(adaptedMetaJsonField.getRequiredIf());
    metaJsonField.setIncludeIf(adaptedMetaJsonField.getIncludeIf());
    metaJsonField.setContextField(adaptedMetaJsonField.getContextField());
    metaJsonField.setContextFieldTarget(adaptedMetaJsonField.getContextFieldTarget());
    metaJsonField.setContextFieldTargetName(adaptedMetaJsonField.getContextFieldTargetName());
    metaJsonField.setContextFieldValue(adaptedMetaJsonField.getContextFieldValue());
    metaJsonField.setContextFieldTitle(adaptedMetaJsonField.getContextFieldTitle());
    metaJsonField.setHidden(adaptedMetaJsonField.getHidden());
    metaJsonField.setRequired(adaptedMetaJsonField.getRequired());
    metaJsonField.setReadonly(adaptedMetaJsonField.getReadonly());
    metaJsonField.setNameField(adaptedMetaJsonField.getNameField());
    metaJsonField.setVisibleInGrid(adaptedMetaJsonField.getVisibleInGrid());
    metaJsonField.setMinSize(adaptedMetaJsonField.getMinSize());
    metaJsonField.setMaxSize(adaptedMetaJsonField.getMaxSize());
    metaJsonField.setPrecision(adaptedMetaJsonField.getPrecision());
    metaJsonField.setScale(adaptedMetaJsonField.getScale());
    metaJsonField.setSequence(adaptedMetaJsonField.getSequence());
    metaJsonField.setColumnSequence(adaptedMetaJsonField.getColumnSequence());
    metaJsonField.setRegex(adaptedMetaJsonField.getRegex());
    metaJsonField.setValueExpr(adaptedMetaJsonField.getValueExpr());
    metaJsonField.setTargetModel(adaptedMetaJsonField.getTargetModel());
    metaJsonField.setEnumType(adaptedMetaJsonField.getEnumType());
    metaJsonField.setFormView(adaptedMetaJsonField.getFormView());
    metaJsonField.setGridView(adaptedMetaJsonField.getGridView());
    metaJsonField.setDomain(adaptedMetaJsonField.getDomain());
    return metaJsonField;
  }
}
