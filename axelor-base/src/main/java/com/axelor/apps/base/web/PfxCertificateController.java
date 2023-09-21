package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.service.PfxCertificateService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PfxCertificateController {
  public void getFileName(ActionRequest request, ActionResponse response) {
    PfxCertificate pfxCertificate = request.getContext().asType(PfxCertificate.class);
    Beans.get(PfxCertificateService.class).setCertificateNameFromFile(pfxCertificate);
    response.setValue("name", pfxCertificate.getName());
  }

  public void getValidityDates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PfxCertificate pfxCertificate = request.getContext().asType(PfxCertificate.class);
    Beans.get(PfxCertificateService.class).setValidityDates(pfxCertificate);
    response.setValue("fromValidityDate", pfxCertificate.getFromValidityDate());
    response.setValue("toValidityDate", pfxCertificate.getToValidityDate());
  }
}
