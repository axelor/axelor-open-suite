package com.axelor.apps.account.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.db.repo.DebtRecoveryHistoryRepository;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DebtRecoveryHistoryController {

  public void printPaymentReminder(ActionRequest request, ActionResponse response) {
    try {
      DebtRecoveryHistory debtRecoveryHistory =
          request.getContext().asType(DebtRecoveryHistory.class);

      debtRecoveryHistory =
          Beans.get(DebtRecoveryHistoryRepository.class).find(debtRecoveryHistory.getId());

      String name = I18n.get("Payment reminder") + " " + debtRecoveryHistory.getName();

      String fileLink =
          ReportFactory.createReport("PaymentReminder.rptdesign", name + "-${date}")
              .addParam("DebtRecoveryHistoryID", debtRecoveryHistory.getId())
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addFormat("pdf")
              .toAttach(debtRecoveryHistory)
              .generate()
              .getFileLink();

      response.setView(ActionView.define(name).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
