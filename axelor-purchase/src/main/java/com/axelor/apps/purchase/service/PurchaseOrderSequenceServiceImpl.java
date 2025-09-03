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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class PurchaseOrderSequenceServiceImpl implements PurchaseOrderSequenceService {

  protected SequenceService sequenceService;

  @Inject
  public PurchaseOrderSequenceServiceImpl(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public void setDraftSequence(PurchaseOrder purchaseOrder) throws AxelorException {
    if (purchaseOrder.getId() != null
        && Strings.isNullOrEmpty(purchaseOrder.getPurchaseOrderSeq())) {
      purchaseOrder.setPurchaseOrderSeq(sequenceService.getDraftSequenceNumber(purchaseOrder));
    }
  }

  @Override
  public String getSequence(Company company, PurchaseOrder purchaseOrder) throws AxelorException {
    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.PURCHASE_ORDER,
            company,
            PurchaseOrder.class,
            "purchaseOrderSeq",
            purchaseOrder);
    if (seq == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_1),
          company.getName());
    }
    return seq;
  }
}
