package com.axelor.apps.sale.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.xml.models.AdaptedConfiguratorCreator;

public class ConfiguratorCreatorXmlAdapter
    extends XmlAdapter<AdaptedConfiguratorCreator, ConfiguratorCreator> {

  @Override
  public AdaptedConfiguratorCreator marshal(ConfiguratorCreator configuratorCreator)
      throws Exception {

    return new AdaptedConfiguratorCreator(configuratorCreator);
  }

  @Override
  public ConfiguratorCreator unmarshal(AdaptedConfiguratorCreator adaptedConfiguratorCreator)
      throws Exception {
    return adaptedConfiguratorCreator.toConfiguratorCreator();
  }
}
