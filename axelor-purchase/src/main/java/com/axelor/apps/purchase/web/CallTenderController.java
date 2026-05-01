/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderOfferImportHistory;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.CallTenderRepository;
import com.axelor.apps.purchase.service.CallTenderExcelService;
import com.axelor.apps.purchase.service.CallTenderGenerateService;
import com.axelor.apps.purchase.service.CallTenderMailService;
import com.axelor.apps.purchase.service.CallTenderOfferImportService;
import com.axelor.apps.purchase.service.CallTenderPurchaseOrderService;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CallTenderController {

  public void generateCallTenderOffers(ActionRequest request, ActionResponse response) {

    var callTender = request.getContext().asType(CallTender.class);
    callTender = Beans.get(CallTenderRepository.class).find(callTender.getId());
    if (callTender != null) {
      Beans.get(CallTenderGenerateService.class).generateCallTenderOffers(callTender);
      response.setReload(true);
    }
  }

  public void generateCallTenderEmails(ActionRequest request, ActionResponse response) {
    try {
      var callTender = request.getContext().asType(CallTender.class);
      callTender = Beans.get(CallTenderRepository.class).find(callTender.getId());
      if (callTender != null) {
        Beans.get(CallTenderMailService.class).generateCallTenderEmails(callTender);
        response.setReload(true);
        response.setView(
            ActionView.define(I18n.get("Generated emails"))
                .model(Message.class.getName())
                .add("grid", "message-grid")
                .add("form", "message-form")
                .domain(
                    "self.id IN (SELECT ctm.emailMessage.id FROM CallTenderMail ctm"
                        + " WHERE ctm.id IN (SELECT cfo.offerMail.id FROM CallTenderOffer cfo"
                        + " WHERE cfo.callTender.id = :_callTenderId)"
                        + " AND ctm.emailMessage IS NOT NULL)")
                .context("_callTenderId", callTender.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void sendCallTenderOffers(ActionRequest request, ActionResponse response) {
    try {
      var callTender = request.getContext().asType(CallTender.class);
      callTender = Beans.get(CallTenderRepository.class).find(callTender.getId());
      if (callTender != null) {
        Beans.get(CallTenderMailService.class).sendCallTenderOffers(callTender);
        response.setInfo(I18n.get("Emails sent successfully."));
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void importOffer(ActionRequest request, ActionResponse response) {
    try {
      Long callTenderId = Long.valueOf(request.getContext().get("_callTenderId").toString());
      CallTender callTender = Beans.get(CallTenderRepository.class).find(callTenderId);

      Partner supplier = getSupplierFromContext(request);
      CallTenderOfferImportHistory wizard =
          request.getContext().asType(CallTenderOfferImportHistory.class);
      MetaFile metaFile = Beans.get(MetaFileRepository.class).find(wizard.getMetaFile().getId());

      CallTenderOfferImportHistory history =
          Beans.get(CallTenderOfferImportService.class)
              .importOffers(callTender, supplier, metaFile);

      response.setInfo(history.getLog());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  protected Partner getSupplierFromContext(ActionRequest request) {
    Map<String, Object> supplierMap =
        (Map<String, Object>) request.getContext().get("supplierPartner");
    return Beans.get(PartnerRepository.class).find(((Integer) supplierMap.get("id")).longValue());
  }

  public void generateImportTemplate(ActionRequest request, ActionResponse response) {
    try {
      Long callTenderId = Long.valueOf(request.getContext().get("_callTenderId").toString());
      CallTender callTender = Beans.get(CallTenderRepository.class).find(callTenderId);

      Partner supplier = null;
      Object supplierRef = request.getContext().get("supplierPartner");
      if (supplierRef instanceof Map) {
        Object id = ((Map<?, ?>) supplierRef).get("id");
        if (id != null) {
          supplier = Beans.get(PartnerRepository.class).find(Long.valueOf(id.toString()));
        }
      }

      MetaFile templateFile =
          Beans.get(CallTenderExcelService.class).generateTemplate(callTender, supplier);

      response.setExportFile(MetaFiles.getPath(templateFile).toString());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generatePurchaseOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var callTender = request.getContext().asType(CallTender.class);
    List<CallTenderOffer> selectedCallTenderOfferList =
        callTender.getCallTenderOfferList().stream()
            .filter(Model::isSelected)
            .collect(Collectors.toList());
    callTender = Beans.get(CallTenderRepository.class).find(callTender.getId());
    List<Long> purchaseOrderIds =
        Beans.get(CallTenderPurchaseOrderService.class)
            .generatePurchaseOrders(callTender, selectedCallTenderOfferList)
            .stream()
            .map(PurchaseOrder::getId)
            .collect(Collectors.toList());

    if (purchaseOrderIds.size() == 1) {
      response.setView(
          ActionView.define(I18n.get("Purchase order"))
              .model(PurchaseOrder.class.getName())
              .add("form", "purchase-order-form")
              .add("grid", "purchase-order-grid")
              .param("search-filters", "purchase-order-filters")
              .param("forceEdit", "true")
              .context("_showRecord", purchaseOrderIds.stream().findFirst().orElse(0L))
              .map());
    } else if (purchaseOrderIds.size() > 1) {
      response.setView(
          ActionView.define(I18n.get("Purchase order"))
              .model(PurchaseOrder.class.getName())
              .add("grid", "purchase-order-grid")
              .add("form", "purchase-order-form")
              .param("search-filters", "purchase-order-filters")
              .domain("self.id in (" + Joiner.on(",").join(purchaseOrderIds) + ")")
              .map());
    }
  }
}
