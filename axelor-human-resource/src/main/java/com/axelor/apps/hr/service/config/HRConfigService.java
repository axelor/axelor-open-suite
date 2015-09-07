package com.axelor.apps.hr.service.config;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.HRConfigRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class HRConfigService extends HRConfigRepository{
	public HRConfig getHRConfig(Company company) throws AxelorException  {
		HRConfig hrConfig = company.getHrConfig();

		if(hrConfig == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.HR_CONFIG_1), company),IException.CONFIGURATION_ERROR);
		}
		return hrConfig;
	}

}
