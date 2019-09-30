/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.TeamTaskServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class TeamTaskBusinessServiceImpl extends TeamTaskServiceImpl
    implements TeamTaskBusinessService {

  private PriceListLineRepository priceListLineRepository;

  @Inject
  public TeamTaskBusinessServiceImpl(PriceListLineRepository priceListLineRepository) {
    this.priceListLineRepository = priceListLineRepository;
  }

  @Override
  public TeamTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo) {
    TeamTask task = create(saleOrderLine.getFullName() + "_task", project, assignedTo);
    task.setProduct(saleOrderLine.getProduct());
    task.setUnit(saleOrderLine.getUnit());
    task.setCurrency(project.getClientPartner().getCurrency());
    if (project.getPriceList() != null) {
      PriceListLine line =
          priceListLineRepository.findByPriceListAndProduct(
              project.getPriceList(), saleOrderLine.getProduct());
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
