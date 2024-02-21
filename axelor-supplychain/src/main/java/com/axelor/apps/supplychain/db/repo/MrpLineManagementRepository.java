/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplychain.db.MrpLine;
import java.util.Map;

public class MrpLineManagementRepository extends MrpLineRepository {

  /** set alert if purchase delivery date is to far from proposal */
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long mrpLineId = (Long) json.get("id");
    MrpLine mrpLine = find(mrpLineId);

    Map<String, Object> mrpLineMap = super.populate(json, context);
    if (mrpLine.getEstimatedDeliveryMrpLine() != null
        && mrpLine.getMaturityDate() != null
        && !mrpLine.getMaturityDate().isEqual(mrpLine.getDeliveryDelayDate())) {
      json.put("respectDeliveryDelayDate", mrpLine.getDeliveryDelayDate());
    } else if (mrpLine.getMrpLineType().getElementSelect()
            == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL_ESTIMATED_DELIVERY
        && mrpLine.getMaturityDate() != null
        && !mrpLine.getMaturityDate().isEqual(mrpLine.getEstimatedDeliveryDate())) {
      json.put("respectDeliveryDelayDate", mrpLine.getEstimatedDeliveryDate());
    }

    return mrpLineMap;
  }

  @Override
  public MrpLine save(MrpLine entity) {
    if (entity.getIsEditedByUser()
        && entity.getMaturityDate() != null
        && entity.getEstimatedDeliveryMrpLine() != null) {
      Product product = entity.getProduct();
      MrpLine mrpLine = entity.getEstimatedDeliveryMrpLine();
      mrpLine.setMaturityDate(entity.getMaturityDate().plusDays(product.getSupplierDeliveryTime()));
      mrpLine.setQty(entity.getQty());
      mrpLine.setIsEditedByUser(true);
      mrpLine.setEstimatedDeliveryDate(
          entity.getMaturityDate().plusDays(product.getSupplierDeliveryTime()));
      entity.setDeliveryDelayDate(
          mrpLine.getMaturityDate().minusDays(product.getSupplierDeliveryTime()));
    }
    return super.save(entity);
  }
}
