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
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ProjectedStockServiceImpl implements ProjectedStockService {

  @Transactional
  @Override
  public List<MrpLine> createProjectedStock(Long productId, Long companyId, Long stockLocationId)
      throws AxelorException {
    Product product = Beans.get(ProductRepository.class).find(productId);
    Company company = Beans.get(CompanyRepository.class).find(companyId);
    StockLocation stockLocation = Beans.get(StockLocationRepository.class).find(stockLocationId);
    Mrp mrp = new Mrp();
    mrp.setStockLocation(stockLocation);
    mrp.addProductSetItem(product);
    mrp = Beans.get(MrpRepository.class).save(mrp);
    mrp = Beans.get(MrpService.class).completeProjectedStock(mrp, product);
    
    List<MrpLine> mrpLineList =
        Query.of(MrpLine.class)
            .filter("self.mrp.id = :mrpId AND self.product.id = :productId")
            .bind("mrpId", mrp.getId())
            .bind("productId", productId)
            .order("maturityDate")
            .order("mrpLineType.typeSelect")
            .order("mrpLineType.sequence")
            .fetch();
    
    return mrpLineList;
  }
}
