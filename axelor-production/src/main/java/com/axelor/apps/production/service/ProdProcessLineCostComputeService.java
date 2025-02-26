package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;

public interface ProdProcessLineCostComputeService {

  BigDecimal computeLineHourlyCost(ProdProcessLine prodProcessLine, BigDecimal qtyToProduce)
      throws AxelorException;
}
