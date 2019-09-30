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
package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class PurchaseRequestServiceImpl implements PurchaseRequestService {

  @Inject private PurchaseRequestRepository purchaseRequestRepo;

  @Transactional
  @Override
  public void confirmCart() {

    List<PurchaseRequest> purchaseRequests =
        purchaseRequestRepo
            .all()
            .filter("self.statusSelect = 1 and self.createdBy = ?1", AuthUtils.getUser())
            .fetch();

    for (PurchaseRequest purchaseRequest : purchaseRequests) {
      purchaseRequest.setStatusSelect(2);
      purchaseRequestRepo.save(purchaseRequest);
    }
  }

  @Transactional
  @Override
  public void acceptRequest(List<PurchaseRequest> purchaseRequests) {

    for (PurchaseRequest purchaseRequest : purchaseRequests) {
      purchaseRequest.setStatusSelect(3);
      purchaseRequestRepo.save(purchaseRequest);
    }
  }
}
