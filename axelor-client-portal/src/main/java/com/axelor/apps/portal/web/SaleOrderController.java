/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.web;

import com.axelor.apps.client.portal.db.PortalQuotation;
import com.axelor.apps.client.portal.db.repo.PortalQuotationRepository;
import com.axelor.apps.portal.service.PortalQuotationService;
import com.axelor.apps.portal.translation.ITranslation;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import javax.mail.MessagingException;

public class SaleOrderController {

  @Transactional
  public void sendPortalQuatation(ActionRequest request, ActionResponse response)
      throws MessagingException, AxelorException {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    PortalQuotation portalQuotation =
        Beans.get(PortalQuotationRepository.class)
            .all()
            .filter("self.saleOrder = :saleOrder AND self.statusSelect = :status")
            .bind("saleOrder", saleOrder)
            .bind("status", SaleOrderRepository.STATUS_DRAFT_QUOTATION)
            .fetchOne();
    if (portalQuotation != null) {
      portalQuotation.setStatusSelect(PortalQuotationRepository.STATUS_CANCELED);
      Beans.get(PortalQuotationRepository.class).save(portalQuotation);
    }

    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
    Beans.get(PortalQuotationService.class).createPortalQuotation(saleOrder);
    response.setNotify(I18n.get(ITranslation.PORTAL_QUATATION_SENT));
  }

  public void checkPortalQuatation(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    PortalQuotation portalQuotation =
        Beans.get(PortalQuotationRepository.class)
            .all()
            .filter("self.saleOrder = :saleOrder AND self.statusSelect = :status")
            .bind("saleOrder", saleOrder)
            .bind("status", SaleOrderRepository.STATUS_DRAFT_QUOTATION)
            .fetchOne();
    if (portalQuotation != null) {
      response.setAlert(I18n.get(ITranslation.PORTAL_QUATATION_EXIST));
    }
  }
}
