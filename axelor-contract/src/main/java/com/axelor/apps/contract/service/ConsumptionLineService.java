package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.contract.db.ConsumptionLine;

public interface ConsumptionLineService {

    /**
     * Fill ConsumptionLine with Product information.
     * @param line to fill.
     * @param product to get information.
     * @return ConsumptionLine filled with Product information.
     */
    ConsumptionLine fill(ConsumptionLine line, Product product);

}
