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
package com.axelor.apps.purchase.web;

import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.service.PurchaseRequestService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class PurchaseRequestController {

  @Inject private PurchaseRequestRepository purchaseRequestRepo;

  @Inject private PurchaseRequestService purchaseRequestService;

  public void confirmCart(ActionRequest request, ActionResponse response) {
    purchaseRequestService.confirmCart();
    response.setReload(true);
  }

  public void acceptRequest(ActionRequest request, ActionResponse response) {

    if (request.getContext().get("_ids") == null) {
      return;
    }

    List<Long> requestIds = (List<Long>) request.getContext().get("_ids");

    if (!requestIds.isEmpty()) {
      List<PurchaseRequest> purchaseRequests =
          purchaseRequestRepo.all().filter("self.id in (?1)", requestIds).fetch();

      purchaseRequestService.acceptRequest(purchaseRequests);

      response.setReload(true);
    }
  }
}
