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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ProjectedStockServiceImpl implements ProjectedStockService {

  @Inject StockLocationRepository stockLocationRepository;

  @Transactional
  @Override
  public List<MrpLine> createProjectedStock(Long productId, Long companyId, Long stockLocationId)
      throws AxelorException {
    Product product = Beans.get(ProductRepository.class).find(productId);
    Company company = Beans.get(CompanyRepository.class).find(companyId);
    StockLocation stockLocation = stockLocationRepository.find(stockLocationId);
    Mrp mrp = new Mrp();
    mrp.setStockLocation(findStockLocation(company, stockLocation));
    // If a company has no stockLocation
    if (mrp.getStockLocation() == null) {
      return null;
    }
    mrp.addProductSetItem(product);
    mrp = Beans.get(MrpRepository.class).save(mrp);
    mrp = Beans.get(MrpService.class).completeProjectedStock(mrp, product, company, stockLocation);

    List<MrpLine> mrpLineList =
        Beans.get(MrpLineRepository.class)
            .all()
            .filter("self.mrp = ?1 AND self.product = ?2", mrp, product)
            .order("maturityDate")
            .order("mrpLineType.typeSelect")
            .order("mrpLineType.sequence")
            .order("id")
            .fetch();

    for (MrpLine mrpLine : mrpLineList) {
      mrpLine.setCompany(mrpLine.getStockLocation().getCompany());
    }
    return mrpLineList;
  }

  protected StockLocation findStockLocation(Company company, StockLocation stockLocation) {
    if (stockLocation != null) {
      return stockLocation;
    } else if (company != null) {
      return stockLocationRepository
          .all()
          .filter("self.company.id = ?1", company.getId())
          .fetchOne();
    }
    return stockLocationRepository.all().fetchOne();
  }
}
