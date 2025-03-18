package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;

public interface PfxCertificateCheckService {

  void checkValidity(PfxCertificate pfxCertificate) throws AxelorException;
}
