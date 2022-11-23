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

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.repo.InterbankCodeLineRepository;
import com.google.inject.Inject;

public class RejectImportBankPaymentService {

  protected InterbankCodeLineRepository interbankCodeLineRepo;
  protected AppAccountService appAccountService;

  @Inject
  public RejectImportBankPaymentService(
      InterbankCodeLineRepository interbankCodeLineRepo, AppAccountService appAccountService) {
    this.appAccountService = appAccountService;
    this.interbankCodeLineRepo = interbankCodeLineRepo;
  }

  /**
   * Fonction permettant de récupérer le motif de rejet/retour
   *
   * @param reasonCode Un code motifs de rejet/retour
   * @param interbankCodeOperation Le type d'opération :
   *     <ul>
   *       <li>0 = Virement
   *       <li>1 = Prélèvement/TIP/Télérèglement
   *       <li>2 = Prélèvement SEPA
   *       <li>3 = LCR/BOR
   *       <li>4 = Cheque
   *     </ul>
   *
   * @return Un motif de rejet/retour
   */
  public InterbankCodeLine getInterbankCodeLine(String reasonCode, int interbankCodeOperation) {
    switch (interbankCodeOperation) {
      case 0:
        return interbankCodeLineRepo
            .all()
            .filter(
                "self.code = ?1 AND self.interbankCode = ?2 AND self.transferCfonbOk = 'true'",
                reasonCode,
                appAccountService.getAppAccount().getTransferAndDirectDebitInterbankCode())
            .fetchOne();
      case 1:
        return interbankCodeLineRepo
            .all()
            .filter(
                "self.code = ?1 AND self.interbankCode = ?2 AND self.directDebitAndTipCfonbOk = 'true'",
                reasonCode,
                appAccountService.getAppAccount().getTransferAndDirectDebitInterbankCode())
            .fetchOne();
      case 2:
        return interbankCodeLineRepo
            .all()
            .filter(
                "self.code = ?1 AND self.interbankCode = ?2 AND self.directDebitSepaOk = 'true'",
                reasonCode,
                appAccountService.getAppAccount().getTransferAndDirectDebitInterbankCode())
            .fetchOne();
      case 3:
        return interbankCodeLineRepo
            .all()
            .filter(
                "self.code = ?1 AND self.interbankCode = ?2 AND self.lcrBorOk = 'true'",
                reasonCode,
                appAccountService.getAppAccount().getTransferAndDirectDebitInterbankCode())
            .fetchOne();
      case 4:
        return interbankCodeLineRepo
            .all()
            .filter(
                "self.code = ?1 AND self.interbankCode = ?2 AND self.chequeOk = 'true'",
                reasonCode,
                appAccountService.getAppAccount().getChequeInterbankCode())
            .fetchOne();
      default:
        return null;
    }
  }
}
