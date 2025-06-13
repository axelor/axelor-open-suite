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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCancelService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCheckService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderEncryptionService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BankOrderController {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void confirm(ActionRequest request, ActionResponse response) {

    try {
      BankOrder bankOrder = request.getContext().asType(BankOrder.class);
      bankOrder = Beans.get(BankOrderRepository.class).find(bankOrder.getId());
      if (bankOrder != null) {
        Beans.get(BankOrderValidationService.class).confirm(bankOrder);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }

  public void realize(ActionRequest request, ActionResponse response) {

    try {
      BankOrder bankOrder = request.getContext().asType(BankOrder.class);
      bankOrder = Beans.get(BankOrderRepository.class).find(bankOrder.getId());
      if (bankOrder != null) {
        Beans.get(BankOrderValidationService.class).realize(bankOrder);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }

  @SuppressWarnings("unchecked")
  public void merge(ActionRequest request, ActionResponse response) {

    try {

      List<Integer> listSelectedBankOrder = (List<Integer>) request.getContext().get("_ids");
      BankOrderRepository bankOrderRepository = Beans.get(BankOrderRepository.class);

      List<BankOrder> bankOrderList = Lists.newArrayList();
      if (listSelectedBankOrder != null) {
        for (Integer bankOrderId : listSelectedBankOrder) {

          BankOrder bankOrder = bankOrderRepository.find(bankOrderId.longValue());

          if (bankOrder != null) {
            bankOrderList.add(bankOrder);
          }
        }

        BankOrder bankOrder = Beans.get(BankOrderMergeService.class).mergeBankOrders(bankOrderList);

        response.setView(
            ActionView.define(I18n.get("Bank Order"))
                .model(BankOrder.class.getName())
                .add("form", "bank-order-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(bankOrder.getId()))
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setBankDetailDomain(ActionRequest request, ActionResponse response) {
    BankOrder bankOrder = request.getContext().asType(BankOrder.class);
    response.setAttr(
        "senderBankDetails",
        "domain",
        Beans.get(BankOrderService.class).createDomainForBankDetails(bankOrder));
  }

  public void fillBankDetails(ActionRequest request, ActionResponse response) {
    BankOrder bankOrder = request.getContext().asType(BankOrder.class);
    BankDetails bankDetails =
        Beans.get(BankOrderCheckService.class).getDefaultBankDetails(bankOrder);
    response.setValue("senderBankDetails", bankDetails);
  }

  public void resetReceivers(ActionRequest request, ActionResponse response) {
    try {
      BankOrder bankOrder = request.getContext().asType(BankOrder.class);
      Beans.get(BankOrderService.class).resetReceivers(bankOrder);
      response.setValue("bankOrderLineList", bankOrder.getBankOrderLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void displayBankOrderLines(ActionRequest actionRequest, ActionResponse response) {
    try {
      String linesDomain = (String) actionRequest.getContext().get("_linesDomain");
      response.setView(
          Beans.get(BankOrderService.class)
              .buildBankOrderLineView("bank-order-line-grid", "bank-order-line-form", linesDomain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void displayBankOrderLinesBankToBank(
      ActionRequest actionRequest, ActionResponse response) {
    try {
      String linesDomain = (String) actionRequest.getContext().get("_linesDomain");
      response.setView(
          Beans.get(BankOrderService.class)
              .buildBankOrderLineView(
                  "bank-order-line-bank-to-bank-grid", "bank-order-line-form", linesDomain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      BankOrder bankOrder = request.getContext().asType(BankOrder.class);
      bankOrder = Beans.get(BankOrderRepository.class).find(bankOrder.getId());
      Beans.get(BankOrderCancelService.class).cancelBankOrder(bankOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setStatusCorrect(ActionRequest request, ActionResponse response) {
    BankOrder bankOrder = request.getContext().asType(BankOrder.class);
    bankOrder = Beans.get(BankOrderRepository.class).find(bankOrder.getId());
    Beans.get(BankOrderService.class).setStatusToDraft(bankOrder);
    response.setReload(true);
  }

  public void decryptAndDownload(ActionRequest request, ActionResponse response)
      throws AxelorException {
    BankOrder bankOrder =
        Beans.get(BankOrderRepository.class)
            .find(Long.parseLong(request.getContext().get("_bankOrder").toString()));

    String password =
        Optional.ofNullable(request.getContext().get("password")).map(Object::toString).orElse("");
    Beans.get(BankOrderEncryptionService.class).checkInputPassword(password);

    String encryptedPassword = Beans.get(AuthService.class).encrypt(password);
    String base64HashedPassword =
        Base64.getUrlEncoder().encodeToString(encryptedPassword.getBytes(StandardCharsets.UTF_8));

    response.setView(
        ActionView.define(I18n.get("Export file"))
            .add(
                "html",
                "ws/aos/bankorder/file-download/"
                    + base64HashedPassword
                    + "/"
                    + bankOrder.getGeneratedMetaFile().getId())
            .param("download", "true")
            .map());
    response.setCanClose(true);
  }

  public void setIsFileEncrypted(ActionRequest request, ActionResponse response)
      throws AxelorException {
    BankOrder bankOrder = request.getContext().asType(BankOrder.class);
    MetaFile generatedMetaFile = bankOrder.getGeneratedMetaFile();
    if (generatedMetaFile == null) {
      return;
    }
    response.setValue(
        "$isMetafileEncrypted",
        Beans.get(BankOrderEncryptionService.class).isFileEncrypted(generatedMetaFile));
  }

  public void encryptUploadedFile(ActionRequest request, ActionResponse response)
      throws AxelorException {
    if (!Beans.get(AppBankPaymentService.class)
        .getAppBankPayment()
        .getEnableBankOrderFileEncryption()) {
      return;
    }
    Context context = request.getContext();
    boolean isMetafileEncrypted =
        Optional.ofNullable(context.get("isMetafileEncrypted"))
            .map(Object::toString)
            .map(Boolean::valueOf)
            .orElse(false);

    BankOrder bankOrder = context.asType(BankOrder.class);
    MetaFile originalFile = bankOrder.getGeneratedMetaFile();

    if (originalFile == null || isMetafileEncrypted) {
      return;
    }
    Beans.get(BankOrderEncryptionService.class).encryptUploadedBankOrderFile(originalFile);
  }

  public void checkBankOrderLineBankDetails(ActionRequest request, ActionResponse response) {
    BankOrder bankOrder = request.getContext().asType(BankOrder.class);
    List<BankOrderLine> bankOrderLines =
        Beans.get(BankOrderCheckService.class).checkBankOrderLineBankDetails(bankOrder);
    response.setValue("bankOrderLineList", bankOrderLines);
  }
}
