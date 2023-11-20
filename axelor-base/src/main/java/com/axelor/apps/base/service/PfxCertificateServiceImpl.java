/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.db.repo.PfxCertificateRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Enumeration;

public class PfxCertificateServiceImpl implements PfxCertificateService {
  protected PfxCertificateRepository pfxCertificateRepository;

  @Inject
  public PfxCertificateServiceImpl(PfxCertificateRepository pfxCertificateRepository) {
    this.pfxCertificateRepository = pfxCertificateRepository;
  }

  @Override
  public void setCertificateNameFromFile(PfxCertificate pfxCertificate) {
    String certificateName = pfxCertificate.getName();
    if (StringUtils.notEmpty(certificateName)) {
      return;
    }
    pfxCertificate.setName(getCertificateName(pfxCertificate));
  }

  @Override
  public String getCertificateName(PfxCertificate pfxCertificate) {
    MetaFile certificate = pfxCertificate.getCertificate();
    if (certificate == null) {
      return "";
    }

    return Files.getNameWithoutExtension(certificate.getFileName());
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void setValidityDates(PfxCertificate pfxCertificate) throws AxelorException {
    KeyStore keyStore = getKeyStore(pfxCertificate);
    setValidityDates(pfxCertificate, keyStore);
  }

  protected void setValidityDates(PfxCertificate pfxCertificate, KeyStore keyStore)
      throws AxelorException {
    try {
      Enumeration<?> aliases = keyStore.aliases();
      Date fromDate = null;
      Date toDate = null;

      while (aliases.hasMoreElements()) {
        String alias = (String) aliases.nextElement();
        fromDate = ((X509Certificate) keyStore.getCertificate(alias)).getNotBefore();
        toDate = ((X509Certificate) keyStore.getCertificate(alias)).getNotAfter();
      }
      setDates(pfxCertificate, fromDate, toDate);
    } catch (KeyStoreException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.PFX_CERTIFICATE_ACCESS_ERROR));
    }
  }

  protected void setDates(PfxCertificate pfxCertificate, Date fromDate, Date toDate) {
    if (fromDate != null) {
      pfxCertificate.setFromValidityDate(
          fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }
    if (toDate != null) {
      pfxCertificate.setToValidityDate(
          toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }
  }

  protected KeyStore getKeyStore(PfxCertificate pfxCertificate) throws AxelorException {
    try {
      File certificateFile =
          new File(MetaFiles.getPath(pfxCertificate.getCertificate()).toString());
      return KeyStore.getInstance(certificateFile, pfxCertificate.getPassword().toCharArray());
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.PFX_CERTIFICATE_WRONG_PASSWORD));
    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.PFX_CERTIFICATE_WRONG_FILE));
    }
  }
}
