package com.axelor.apps.sale.xml.adapters;

import com.axelor.apps.sale.db.ConfiguratorProductFormula;
import com.axelor.apps.sale.xml.models.AdaptedConfiguratorProductFormula;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.google.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ConfiguratorProductFormulaXmlAdapter
    extends XmlAdapter<AdaptedConfiguratorProductFormula, ConfiguratorProductFormula> {

  private MetaFieldRepository metaFieldRepository;
  
  public ConfiguratorProductFormulaXmlAdapter() {}
  
  @Inject
  public ConfiguratorProductFormulaXmlAdapter(MetaFieldRepository metaFieldRepository) {

    this.metaFieldRepository = metaFieldRepository;
  }

  @Override
  public AdaptedConfiguratorProductFormula marshal(
      ConfiguratorProductFormula configuratorProductFormula) throws Exception {
    AdaptedConfiguratorProductFormula adaptedConfiguratorProductFormula =
        new AdaptedConfiguratorProductFormula();
    adaptedConfiguratorProductFormula.setFormula(configuratorProductFormula.getFormula());
    adaptedConfiguratorProductFormula.setMetaFieldName(
        configuratorProductFormula.getMetaField().getName());
    adaptedConfiguratorProductFormula.setShowOnConfigurator(
        configuratorProductFormula.getShowOnConfigurator());
    adaptedConfiguratorProductFormula.setConfiguratorCreatorImportId(
        configuratorProductFormula.getProductCreator().getId());
    return adaptedConfiguratorProductFormula;
  }

  @Override
  public ConfiguratorProductFormula unmarshal(
      AdaptedConfiguratorProductFormula adaptedConfiguratorProductFormula) throws Exception {
    ConfiguratorProductFormula configuratorProductFormula = new ConfiguratorProductFormula();
    configuratorProductFormula.setFormula(adaptedConfiguratorProductFormula.getFormula());
    configuratorProductFormula.setMetaField(
        metaFieldRepository.findByName(adaptedConfiguratorProductFormula.getMetaFieldName()));
    configuratorProductFormula.setShowOnConfigurator(
        adaptedConfiguratorProductFormula.getShowOnConfigurator());
    configuratorProductFormula.setImportId(adaptedConfiguratorProductFormula.getId().toString());
    return configuratorProductFormula;
  }
}
