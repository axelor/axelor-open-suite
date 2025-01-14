package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;

public abstract class GenericApiCreateService {

  protected abstract void setData(Partner partner, String result) throws AxelorException;
}
