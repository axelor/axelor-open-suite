package com.axelor.apps.production.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.axelor.apps.production.xml.models.ProductionAdaptedConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorCreator;

public class ProductionConfiguratorCreatorAdapter
    extends XmlAdapter<ProductionAdaptedConfiguratorCreator, ConfiguratorCreator> {

  @Override
  public ProductionAdaptedConfiguratorCreator marshal(ConfiguratorCreator v) throws Exception {

    return new ProductionAdaptedConfiguratorCreator(v, v.getConfiguratorBom());
  }

  @Override
  public ConfiguratorCreator unmarshal(ProductionAdaptedConfiguratorCreator v) throws Exception {
    return v.toConfiguratorCreator();
  }
}
