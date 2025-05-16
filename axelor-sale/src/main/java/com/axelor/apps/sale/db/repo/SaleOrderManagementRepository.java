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
package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCopyService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOrderingStatusService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSale;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import javax.persistence.PersistenceException;

public class SaleOrderManagementRepository extends SaleOrderRepository {

  protected final SaleOrderCopyService saleOrderCopyService;
  protected final SaleOrderOrderingStatusService saleOrderOrderingStatusService;

  @Inject
  public SaleOrderManagementRepository(
      SaleOrderCopyService saleOrderCopyService,
      SaleOrderOrderingStatusService saleOrderOrderingStatusService) {
    this.saleOrderCopyService = saleOrderCopyService;
    this.saleOrderOrderingStatusService = saleOrderOrderingStatusService;
  }

  @Override
  public SaleOrder copy(SaleOrder entity, boolean deep) {

    SaleOrder copy = super.copy(entity, deep);
    saleOrderCopyService.copySaleOrder(copy);
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
      if (appSale.getIsQuotationAndOrderSplitEnabled()) {
        saleOrderOrderingStatusService.updateOrderingStatus(saleOrder);
      }
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
    MarginComputeService marginComputeService = Beans.get(MarginComputeService.class);
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        marginComputeService.computeSubMargin(
            saleOrder, saleOrderLine, saleOrderLine.getExTaxTotal());
      }
    }
  }

  @Override
  public void remove(SaleOrder saleOrder) {
    try {
      if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SaleExceptionMessage.SALE_ORDER_CANNOT_DELETE_COMFIRMED_ORDER));
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
    super.remove(saleOrder);
  }
}
