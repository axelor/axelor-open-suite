/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class SaleOrderLineProjectServiceImpl extends SaleOrderLineServiceSupplyChainImpl
    implements SaleOrderLineProjectService {

  @Inject private SaleOrderLineRepository saleOrderLineRepo;

  @Transactional
  @Override
  public void setProject(List<Long> saleOrderLineIds, Project project) {

    if (saleOrderLineIds != null) {

      List<SaleOrderLine> saleOrderLineList =
          saleOrderLineRepo.all().filter("self.id in ?1", saleOrderLineIds).fetch();

      for (SaleOrderLine line : saleOrderLineList) {
        line.setProject(project);
        saleOrderLineRepo.save(line);
      }
    }
  }

  @Override
  public SaleOrderLine createAnalyticDistributionWithTemplate(SaleOrderLine saleOrderLine) {

    SaleOrderLine soLine = super.createAnalyticDistributionWithTemplate(saleOrderLine);
    List<AnalyticMoveLine> analyticMoveLineList = soLine.getAnalyticMoveLineList();

    if (soLine.getProject() != null && analyticMoveLineList != null) {
      analyticMoveLineList.forEach(analyticLine -> analyticLine.setProject(soLine.getProject()));
    }
    return soLine;
  }
}
