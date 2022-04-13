package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface AccountConfigAnalyticService {

  void checkChangesInAnalytic(
      List<AnalyticAxisByCompany> initialList, List<AnalyticAxisByCompany> modifiedList)
      throws AxelorException;
}
