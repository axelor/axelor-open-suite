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
package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class SaleOrderManagementRepository extends SaleOrderRepository {

  @Override
  public SaleOrder copy(SaleOrder entity, boolean deep) {

    SaleOrder copy = super.copy(entity, deep);

    copy.setStatusSelect(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    copy.setSaleOrderSeq(null);
    copy.clearBatchSet();
    copy.setImportId(null);
    copy.setCreationDate(Beans.get(AppBaseService.class).getTodayDate());
    copy.setConfirmationDateTime(null);
    copy.setConfirmedByUser(null);
    copy.setOrderDate(null);
    copy.setOrderNumber(null);
    copy.setVersionNumber(1);
    copy.setTotalCostPrice(null);
    copy.setTotalGrossMargin(null);
    copy.setMarginRate(null);
    copy.setEndOfValidityDate(null);
    copy.setDeliveryDate(null);
    copy.setOrderBeingEdited(false);

    for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
      saleOrderLine.setDesiredDelivDate(null);
      saleOrderLine.setEstimatedDelivDate(null);
    }

    return copy;
  }

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    try {
      computeSeq(saleOrder);
      computeFullName(saleOrder);
      computeSubMargin(saleOrder);
      Beans.get(SaleOrderMarginService.class).computeMarginSaleOrder(saleOrder);
      return super.save(saleOrder);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  public void computeSeq(SaleOrder saleOrder) {
    try {
      if (saleOrder.getId() == null) {
        saleOrder = super.save(saleOrder);
      }
      if (Strings.isNullOrEmpty(saleOrder.getSaleOrderSeq()) && !saleOrder.getTemplate()) {
        if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION) {
          saleOrder.setSaleOrderSeq(
              Beans.get(SequenceService.class).getDraftSequenceNumber(saleOrder));
        }
      }

    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  public void computeFullName(SaleOrder saleOrder) {
    try {
      if (!Strings.isNullOrEmpty(saleOrder.getSaleOrderSeq()))
        saleOrder.setFullName(
            saleOrder.getSaleOrderSeq() + "-" + saleOrder.getClientPartner().getName());
      else saleOrder.setFullName(saleOrder.getClientPartner().getName());
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  public void computeSubMargin(SaleOrder saleOrder) throws AxelorException {

    if (saleOrder.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        Beans.get(SaleOrderLineService.class).computeSubMargin(saleOrder, saleOrderLine);
      }
    }
  }
}
