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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class SaleOrderSequenceServiceImpl implements SaleOrderSequenceService {

  protected final SequenceService sequenceService;
  protected final SaleOrderRepository saleOrderRepository;
  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderSequenceServiceImpl(
      SequenceService sequenceService,
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService) {
    this.sequenceService = sequenceService;
    this.saleOrderRepository = saleOrderRepository;
    this.appSaleService = appSaleService;
  }

  @Override
  public String getQuotationSequence(SaleOrder saleOrder) throws AxelorException {
    Company company = saleOrder.getCompany();
    String seq;
    if (!appSaleService.getAppSale().getIsQuotationAndOrderSplitEnabled()) {
      seq = getSequence(saleOrder);
    } else {
      seq = getSplitQuotationSequence(saleOrder);
    }

    if (seq == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SaleExceptionMessage.SALES_ORDER_1),
          company.getName());
    }
    return seq;
  }

  protected String getSplitQuotationSequence(SaleOrder saleOrder) throws AxelorException {
    String seq;
    SaleOrder originSaleQuotation = saleOrder.getOriginSaleQuotation();
    if (originSaleQuotation != null) {
      long childrenCounter =
          saleOrderRepository
              .all()
              .filter("self.originSaleQuotation = :originSaleQuotation AND self.id != :id")
              .bind("originSaleQuotation", originSaleQuotation)
              .bind("id", saleOrder.getId())
              .count();
      seq = originSaleQuotation.getSaleOrderSeq() + "_" + (childrenCounter + 1);
    } else {
      seq = getSequence(saleOrder);
    }
    return seq;
  }

  protected String getSequence(SaleOrder saleOrder) throws AxelorException {
    return sequenceService.getSequenceNumber(
        SequenceRepository.SALES_ORDER,
        saleOrder.getCompany(),
        SaleOrder.class,
        "saleOrderSeq",
        saleOrder);
  }
}
