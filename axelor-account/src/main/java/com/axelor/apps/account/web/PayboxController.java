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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.payment.PayboxService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherPayboxService;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PayboxController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void paybox(ActionRequest request, ActionResponse response) {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    try {
      Partner partner = paymentVoucher.getPartner();

      if ((partner.getEmailAddress().getAddress() != null
              && !partner.getEmailAddress().getAddress().isEmpty())
          || (paymentVoucher.getEmail() != null && !paymentVoucher.getEmail().isEmpty())
          || paymentVoucher.getDefaultEmailOk()) {

        String url = Beans.get(PayboxService.class).paybox(paymentVoucher);

        Map<String, Object> mapView = new HashMap<String, Object>();
        mapView.put("title", I18n.get(IExceptionMessage.PAYBOX_5));
        mapView.put("resource", url);
        mapView.put("viewType", "html");
        response.setView(mapView);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void addPayboxEmail(ActionRequest request, ActionResponse response) {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    try {
      Beans.get(PayboxService.class)
          .addPayboxEmail(
              paymentVoucher.getPartner(),
              paymentVoucher.getEmail(),
              paymentVoucher.getToSaveEmailOk());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Lancer le batch à travers un web service.
   *
   * @param request
   * @param response
   * @throws Exception
   */
  public void webServicePaybox(ActionRequest request, ActionResponse response) throws Exception {

    Context context = request.getContext();

    PaymentVoucherPayboxService paymentVoucherPayboxService =
        Beans.get(PaymentVoucherPayboxService.class);
    PaymentVoucherRepository paymentVoucherRepo = Beans.get(PaymentVoucherRepository.class);

    String idPaymentVoucher = (String) context.get("idPV");
    String operation = (String) context.get("retour");
    String signature = (String) context.get("sign");

    if (idPaymentVoucher != null && operation != null && signature != null) {

      LOG.debug("idPaymentVoucher :" + idPaymentVoucher);

      PaymentVoucher paymentVoucher = paymentVoucherRepo.find(Long.parseLong(idPaymentVoucher));
      LOG.debug("paymentVoucher :" + paymentVoucher);

      boolean verified = false;

      if (paymentVoucher != null
          && paymentVoucher.getCompany() != null
          && !paymentVoucher.getPayboxPaidOk()) {

        List<String> varList = new ArrayList<String>();

        String retourVars =
            paymentVoucher.getCompany().getAccountConfig().getPayboxConfig().getPayboxRetour();
        String[] retours = retourVars.split(";");

        varList.add("idPV=" + idPaymentVoucher);
        LOG.debug("idPV=" + idPaymentVoucher);
        varList.add("retour=" + operation);
        LOG.debug("retour=" + operation);
        for (int i = 0; i < retours.length - 1; i++) {
          String variableName = retours[i].split(":")[0];
          String varValue = (String) context.get(variableName);
          String varBuilt = variableName + "=" + varValue;
          LOG.debug(varBuilt);
          if (varValue != null) {
            varList.add(varBuilt);
          }
        }
        verified =
            Beans.get(PayboxService.class)
                .checkPaybox(signature, varList, paymentVoucher.getCompany());
        LOG.debug("L'adresse URL est-elle correcte ? : {}", verified);
      }
      if (verified) {
        if (operation.equals("1")
            && (String) context.get("idtrans") != null
            && (String) context.get("montant") != null) {
          paymentVoucherPayboxService.authorizeConfirmPaymentVoucher(
              paymentVoucher, (String) context.get("idtrans"), (String) context.get("montant"));
          response.setFlash(I18n.get(IExceptionMessage.PAYBOX_6));
          LOG.debug("Paiement réalisé");
        } else if (operation.equals("2")) {
          response.setFlash(I18n.get(IExceptionMessage.PAYBOX_7));
          LOG.debug("Paiement échoué");
        } else if (operation.equals("3")) {
          response.setFlash(I18n.get(IExceptionMessage.PAYBOX_8));
          LOG.debug("Paiement annulé");
        }
      } else {
        response.setFlash(I18n.get(IExceptionMessage.PAYBOX_9));
        LOG.debug("Retour d'information de Paybox erroné");
      }
    }
  }
}
