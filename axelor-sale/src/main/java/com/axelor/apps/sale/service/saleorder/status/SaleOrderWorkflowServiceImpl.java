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
package com.axelor.apps.sale.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

public class SaleOrderWorkflowServiceImpl implements SaleOrderWorkflowService {

  protected PartnerRepository partnerRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected AppSaleService appSaleService;
  protected AppCrmService appCrmService;
  protected UserService userService;
  protected SaleOrderCheckService saleOrderCheckService;

  @Inject
  public SaleOrderWorkflowServiceImpl(
      PartnerRepository partnerRepo,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      AppCrmService appCrmService,
      UserService userService,
      SaleOrderCheckService saleOrderCheckService) {
    this.partnerRepo = partnerRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.appSaleService = appSaleService;
    this.appCrmService = appCrmService;
    this.userService = userService;
    this.saleOrderCheckService = saleOrderCheckService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelSaleOrder(
      SaleOrder saleOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException {

    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    authorizedStatus.add(SaleOrderRepository.STATUS_FINALIZED_QUOTATION);
    if (saleOrder.getStatusSelect() == null
        || !authorizedStatus.contains(saleOrder.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_CANCEL_WRONG_STATUS));
    }

    Query q =
        JPA.em()
            .createQuery(
                "select count(*) FROM SaleOrder as self WHERE self.statusSelect in (?1 , ?2) AND self.clientPartner = ?3 ");
    q.setParameter(1, SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    q.setParameter(2, SaleOrderRepository.STATUS_ORDER_COMPLETED);
    q.setParameter(3, saleOrder.getClientPartner());
    if ((long) q.getSingleResult() == 0) {
      saleOrder.getClientPartner().setIsCustomer(false);
      saleOrder.getClientPartner().setIsProspect(true);
    }
    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_CANCELED);
    saleOrder.setCancelReason(cancelReason);
    if (Strings.isNullOrEmpty(cancelReasonStr)) {
      saleOrder.setCancelReasonStr(cancelReason.getName());
    } else {
      saleOrder.setCancelReasonStr(cancelReasonStr);
    }
    saleOrderRepo.save(saleOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void completeSaleOrder(SaleOrder saleOrder) throws AxelorException {

    if (saleOrder.getStatusSelect() == null
        || saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_COMPLETE_WRONG_STATUS));
    }

    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_ORDER_COMPLETED);
    saleOrder.setOrderBeingEdited(false);

    saleOrderRepo.save(saleOrder);
  }
}
