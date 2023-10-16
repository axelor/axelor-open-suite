package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.apps.account.service.move.MoveControlService;
import com.axelor.apps.account.service.move.MoveCustAccountService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveSequenceService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.move.MoveValidateHRServiceImpl;
import com.google.inject.Inject;

public class MoveValidateBudgetServiceImpl extends MoveValidateHRServiceImpl {

  protected MoveBudgetService moveBudgetService;

  @Inject
  public MoveValidateBudgetServiceImpl(
      MoveLineControlService moveLineControlService,
      MoveLineToolService moveLineToolService,
      AccountConfigService accountConfigService,
      MoveSequenceService moveSequenceService,
      MoveCustAccountService moveCustAccountService,
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveRepository moveRepository,
      AccountRepository accountRepository,
      PartnerRepository partnerRepository,
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      FixedAssetGenerationService fixedAssetGenerationService,
      MoveLineTaxService moveLineTaxService,
      PeriodServiceAccount periodServiceAccount,
      MoveControlService moveControlService,
      MoveCutOffService moveCutOffService,
      MoveLineCheckService moveLineCheckService,
      ExpenseRepository expenseRepository,
      MoveBudgetService moveBudgetService) {
    super(
        moveLineControlService,
        moveLineToolService,
        accountConfigService,
        moveSequenceService,
        moveCustAccountService,
        moveToolService,
        moveInvoiceTermService,
        moveRepository,
        accountRepository,
        partnerRepository,
        appBaseService,
        appAccountService,
        fixedAssetGenerationService,
        moveLineTaxService,
        periodServiceAccount,
        moveControlService,
        moveCutOffService,
        moveLineCheckService,
        expenseRepository);
    this.moveBudgetService = moveBudgetService;
  }

  @Override
  public void accounting(Move move) throws AxelorException {

    moveBudgetService.autoComputeBudgetDistribution(move);

    super.accounting(move);
  }
}
