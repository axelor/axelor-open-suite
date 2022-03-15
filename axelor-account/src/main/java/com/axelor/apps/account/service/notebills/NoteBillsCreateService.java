package com.axelor.apps.account.service.notebills;

import com.axelor.apps.account.db.NoteBills;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;

public interface NoteBillsCreateService {

  NoteBills createNoteBills(Company company, Partner partner, Batch batch) throws AxelorException;
}
