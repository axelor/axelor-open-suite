package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MoveLineMassEntryRecordServiceImpl implements MoveLineMassEntryRecordService {

  protected MoveLineMassEntryService moveLineMassEntryService;
  protected AccountingSituationService accountingSituationService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;

  @Inject
  public MoveLineMassEntryRecordServiceImpl(
      MoveLineMassEntryService moveLineMassEntryService,
      AccountingSituationService accountingSituationService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService) {
    this.moveLineMassEntryService = moveLineMassEntryService;
    this.accountingSituationService = accountingSituationService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
  }

  @Override
  public void setCurrencyRate(Move move, MoveLineMassEntry moveLine) throws AxelorException {
    BigDecimal currencyRate = BigDecimal.ONE;

    currencyRate =
        moveLineMassEntryService.computeCurrentRate(
            currencyRate,
            move.getMoveLineMassEntryList(),
            move.getCurrency(),
            move.getCompanyCurrency(),
            moveLine.getTemporaryMoveNumber(),
            moveLine.getOriginDate());

    moveLine.setCurrencyRate(currencyRate);
  }

  @Override
  public void resetDebit(MoveLineMassEntry moveLine) {
    if (moveLine.getCredit().signum() != 0 && moveLine.getDebit().signum() != 0) {
      moveLine.setDebit(BigDecimal.ZERO);
    }
  }

  @Override
  public void setMovePfpValidatorUser(MoveLineMassEntry moveLine, Company company) {
    if (ObjectUtils.notEmpty(company)) {
      moveLine.setMovePfpValidatorUser(
          Beans.get(MoveLineMassEntryService.class)
              .getPfpValidatorUserForInTaxAccount(
                  moveLine.getAccount(), company, moveLine.getPartner()));
    }
  }

  @Override
  public void setCutOff(MoveLineMassEntry moveLine) {
    if (moveLine.getAccount() != null && !moveLine.getAccount().getManageCutOffPeriod()) {
      moveLine.setCutOffStartDate(null);
      moveLine.setCutOffEndDate(null);
    } else if (ObjectUtils.isEmpty(moveLine.getCutOffStartDate())
        && ObjectUtils.isEmpty(moveLine.getCutOffEndDate())
        && ObjectUtils.notEmpty(moveLine.getAccount())) {
      moveLine.setCutOffStartDate(moveLine.getDate());
      moveLine.setCutOffEndDate(moveLine.getDate());
    }
  }

  @Override
  public void setMovePartnerBankDetails(MoveLineMassEntry moveLine) {
    moveLine.setMovePartnerBankDetails(
        moveLine.getPartner().getBankDetailsList().stream()
            .filter(it -> it.getIsDefault() && it.getActive())
            .findFirst()
            .orElse(null));
  }

  @Override
  public void setCurrencyCode(MoveLineMassEntry moveLine) {
    moveLine.setCurrencyCode(
        moveLine.getPartner().getCurrency() != null
            ? moveLine.getPartner().getCurrency().getCodeISO()
            : null);
  }

  @Override
  public void resetPartner(MoveLineMassEntry moveLine, MoveLineMassEntry newMoveLine) {
    if (newMoveLine == null) {
      moveLine.setPartner(null);
      moveLine.setPartnerId(null);
      moveLine.setPartnerSeq(null);
      moveLine.setPartnerFullName(null);
      moveLine.setMovePartnerBankDetails(null);
      moveLine.setVatSystemSelect(null);
      moveLine.setTaxLine(null);
      moveLine.setAnalyticDistributionTemplate(null);
      moveLine.setCurrencyCode(null);
    } else {
      Partner newPartner = newMoveLine.getPartner();
      moveLine.setPartner(newPartner);
      moveLine.setPartnerId(newPartner.getId());
      moveLine.setPartnerSeq(newPartner.getPartnerSeq());
      moveLine.setPartnerFullName(newPartner.getFullName());
      moveLine.setMovePartnerBankDetails(newMoveLine.getMovePartnerBankDetails());
      moveLine.setVatSystemSelect(newMoveLine.getVatSystemSelect());
      moveLine.setTaxLine(newMoveLine.getTaxLine());
      moveLine.setAnalyticDistributionTemplate(newMoveLine.getAnalyticDistributionTemplate());
      moveLine.setCurrencyCode(newMoveLine.getCurrencyCode());
    }
  }

  @Override
  public void setMovePaymentCondition(MoveLineMassEntry moveLine, int journalTechnicalTypeSelect) {
    moveLine.setMovePaymentCondition(null);
    if (journalTechnicalTypeSelect != JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
      moveLine.setMovePaymentCondition(moveLine.getPartner().getPaymentCondition());
    }
  }

  @Override
  public void setMovePaymentMode(MoveLineMassEntry moveLine, Integer technicalTypeSelect) {
    switch (technicalTypeSelect) {
      case 1:
        moveLine.setMovePaymentMode(moveLine.getPartner().getOutPaymentMode());
        break;
      case 2:
        moveLine.setMovePaymentMode(moveLine.getPartner().getInPaymentMode());
        break;
      default:
        moveLine.setMovePaymentMode(null);
        break;
    }
  }

  @Override
  public void setVatSystemSelect(MoveLineMassEntry moveLine, Move move) {
    AccountingSituation accountingSituation =
        accountingSituationService.getAccountingSituation(moveLine.getPartner(), move.getCompany());
    moveLine.setVatSystemSelect(null);
    if (accountingSituation != null) {
      moveLine.setVatSystemSelect(accountingSituation.getVatSystemSelect());
    }
  }

  @Override
  public void loadAccountInformation(Move move, MoveLineMassEntry moveLine) throws AxelorException {
    Account accountingAccount =
        moveLoadDefaultConfigService.getAccountingAccountFromAccountConfig(move);

    if (accountingAccount != null) {
      moveLine.setAccount(accountingAccount);

      AnalyticDistributionTemplate analyticDistributionTemplate =
          accountingAccount.getAnalyticDistributionTemplate();
      if (accountingAccount.getAnalyticDistributionAuthorized()
          && analyticDistributionTemplate != null) {
        moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);
      }
    }
    moveLine.setTaxLine(moveLoadDefaultConfigService.getTaxLine(move, moveLine, accountingAccount));
  }

  @Override
  public void setAnalytics(MoveLine newMoveLine, MoveLine moveLine) {
    newMoveLine.setAnalyticDistributionTemplate(moveLine.getAnalyticDistributionTemplate());
    newMoveLine.setAxis1AnalyticAccount(moveLine.getAxis1AnalyticAccount());
    newMoveLine.setAxis2AnalyticAccount(moveLine.getAxis2AnalyticAccount());
    newMoveLine.setAxis3AnalyticAccount(moveLine.getAxis3AnalyticAccount());
    newMoveLine.setAxis4AnalyticAccount(moveLine.getAxis4AnalyticAccount());
    newMoveLine.setAxis5AnalyticAccount(moveLine.getAxis5AnalyticAccount());
    newMoveLine.setAnalyticMoveLineList(moveLine.getAnalyticMoveLineList());
  }

  @Override
  public void setMoveStatusSelect(List<MoveLineMassEntry> massEntryLines, Integer newStatusSelect) {
    for (MoveLineMassEntry line : massEntryLines) {
      if (!Objects.equals(MoveRepository.STATUS_ACCOUNTED, line.getMoveStatusSelect())) {
        line.setMoveStatusSelect(newStatusSelect);
      }
    }
  }
}
