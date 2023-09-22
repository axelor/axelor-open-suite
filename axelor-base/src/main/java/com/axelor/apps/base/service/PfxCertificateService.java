package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;

public interface PfxCertificateService {

  void setCertificateNameFromFile(PfxCertificate pfxCertificate);

  String getCertificateName(PfxCertificate pfxCertificate);

  void setValidityDates(PfxCertificate pfxCertificate) throws AxelorException;
}
