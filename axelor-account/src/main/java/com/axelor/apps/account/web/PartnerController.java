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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.NotificationRepository;
import com.axelor.apps.account.service.AccountingSituationInitService;
import com.axelor.apps.account.service.PartnerAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class PartnerController {

  public void createAccountingSituations(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Partner partner = request.getContext().asType(Partner.class);

    List<AccountingSituation> accountingSituationList =
        Beans.get(AccountingSituationInitService.class)
            .createAccountingSituation(Beans.get(PartnerRepository.class).find(partner.getId()));

    if (accountingSituationList != null) {
      response.setValue("accountingSituationList", accountingSituationList);
    }
  }

  public void getDefaultSpecificTaxNote(ActionRequest request, ActionResponse response) {

    Partner partner = request.getContext().asType(Partner.class);
    response.setValue(
        "specificTaxNote",
        Beans.get(PartnerAccountService.class).getDefaultSpecificTaxNote(partner));
  }

  public void checkAnyCompanyAccountConfigAttached(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      if (!partner.getIsFactor()) {
        long accountConfigCount =
            Beans.get(AccountConfigRepository.class)
                .all()
                .filter("self.factorPartner = :factorPartner")
                .bind("factorPartner", partner.getId())
                .count();
        if (accountConfigCount > 0) {
          response.setValue("factorCantBeRemoved", true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnyNotificationAttached(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      if (!partner.getIsFactor()) {
        long notificationCount =
            Beans.get(NotificationRepository.class)
                .all()
                .filter("self.factorPartner = :factorPartner")
                .bind("factorPartner", partner.getId())
                .count();
        if (notificationCount > 0) {
          response.setValue("factorCantBeRemoved", true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnyPayableMoveLineAttached(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      if (!partner.getIsSupplier()) {
        long moveLineCount =
            Beans.get(MoveLineRepository.class)
                .all()
                .filter(
                    "self.partner = :partner AND self.account.accountType.technicalTypeSelect = :technicalTypeSelect")
                .bind("partner", partner.getId())
                .bind("technicalTypeSelect", AccountTypeRepository.TYPE_PAYABLE)
                .count();
        if (moveLineCount > 0) {
          response.setValue("supplierCantBeRemoved", true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnyInvoiceSupplierPurchaseAttached(
      ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      if (!partner.getIsSupplier()) {
        long invoiceCount =
            Beans.get(InvoiceRepository.class)
                .all()
                .filter(
                    "self.operationTypeSelect = :operationTypeSelect AND self.partner = :partner")
                .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)
                .bind("partner", partner.getId())
                .count();
        if (invoiceCount > 0) {
          response.setValue("supplierCantBeRemoved", true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnyReceivableMoveLineAttached(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      if (!partner.getIsCustomer()) {
        long moveLineCount =
            Beans.get(MoveLineRepository.class)
                .all()
                .filter(
                    "self.partner = :partner AND self.account.accountType.technicalTypeSelect = :technicalTypeSelect")
                .bind("partner", partner.getId())
                .bind("technicalTypeSelect", AccountTypeRepository.TYPE_RECEIVABLE)
                .count();
        if (moveLineCount > 0) {
          response.setValue("customerCantBeRemoved", true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnyInvoiceClientSaleAttached(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      if (!partner.getIsCustomer()) {
        long invoiceCount =
            Beans.get(InvoiceRepository.class)
                .all()
                .filter(
                    "self.operationTypeSelect = :operationTypeSelect AND self.partner = :partner")
                .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
                .bind("partner", partner.getId())
                .count();
        if (invoiceCount > 0) {
          response.setValue("customerCantBeRemoved", true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
