/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.service.BillOfMaterialComputeNameService;
import com.axelor.db.JPA;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillOfMaterialManagementRepository extends BillOfMaterialRepository {

  private final Map<String, BigDecimal> bomQtyCache = new HashMap<>();

  protected BillOfMaterialLineRepository billOfMaterialLineRepository;
  protected BillOfMaterialComputeNameService billOfMaterialComputeNameService;

  @Inject
  public BillOfMaterialManagementRepository(
      BillOfMaterialLineRepository billOfMaterialLineRepository,
      BillOfMaterialComputeNameService billOfMaterialComputeNameService) {

    this.billOfMaterialLineRepository = billOfMaterialLineRepository;
    this.billOfMaterialComputeNameService = billOfMaterialComputeNameService;
  }

  @Override
  public BillOfMaterial save(BillOfMaterial billOfMaterial) {
    billOfMaterial = super.save(billOfMaterial);
    billOfMaterial.setFullName(billOfMaterialComputeNameService.computeFullName(billOfMaterial));
    return billOfMaterial;
  }

  @Override
  public BillOfMaterial copy(BillOfMaterial entity, boolean deep) {

    BillOfMaterial copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setVersionNumber(1);
    copy.setOriginalBillOfMaterial(null);
    copy.setCostPrice(BigDecimal.ZERO);
    copy.clearCostSheetList();
    copy.clearBillOfMaterialLineList();

    List<BillOfMaterialLine> billOfMaterialLineList = entity.getBillOfMaterialLineList();
    if (billOfMaterialLineList != null && !billOfMaterialLineList.isEmpty()) {
      billOfMaterialLineList.forEach(
          boml ->
              copy.addBillOfMaterialLineListItem(billOfMaterialLineRepository.copy(boml, deep)));
    }

    return copy;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (context.get("_xShowUsedBOMPanel") != null && context.get("id") != null) {

      Long productId = (Long) context.get("id");
      Long bomId = (Long) json.get("id");

      String cacheKey = productId + "-" + bomId;
      BigDecimal totalQty = bomQtyCache.get(cacheKey);

      if (totalQty == null) {
        totalQty =
            (BigDecimal)
                JPA.em()
                    .createQuery(
                        "SELECT COALESCE(SUM(self.qty), 0) "
                            + "FROM BillOfMaterialLine self "
                            + "WHERE self.product.id = :productId "
                            + "AND self.billOfMaterialParent.id = :billOfMaterial")
                    .setParameter("productId", productId)
                    .setParameter("billOfMaterial", bomId)
                    .getSingleResult();

        bomQtyCache.put(cacheKey, totalQty);
      }
      json.put("billOfMaterialLineList.qty", totalQty);
    }
    return super.populate(json, context);
  }
}
