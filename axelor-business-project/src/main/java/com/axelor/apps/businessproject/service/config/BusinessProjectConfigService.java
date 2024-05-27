package com.axelor.apps.businessproject.service.config;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.businessproject.db.BusinessProjectConfig;

public interface BusinessProjectConfigService {

  BusinessProjectConfig getBusinessProjectConfig(Company company) throws AxelorException;
}
