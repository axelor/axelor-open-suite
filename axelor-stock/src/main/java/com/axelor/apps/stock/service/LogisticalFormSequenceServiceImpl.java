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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class LogisticalFormSequenceServiceImpl implements LogisticalFormSequenceService {

  protected SequenceService sequenceService;

  @Inject
  public LogisticalFormSequenceServiceImpl(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public void setSequence(LogisticalForm logisticalForm) throws AxelorException {
    Company company = logisticalForm.getCompany();
    String sequenceNumber =
        sequenceService.getSequenceNumber(
            "logisticalForm", company, LogisticalForm.class, "deliveryNumberSeq", logisticalForm);
    if (Strings.isNullOrEmpty(sequenceNumber)) {
      throw new AxelorException(
          Sequence.class,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(StockExceptionMessage.LOGISTICAL_FORM_MISSING_SEQUENCE),
          company != null ? company.getName() : null);
    }
    logisticalForm.setDeliveryNumberSeq(sequenceNumber);
  }
}
