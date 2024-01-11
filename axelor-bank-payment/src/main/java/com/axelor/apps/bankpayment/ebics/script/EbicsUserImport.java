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
package com.axelor.apps.bankpayment.ebics.script;

import com.axelor.apps.bankpayment.db.EbicsBank;
import com.axelor.apps.bankpayment.db.EbicsCertificate;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.service.EbicsCertificateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.service.BankService;
import com.google.inject.Inject;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class EbicsUserImport {

  @Inject private EbicsCertificateService certificateService;

  @Inject private BankService bankService;

  public Object importEbicsUser(Object bean, Map<String, Object> context) throws AxelorException {

    assert bean instanceof EbicsUser;

    EbicsUser user = (EbicsUser) bean;

    updateCertificate(user.getA005Certificate());
    updateCertificate(user.getE002Certificate());
    updateCertificate(user.getX002Certificate());

    EbicsPartner partner = user.getEbicsPartner();
    if (partner != null) {
      EbicsBank ebicsBank = partner.getEbicsBank();
      if (ebicsBank.getVersion() == 0) {
        for (EbicsCertificate cert : ebicsBank.getEbicsCertificateList()) {
          updateCertificate(cert);
        }
        Bank bank = ebicsBank.getBank();
        if (bank.getVersion() == 0) {
          bankService.computeFullName(bank);
          bankService.splitBic(bank);
        }
      }
    }

    return user;
  }

  private void updateCertificate(EbicsCertificate cert) throws AxelorException {

    if (cert == null) {
      return;
    }

    String pem = cert.getPemString();
    if (pem == null) {
      return;
    }

    try {
      X509Certificate certificate = certificateService.convertToCertificate(pem);
      certificateService.updateCertificate(certificate, cert, false);
    } catch (IOException | CertificateException e) {
      e.printStackTrace();
    }
  }
}
