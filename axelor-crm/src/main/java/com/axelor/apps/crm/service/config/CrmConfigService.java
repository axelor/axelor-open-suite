package com.axelor.apps.crm.service.config;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.crm.db.CrmConfig;
import com.axelor.apps.crm.db.repo.CrmConfigRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class CrmConfigService extends CrmConfigRepository{
	public CrmConfig getCrmConfig(Company company) throws AxelorException  {
		CrmConfig crmConfig = company.getCrmConfig();

		if(crmConfig == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CRM_CONFIG_1), company.getName()),IException.CONFIGURATION_ERROR);
		}
		return crmConfig;
	}

}
