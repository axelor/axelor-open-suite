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
