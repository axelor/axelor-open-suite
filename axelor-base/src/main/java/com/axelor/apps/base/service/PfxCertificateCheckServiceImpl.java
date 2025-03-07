package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import java.time.LocalDate;

public class PfxCertificateCheckServiceImpl implements PfxCertificateCheckService {
  protected final AppBaseService appBaseService;

  @Inject
  public PfxCertificateCheckServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public void checkValidity(PfxCertificate pfxCertificate) throws AxelorException {
    LocalDate pfxCertificateFromDate = pfxCertificate.getFromValidityDate();
    LocalDate pfxCertificateToDate = pfxCertificate.getToValidityDate();
    LocalDate todayDate = appBaseService.getTodayDate(null);

    if (!LocalDateHelper.isBetween(pfxCertificateFromDate, pfxCertificateToDate, todayDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.PFX_CERTIFICATE_VALIDITY_ERROR));
    }
  }
}
