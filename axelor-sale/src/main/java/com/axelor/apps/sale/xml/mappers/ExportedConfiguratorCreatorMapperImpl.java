package com.axelor.apps.sale.xml.mappers;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.xml.models.ExportedConfiguratorCreator;

public class ExportedConfiguratorCreatorMapperImpl implements ExportedConfiguratorCreatorMapper {

  @Override
  public ExportedConfiguratorCreator mapFrom(ConfiguratorCreator cc) {

    ExportedConfiguratorCreator exportedCC = new ExportedConfiguratorCreator();

    exportedCC.setId(cc.getId());
    exportedCC.setName(cc.getName());
    exportedCC.setAttributes(cc.getAttributes());
    exportedCC.setConfiguratorProductFormulaList(cc.getConfiguratorProductFormulaList());
    exportedCC.setConfiguratorSOLineFormulaList(cc.getConfiguratorSOLineFormulaList());
    exportedCC.setAuthorizedUserSet(cc.getAuthorizedUserSet());
    exportedCC.setAuthorizedGroupSet(cc.getAuthorizedGroupSet());
    exportedCC.setGenerateProduct(cc.getGenerateProduct());
    exportedCC.setQtyFormula(cc.getQtyFormula());
    exportedCC.setIsActive(cc.getIsActive());
    exportedCC.setCopyNeedingUpdate(cc.getCopyNeedingUpdate());
    exportedCC.setAttrs(cc.getAttrs());

    return exportedCC;
  }

  @Override
  public ConfiguratorCreator mapFrom(ExportedConfiguratorCreator exportedConfiguratorCreator) {
    // TODO Auto-generated method stub
    return null;
  }
}
