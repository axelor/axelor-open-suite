/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.DebtRecoveryHistoryRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.debtrecovery.PayerQualityService;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.google.inject.Inject;

public class PayerQualityBankPaymentService extends PayerQualityService {

  @Inject
  public PayerQualityBankPaymentService(
      AppAccountService appAccountService,
      PartnerRepository partnerRepository,
      DebtRecoveryHistoryRepository debtRecoveryHistoryRepo) {
    super(appAccountService, partnerRepository, debtRecoveryHistoryRepo);
  }

  @Override
  protected boolean checkTechnicalRejectOk(MoveLine moveLine) {
    return !moveLine.getInterbankCodeLine().getTechnicalRejectOk();
  }
}
