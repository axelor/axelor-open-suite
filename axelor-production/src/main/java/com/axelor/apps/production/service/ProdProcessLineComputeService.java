package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;

public interface ProdProcessLineComputeService {
  BigDecimal computeMachineDuration(ProdProcessLine prodProcessLine, BigDecimal qtyToProduce);

  BigDecimal getNbCycles(BigDecimal maxCapacityPerCycle, BigDecimal qtyToProduce);
}
