package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.supplychain.db.Mrp;

public interface MrpCallTenderService {

  CallTender generateCallTenderForAllLines(Mrp mrp) throws AxelorException;

  CallTender generateCallTenderForSelectedLines(Mrp mrp) throws AxelorException;
}
