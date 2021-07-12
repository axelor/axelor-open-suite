package com.axelor.apps.sale.xml.mappers;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.xml.models.ExportedConfiguratorCreator;

public interface ExportedConfiguratorCreatorMapper {

  ExportedConfiguratorCreator mapFrom(ConfiguratorCreator configuratorCreator);

  ConfiguratorCreator mapFrom(ExportedConfiguratorCreator exportedConfiguratorCreator);
}
