package com.axelor.apps.account.service.fixedasset;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetDerogatoryLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class FixedAssetDerogatoryLineMoveServiceImpl
    implements FixedAssetDerogatoryLineMoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository;
  protected MoveCreateService moveCreateService;
  protected MoveRepository moveRepo;

  @Inject
  public FixedAssetDerogatoryLineMoveServiceImpl(
      FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository,
      MoveCreateService moveCreateService,
      MoveRepository moveRepo) {
    this.fixedAssetDerogatoryLineRepository = fixedAssetDerogatoryLineRepository;
    this.moveCreateService = moveCreateService;
    this.moveRepo = moveRepo;
  }

  @Override
  @Transactional
  public void realize(FixedAssetDerogatoryLine fixedAssetLine) throws AxelorException {
    Objects.requireNonNull(fixedAssetLine);
    log.debug("Computing action 'realize' on " + fixedAssetLine);
    BigDecimal derogatoryAmount = fixedAssetLine.getDerogatoryAmount();
    BigDecimal incomeDepreciationAmount = fixedAssetLine.getIncomeDepreciationAmount();
    BigDecimal derogatoryBalanceAmount = fixedAssetLine.getDerogatoryBalanceAmount();
    // If derogatoryAmount or incomeDepreciationAmount or derogatoryBalanceAmount are greater than 0
    // then we proceed.
    if ((derogatoryAmount != null && derogatoryAmount.compareTo(BigDecimal.ZERO) > 0)
        || (incomeDepreciationAmount != null
            && incomeDepreciationAmount.abs().compareTo(BigDecimal.ZERO) > 0)
        || (derogatoryBalanceAmount != null
            && derogatoryBalanceAmount.compareTo(BigDecimal.ZERO) > 0)) {

      generateMove(fixedAssetLine);
      fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_REALIZED);
      fixedAssetDerogatoryLineRepository.save(fixedAssetLine);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  private void generateMove(FixedAssetDerogatoryLine fixedAssetLine) throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();

    log.debug(
        "Creating an fixed asset derogatory line specific accounting entry {} (Company : {}, Journal : {})",
        fixedAsset.getReference(),
        company.getName(),
        journal.getCode());

    // Creating move
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            company.getCurrency(),
            partner,
            date,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<>();

      String origin = fixedAsset.getReference();
      Account debitLineAccount = null;
      Account creditLineAccount = null;
      BigDecimal amount = null;
      
      if (fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount() == null 
    		  || fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount() == null
    		  || fixedAsset.getFixedAssetCategory().getIncomeDepreciationDerogatoryAccount() == null) {
    	  throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD,
    			  I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
    			  "Expense depreciation derogatory/Capital depreciation derogatory/Income depreciation derogatory");
      }
      if (fixedAssetLine.getDerogatoryAmount().compareTo(BigDecimal.ZERO) > 0) {
        debitLineAccount =
            fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
        creditLineAccount =
            fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
        amount = fixedAssetLine.getDerogatoryAmount().abs();
      }

      else if (fixedAssetLine.getIncomeDepreciationAmount().abs().compareTo(BigDecimal.ZERO) > 0){
        debitLineAccount =
            fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
        creditLineAccount =
            fixedAsset.getFixedAssetCategory().getIncomeDepreciationDerogatoryAccount();
        amount = fixedAssetLine.getIncomeDepreciationAmount().abs();
      }
      // We call this function only if derogatoryAmount or incomeDepreciationAmount or
      // derogatoryBalanceAmount are greater than 0
      // This means that if derogatoryAmount or incomeDepreciationAmount are not greater than zero then
      // derogatoryBalanceAmount is.
      else {
	  	debitLineAccount =
                fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
            creditLineAccount =
                fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
            amount = fixedAssetLine.getDerogatoryAmount().abs(); 
      }

      // Creating accounting debit move line
      MoveLine debitMoveLine =
          new MoveLine(
              move,
              partner,
              debitLineAccount,
              date,
              null,
              1,
              amount,
              BigDecimal.ZERO,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(debitMoveLine);
      // Creating accounting debit move line
      MoveLine creditMoveLine =
          new MoveLine(
              move,
              partner,
              creditLineAccount,
              date,
              null,
              2,
              BigDecimal.ZERO,
              amount,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(creditMoveLine);
      move.getMoveLineList().addAll(moveLines);
    }

    moveRepo.save(move);
    fixedAssetLine.setDerogatoryDepreciationMove(move);
  }
}
