package com.axelor.apps.businessproject.service.config;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.businessproject.db.BusinessProjectConfig;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.i18n.I18n;

public class BusinessProjectConfigServiceImpl implements BusinessProjectConfigService {

  @Override
  public BusinessProjectConfig getBusinessProjectConfig(Company company) throws AxelorException {

    BusinessProjectConfig businessProjectConfig = company.getBusinessProjectConfig();

    if (businessProjectConfig == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.BUSINESS_PROJECT_CONFIG_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return businessProjectConfig;
  }
}
