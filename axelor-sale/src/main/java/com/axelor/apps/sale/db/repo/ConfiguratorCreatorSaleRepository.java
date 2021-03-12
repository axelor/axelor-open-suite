package com.axelor.apps.sale.db.repo;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportService;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class ConfiguratorCreatorSaleRepository extends ConfiguratorCreatorRepository {

  @Override
  public ConfiguratorCreator copy(ConfiguratorCreator entity, boolean deep) {
    ConfiguratorCreator copy = super.copy(entity, deep);
    copy.setCopyNeedingUpdate(true);
    copy.setName(copy.getName() + " (" + I18n.get(IExceptionMessage.COPY) + ")");
    return copy;
  }

  @Override
  public ConfiguratorCreator save(ConfiguratorCreator entity) {
    try {
      if (entity.getCopyNeedingUpdate()) {
        entity = super.save(entity);
        entity.setCopyNeedingUpdate(false);
        Beans.get(ConfiguratorCreatorImportService.class).fixAttributesName(entity);
        ConfiguratorCreatorService configuratorCreatorService =
            Beans.get(ConfiguratorCreatorService.class);
        configuratorCreatorService.updateAttributes(entity);
        configuratorCreatorService.updateIndicators(entity);
        return entity;
      } else {
        return super.save(entity);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new PersistenceException(e);
    }
  }
}
