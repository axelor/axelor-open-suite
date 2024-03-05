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
package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSale;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.PersistenceException;

public class SaleOrderManagementRepository extends SaleOrderRepository {

  @Override
  public SaleOrder copy(SaleOrder entity, boolean deep) {

    SaleOrder copy = super.copy(entity, deep);

    copy.setStatusSelect(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    copy.setSaleOrderSeq(null);
    copy.clearBatchSet();
    copy.setImportId(null);
    copy.setCreationDate(Beans.get(AppBaseService.class).getTodayDate(entity.getCompany()));
    copy.setConfirmationDateTime(null);
    copy.setConfirmedByUser(null);
    copy.setOrderDate(null);
    copy.setOrderNumber(null);
    copy.setVersionNumber(1);
    copy.setTotalCostPrice(null);
    copy.setTotalGrossMargin(null);
    copy.setMarginRate(null);
    copy.setEstimatedShippingDate(null);
    copy.setOrderBeingEdited(false);
    if (copy.getAdvancePaymentAmountNeeded().compareTo(copy.getAdvanceTotal()) <= 0) {
      copy.setAdvancePaymentAmountNeeded(BigDecimal.ZERO);
      copy.setAdvancePaymentNeeded(false);
      copy.clearAdvancePaymentList();
    }

    if (copy.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
        saleOrderLine.setDesiredDeliveryDate(null);
        saleOrderLine.setEstimatedShippingDate(null);
        saleOrderLine.setDiscountDerogation(null);
      }
    }
    Beans.get(SaleOrderService.class).computeEndOfValidityDate(copy);

    return copy;
  }

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    try {
      AppSale appSale = Beans.get(AppSaleService.class).getAppSale();
      SaleOrderComputeService saleOrderComputeService = Beans.get(SaleOrderComputeService.class);

      if (appSale.getEnablePackManagement()) {
        saleOrderComputeService.computePackTotal(saleOrder);
      } else {
        saleOrderComputeService.resetPackTotal(saleOrder);
      }
      computeSeq(saleOrder);
      computeFullName(saleOrder);

      if (appSale.getManagePartnerComplementaryProduct()) {
        Beans.get(SaleOrderService.class).manageComplementaryProductSOLines(saleOrder);
      }

      computeSubMargin(saleOrder);
      Beans.get(SaleOrderMarginService.class).computeMarginSaleOrder(saleOrder);
      return super.save(saleOrder);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
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
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  public void computeFullName(SaleOrder saleOrder) {
    try {
      if (saleOrder.getClientPartner() != null) {
        String fullName = saleOrder.getClientPartner().getName();
        if (!Strings.isNullOrEmpty(saleOrder.getSaleOrderSeq())) {
          fullName = saleOrder.getSaleOrderSeq() + "-" + fullName;
        }
        saleOrder.setFullName(fullName);
      }
    } catch (Exception e) {
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  protected void computeSubMargin(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    SaleOrderMarginService saleOrderMarginService = Beans.get(SaleOrderMarginService.class);
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        saleOrderMarginService.computeSubMargin(saleOrder, saleOrderLine);
      }
    }
  }
}
