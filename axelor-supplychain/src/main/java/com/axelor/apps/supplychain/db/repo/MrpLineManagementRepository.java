/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.inject.Beans;
import java.util.Map;

public class MrpLineManagementRepository extends MrpLineRepository {

  /** set alert if purchase delivery date is to far from proposal */
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long mrpLineId = (Long) json.get("id");
    MrpLine mrpLine = find(mrpLineId);

    Map<String, Object> mrpLineMap = super.populate(json, context);
    if (PurchaseOrder.class.getName().equals(mrpLine.getProposalSelect())) {
      PurchaseOrder purchaseOrder =
          Beans.get(PurchaseOrderRepository.class).find(mrpLine.getProposalSelectId());
      mrpLine.setIsOutDayNbBetweenPurchaseAndProposal(
          mrpLine.getProduct() != null
              && mrpLine.getMaturityDate() != null
              && (purchaseOrder.getDeliveryDate() == null
                  || mrpLine.getProduct().getMrpFamily() == null
                  || Math.abs(
                          mrpLine.getMaturityDate().toEpochDay()
                              - purchaseOrder.getDeliveryDate().toEpochDay())
                      > mrpLine.getProduct().getMrpFamily().getDayNbBetweenPurchaseAndProposal()));
      json.put(
          "isOutDayNbBetweenPurchaseAndProposal",
          mrpLine.getIsOutDayNbBetweenPurchaseAndProposal());
    }
    return mrpLineMap;
  }
}
