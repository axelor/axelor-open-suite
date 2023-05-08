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
package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.AccountingCutOffService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.moveline.MoveLineServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoveLineBankPaymentServiceImpl extends MoveLineServiceImpl
    implements MoveLineBankPaymentService {

  @Inject
  public MoveLineBankPaymentServiceImpl(
      MoveLineRepository moveLineRepository,
      InvoiceRepository invoiceRepository,
      PaymentService paymentService,
      MoveLineToolService moveLineToolService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      InvoiceTermService invoiceTermService,
      MoveLineControlService moveLineControlService,
      AccountingCutOffService cutOffService) {
    super(
        moveLineRepository,
        invoiceRepository,
        paymentService,
        moveLineToolService,
        appAccountService,
        accountConfigService,
        invoiceTermService,
        moveLineControlService,
        cutOffService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine removePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
    postedNbrs.remove(postedNbr);
    posted = String.join(",", postedNbrs);
    moveLine.setPostedNbr(posted);
    return moveLine;
  }

  @Override
  public void setIsSelectedBankReconciliation(MoveLine moveLine) {
    if (moveLine.getIsSelectedBankReconciliation() != null) {
      moveLine.setIsSelectedBankReconciliation(!moveLine.getIsSelectedBankReconciliation());
    } else {
      moveLine.setIsSelectedBankReconciliation(true);
    }
  }
}
