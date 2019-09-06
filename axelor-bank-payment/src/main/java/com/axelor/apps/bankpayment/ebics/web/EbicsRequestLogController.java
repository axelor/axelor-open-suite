package com.axelor.apps.bankpayment.ebics.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.bankpayment.db.EbicsRequestLog;
import com.axelor.apps.bankpayment.db.repo.EbicsRequestLogRepository;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EbicsRequestLogController {

  public void print(ActionRequest request, ActionResponse response) {
    try {
      EbicsRequestLog ebicsRequestLog = request.getContext().asType(EbicsRequestLog.class);
      ebicsRequestLog = Beans.get(EbicsRequestLogRepository.class).find(ebicsRequestLog.getId());

      String name = "Ebics report log " + ebicsRequestLog.getRequestType();

      String fileLink =
          ReportFactory.createReport("EbicsReportLog.rptdesign", name + "-${date}")
              .addParam("EbicsReportLogId", ebicsRequestLog.getId())
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addFormat("pdf")
              .toAttach(ebicsRequestLog)
              .generate()
              .getFileLink();

      response.setView(ActionView.define(name).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
