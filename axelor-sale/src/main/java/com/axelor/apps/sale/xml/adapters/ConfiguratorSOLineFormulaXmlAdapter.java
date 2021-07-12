package com.axelor.apps.sale.xml.adapters;

import com.axelor.apps.sale.db.ConfiguratorSOLineFormula;
import com.axelor.apps.sale.xml.models.AdaptedConfiguratorSOLineFormula;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.google.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ConfiguratorSOLineFormulaXmlAdapter
    extends XmlAdapter<AdaptedConfiguratorSOLineFormula, ConfiguratorSOLineFormula> {

  private MetaFieldRepository metaFieldRepository;
  
  public ConfiguratorSOLineFormulaXmlAdapter() {}
  @Inject
  public ConfiguratorSOLineFormulaXmlAdapter(MetaFieldRepository metaFieldRepository) {

    this.metaFieldRepository = metaFieldRepository;
  }

  @Override
  public AdaptedConfiguratorSOLineFormula marshal(
      ConfiguratorSOLineFormula configuratorSOLineFormula) throws Exception {
    AdaptedConfiguratorSOLineFormula adaptedConfiguratorSOLineFormula =
        new AdaptedConfiguratorSOLineFormula();
    adaptedConfiguratorSOLineFormula.setFormula(configuratorSOLineFormula.getFormula());
    adaptedConfiguratorSOLineFormula.setMetaFieldName(
        configuratorSOLineFormula.getMetaField().getName());
    adaptedConfiguratorSOLineFormula.setShowOnConfigurator(
        configuratorSOLineFormula.getShowOnConfigurator());
    adaptedConfiguratorSOLineFormula.setUpdateFromSelect(
        configuratorSOLineFormula.getUpdateFromSelect());
    adaptedConfiguratorSOLineFormula.setConfiguratorCreatorImportId(
        configuratorSOLineFormula.getSoLineCreator().getId());
    return adaptedConfiguratorSOLineFormula;
  }

  @Override
  public ConfiguratorSOLineFormula unmarshal(
      AdaptedConfiguratorSOLineFormula adaptedConfiguratorSOLineFormula) throws Exception {

    ConfiguratorSOLineFormula configuratorSOLineForumla = new ConfiguratorSOLineFormula();
    configuratorSOLineForumla.setFormula(adaptedConfiguratorSOLineFormula.getFormula());
    configuratorSOLineForumla.setMetaField(
        metaFieldRepository.findByName(adaptedConfiguratorSOLineFormula.getMetaFieldName()));
    configuratorSOLineForumla.setShowOnConfigurator(
        adaptedConfiguratorSOLineFormula.getShowOnConfigurator());
    configuratorSOLineForumla.setImportId(adaptedConfiguratorSOLineFormula.getId().toString());
    configuratorSOLineForumla.setUpdateFromSelect(
        adaptedConfiguratorSOLineFormula.getUpdateFromSelect());

    return configuratorSOLineForumla;
  }
}
