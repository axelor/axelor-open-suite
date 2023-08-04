/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class QIIdentificationServiceImpl implements QIIdentificationService {
  @Inject AppBaseService appBaseService;

  @Override
  public void updateQIIdentification(QIIdentification qiIdentification) {
    Company company = qiIdentification.getQi().getCompany();
    if (qiIdentification.getCustomerPartner() != null
        || qiIdentification.getSupplierPartner() != null
        || qiIdentification.getContact() != null
        || qiIdentification.getDetectedByInternal() != null
        || qiIdentification.getCustomerSaleOrder() != null
        || qiIdentification.getCustomerSaleOrderLine() != null
        || qiIdentification.getSupplierPurchaseOrder() != null
        || qiIdentification.getSupplierPurchaseOrderLine() != null
        || qiIdentification.getStockMove() != null
        || qiIdentification.getStockMoveLine() != null
        || qiIdentification.getManufOrder() != null
        || qiIdentification.getOperationOrder() != null
        || qiIdentification.getToConsumeProdProduct() != null
        || qiIdentification.getConsumedProdProduct() != null
        || qiIdentification.getProduct() != null
        || qiIdentification.getQualityControl() != null
        || qiIdentification.getQuantity().compareTo(BigDecimal.ZERO) != 0
        || qiIdentification.getNonConformingQuantity().compareTo(BigDecimal.ZERO) != 0
        || StringUtils.notBlank(qiIdentification.getDocumentReference())) {
      qiIdentification.setWrittenBy(AuthUtils.getUser());
      qiIdentification.setWrittenOn(appBaseService.getTodayDateTime(company).toLocalDateTime());
    }
  }
}
