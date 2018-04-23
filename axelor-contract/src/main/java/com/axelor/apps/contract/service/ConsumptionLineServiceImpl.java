package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ConsumptionLineServiceImpl implements ConsumptionLineService {

    protected AppBaseService appBaseService;

    @Inject
    public ConsumptionLineServiceImpl(AppBaseService appBaseService) {
        this.appBaseService = appBaseService;
    }

    @Override
    public ConsumptionLine fill(ConsumptionLine line, Product product) {
        Preconditions.checkNotNull(product, I18n.get("The product for fill " +
                "the consumption line can't be empty or null."));
        line.setLineDate(appBaseService.getTodayDate());
        line.setProduct(product);
        line.setReference(product.getName());
        line.setUnit(product.getUnit());
        return line;
    }

}
