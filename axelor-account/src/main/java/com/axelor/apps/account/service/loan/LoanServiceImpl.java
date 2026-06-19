/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.loan;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanManagementConfig;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;

public class LoanServiceImpl implements LoanService {

  protected SequenceService sequenceService;

  @Inject
  public LoanServiceImpl(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public String generateReference(Loan loan) throws AxelorException {
    LoanManagementConfig config = loan.getLoanManagementConfig();
    String companyName = loan.getCompany() != null ? loan.getCompany().getName() : "";
    Sequence sequence = config != null ? config.getSequence() : null;
    if (sequence == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.LOAN_SEQUENCE_MISSING),
          companyName);
    }
    String reference = sequenceService.getSequenceNumber(sequence, Loan.class, "reference", loan);
    if (reference == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.LOAN_SEQUENCE_MISSING),
          companyName);
    }
    return reference;
  }
}
