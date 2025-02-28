package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import java.math.BigDecimal;

public interface ProdProcessComputationService {

  long getLeadTime(ProdProcess prodProcess, BigDecimal qty) throws AxelorException;
}
