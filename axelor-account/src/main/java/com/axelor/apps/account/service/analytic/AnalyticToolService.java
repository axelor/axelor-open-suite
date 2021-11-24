package com.axelor.apps.account.service.analytic;

import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;

public interface AnalyticToolService {

  boolean isManageAnalytic(Company company) throws AxelorException;
}
