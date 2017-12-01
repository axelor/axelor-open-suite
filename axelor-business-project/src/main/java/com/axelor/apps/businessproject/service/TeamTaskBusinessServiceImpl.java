package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.project.service.TeamTaskServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class TeamTaskBusinessServiceImpl extends TeamTaskServiceImpl {

    private PriceListLineRepository priceListLineRepository;

    @Inject
    public TeamTaskBusinessServiceImpl(PriceListLineRepository priceListLineRepository) {
        this.priceListLineRepository = priceListLineRepository;
    }

    @Override
    public TeamTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo) {
        TeamTask task = super.create(saleOrderLine, project, assignedTo);
        task.setProduct(saleOrderLine.getProduct());
        task.setUnit(saleOrderLine.getUnit());
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
