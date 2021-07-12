package com.axelor.apps.sale.xml.adapters;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.xml.models.AdaptedConfiguratorCreator;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ConfiguratorCreatorXmlAdapter
    extends XmlAdapter<AdaptedConfiguratorCreator, ConfiguratorCreator> {

  @Override
  public AdaptedConfiguratorCreator marshal(ConfiguratorCreator configuratorCreator)
      throws Exception {
    AdaptedConfiguratorCreator exportedCC = new AdaptedConfiguratorCreator();

    exportedCC.setId(configuratorCreator.getId());
    exportedCC.setName(configuratorCreator.getName());
    exportedCC.setAttributes(configuratorCreator.getAttributes());
    exportedCC.setConfiguratorProductFormulaList(
        configuratorCreator.getConfiguratorProductFormulaList());
    exportedCC.setConfiguratorSOLineFormulaList(
        configuratorCreator.getConfiguratorSOLineFormulaList());
    exportedCC.setAuthorizedUserSet(configuratorCreator.getAuthorizedUserSet());
    exportedCC.setAuthorizedGroupSet(configuratorCreator.getAuthorizedGroupSet());
    exportedCC.setGenerateProduct(configuratorCreator.getGenerateProduct());
    exportedCC.setQtyFormula(configuratorCreator.getQtyFormula());
    exportedCC.setIsActive(configuratorCreator.getIsActive());
    exportedCC.setCopyNeedingUpdate(configuratorCreator.getCopyNeedingUpdate());
    exportedCC.setAttrs(configuratorCreator.getAttrs());

    return exportedCC;
  }

  @Override
  public ConfiguratorCreator unmarshal(AdaptedConfiguratorCreator adaptedConfiguratorCreator)
      throws Exception {

    ConfiguratorCreator configuratorCreator = new ConfiguratorCreator();

    configuratorCreator.setImportId(adaptedConfiguratorCreator.getId().toString());
    configuratorCreator.setName(adaptedConfiguratorCreator.getName());
    configuratorCreator.setAttributes(adaptedConfiguratorCreator.getAttributes());
    configuratorCreator.setConfiguratorProductFormulaList(
        adaptedConfiguratorCreator.getConfiguratorProductFormulaList());
    configuratorCreator.setConfiguratorSOLineFormulaList(
        adaptedConfiguratorCreator.getConfiguratorSOLineFormulaList());
    configuratorCreator.setAuthorizedUserSet(adaptedConfiguratorCreator.getAuthorizedUserSet());
    configuratorCreator.setAuthorizedGroupSet(adaptedConfiguratorCreator.getAuthorizedGroupSet());
    configuratorCreator.setGenerateProduct(adaptedConfiguratorCreator.getGenerateProduct());
    configuratorCreator.setQtyFormula(adaptedConfiguratorCreator.getQtyFormula());
    configuratorCreator.setIsActive(adaptedConfiguratorCreator.getIsActive());
    configuratorCreator.setCopyNeedingUpdate(adaptedConfiguratorCreator.getCopyNeedingUpdate());
    configuratorCreator.setAttrs(adaptedConfiguratorCreator.getAttrs());
    return configuratorCreator;
  }
}
