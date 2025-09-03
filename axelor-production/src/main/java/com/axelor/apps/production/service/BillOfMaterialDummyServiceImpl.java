/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BillOfMaterialDummyServiceImpl implements BillOfMaterialDummyService {

  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderLineDetailsRepository saleOrderLineDetailsRepository;

  @Inject
  public BillOfMaterialDummyServiceImpl(
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineDetailsRepository saleOrderLineDetailsRepository) {
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderLineDetailsRepository = saleOrderLineDetailsRepository;
  }

  @Override
  public boolean getIsUsedInSaleOrder(BillOfMaterial billOfMaterial) {
    List<BillOfMaterialLine> billOfMaterialLineList = billOfMaterial.getBillOfMaterialLineList();
    if (CollectionUtils.isEmpty(billOfMaterialLineList)) {
      return false;
    }
    String idList =
        billOfMaterialLineList.stream()
            .map(BillOfMaterialLine::getId)
            .map(Object::toString)
            .collect(Collectors.joining(","));
    String filter =
        "self.billOfMaterialLine IS NOT NULL AND self.billOfMaterialLine IN (" + idList + ")";
    return CollectionUtils.isNotEmpty(saleOrderLineRepository.all().filter(filter).fetch())
        || CollectionUtils.isNotEmpty(saleOrderLineDetailsRepository.all().filter(filter).fetch());
  }
}
