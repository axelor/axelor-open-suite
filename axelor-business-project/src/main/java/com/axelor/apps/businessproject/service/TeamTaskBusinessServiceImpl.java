package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.project.service.TeamTaskServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class TeamTaskBusinessServiceImpl extends TeamTaskServiceImpl {

    private PriceListLineRepository priceListLineRepository;
    private CurrencyConversionService currencyConversionService;

    @Inject
    public TeamTaskBusinessServiceImpl(PriceListLineRepository priceListLineRepository, CurrencyConversionService currencyConversionService) {
        this.priceListLineRepository = priceListLineRepository;
        this.currencyConversionService = currencyConversionService;
    }

    public TeamTask create(String subject, Project project, SaleOrderLine saleOrderLine) {
        TeamTask task = create(subject, project);
        task.setProduct(saleOrderLine.getProduct());
        task.setCurrency(project.getClientPartner().getCurrency());
        if (project.getPriceList() != null) {
            PriceListLine line = priceListLineRepository.findByPriceListAndProduct(project.getPriceList(), saleOrderLine.getProduct());
            if (line != null) {
                task.setSalePrice(line.getAmount());
            }
        }
        if (task.getSalePrice() == null) {
            task.setSalePrice(saleOrderLine.getProduct().getSalePrice());
        }
        task.setQuantity(saleOrderLine.getQty());
        return task;
    }
}
