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
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.massentry.MassEntryMoveCreateService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineMassEntryRecordServiceImpl implements MoveLineMassEntryRecordService {

  protected MoveLineMassEntryService moveLineMassEntryService;
  protected MoveLineRecordService moveLineRecordService;
  protected TaxAccountToolService taxAccountToolService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;
  protected MassEntryMoveCreateService massEntryMoveCreateService;
  protected MoveLineTaxService moveLineTaxService;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveLineMassEntryRecordServiceImpl(
      MoveLineMassEntryService moveLineMassEntryService,
      MoveLineRecordService moveLineRecordService,
      TaxAccountToolService taxAccountToolService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      MassEntryMoveCreateService massEntryMoveCreateService,
      MoveLineTaxService moveLineTaxService,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      MoveLineToolService moveLineToolService) {
    this.moveLineMassEntryService = moveLineMassEntryService;
    this.moveLineRecordService = moveLineRecordService;
    this.taxAccountToolService = taxAccountToolService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
    this.massEntryMoveCreateService = massEntryMoveCreateService;
    this.moveLineTaxService = moveLineTaxService;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  public void setCurrencyRate(Move move, MoveLineMassEntry moveLine) throws AxelorException {
    BigDecimal currencyRate = BigDecimal.ONE;

    currencyRate =
        moveLineMassEntryService.computeCurrentRate(
            currencyRate,
            move.getCompany(),
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
          moveLineMassEntryService.getPfpValidatorUserForInTaxAccount(
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
  public void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException {
    moveLineRecordService.refreshAccountInformation(moveLine, move);

    if (ObjectUtils.isEmpty(moveLine.getAccount())) {
      moveLine.setVatSystemSelect(
          taxAccountToolService.calculateVatSystem(
              moveLine.getPartner(),
              move.getCompany(),
              null,
              (move.getJournal().getJournalType().getTechnicalTypeSelect()
                  == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE),
              (move.getJournal().getJournalType().getTechnicalTypeSelect()
                  == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)));
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
      moveLine.setAccount(null);
      moveLine.setPartner(null);
      moveLine.setPartnerId(null);
      moveLine.setPartnerSeq(null);
      moveLine.setPartnerFullName(null);
      moveLine.setMovePartnerBankDetails(null);
      moveLine.setVatSystemSelect(null);
      moveLine.setTaxLineSet(Sets.newHashSet());
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
      moveLine.setTaxLineSet(newMoveLine.getTaxLineSet());
      moveLine.setAnalyticDistributionTemplate(newMoveLine.getAnalyticDistributionTemplate());
      moveLine.setCurrencyCode(newMoveLine.getCurrencyCode());
    }
  }

  @Override
  public void setMovePaymentCondition(MoveLineMassEntry moveLine, int journalTechnicalTypeSelect) {
    if (journalTechnicalTypeSelect != JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER) {
      moveLine.setMovePaymentCondition(moveLine.getPartner().getPaymentCondition());
    } else {
      moveLine.setMovePaymentCondition(null);
    }
  }

  @Override
  public void setMovePaymentMode(MoveLineMassEntry moveLine, Integer technicalTypeSelect) {
    switch (technicalTypeSelect) {
      case JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE:
        moveLine.setMovePaymentMode(moveLine.getPartner().getOutPaymentMode());
        break;
      case JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE:
        moveLine.setMovePaymentMode(moveLine.getPartner().getInPaymentMode());
        break;
      default:
        moveLine.setMovePaymentMode(null);
        break;
    }
  }

  @Override
  public void setVatSystemSelect(MoveLineMassEntry moveLine, Move move) throws AxelorException {
    moveLine.setVatSystemSelect(moveLineTaxService.getVatSystem(move, moveLine));
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
    } else {
      moveLine.setAccount(null);
    }
    moveLine.setTaxLineSet(
        moveLoadDefaultConfigService.getTaxLineSet(move, moveLine, accountingAccount));
  }

  @Override
  public void setAnalytics(MoveLine newMoveLine, MoveLine moveLine) {
    newMoveLine.setAnalyticDistributionTemplate(moveLine.getAnalyticDistributionTemplate());
    newMoveLine.setAxis1AnalyticAccount(moveLine.getAxis1AnalyticAccount());
    newMoveLine.setAxis2AnalyticAccount(moveLine.getAxis2AnalyticAccount());
    newMoveLine.setAxis3AnalyticAccount(moveLine.getAxis3AnalyticAccount());
    newMoveLine.setAxis4AnalyticAccount(moveLine.getAxis4AnalyticAccount());
    newMoveLine.setAxis5AnalyticAccount(moveLine.getAxis5AnalyticAccount());
  }

  @Override
  public void setMoveStatusSelect(List<MoveLineMassEntry> massEntryLines, Integer newStatusSelect) {
    for (MoveLineMassEntry line : massEntryLines) {
      if (!Objects.equals(MoveRepository.STATUS_ACCOUNTED, line.getMoveStatusSelect())) {
        line.setMoveStatusSelect(newStatusSelect);
      }
    }
  }

  @Override
  public void setNextTemporaryMoveNumber(MoveLineMassEntry moveLine, Move move) {
    moveLine.setTemporaryMoveNumber(
        massEntryMoveCreateService.getMaxTemporaryMoveNumber(move.getMoveLineMassEntryList()) + 1);
  }

  @Override
  public void setPartner(MoveLineMassEntry moveLine, Move move) throws AxelorException {
    if (moveLine.getPartner() == null) {
      this.resetPartner(moveLine, null);
    } else {
      if (move != null && move.getJournal() != null) {
        int journalTechnicalTypeSelect =
            move.getJournal().getJournalType().getTechnicalTypeSelect();
        this.setMovePaymentMode(moveLine, journalTechnicalTypeSelect);
        move.setPartner(moveLine.getPartner());
        this.setMovePaymentCondition(moveLine, journalTechnicalTypeSelect);
        this.loadAccountInformation(move, moveLine);
        move.setPartner(null);
        this.setVatSystemSelect(moveLine, move);
      }
      this.setMovePartnerBankDetails(moveLine);
      this.setCurrencyCode(moveLine);
    }
  }

  @Override
  public MoveLineMassEntry setInputAction(MoveLineMassEntry moveLine, Move move) {
    if (moveLine.getInputAction() == MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_MOVE) {
      moveLine = moveLineMassEntryService.createMoveLineMassEntry(move.getCompany());
      moveLineToolService.setDecimals(moveLine, move);
      this.setNextTemporaryMoveNumber(moveLine, move);

      moveLine.setCounter(1);
    }
    return moveLine;
  }

  @Override
  public void fillAnalyticMoveLineMassEntryList(
      MoveLineMassEntry moveLineMassEntry, MoveLine moveLine) {
    moveLineMassEntry.clearAnalyticMoveLineMassEntryList();
    if (CollectionUtils.isNotEmpty(moveLine.getAnalyticMoveLineList())) {
      for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
        AnalyticMoveLine copyAnalyticMoveLine =
            analyticMoveLineRepository.copy(analyticMoveLine, false);
        moveLineMassEntry.addAnalyticMoveLineMassEntryListItem(copyAnalyticMoveLine);
      }
    }
  }

  @Override
  public void fillAnalyticMoveLineList(MoveLineMassEntry moveLineMassEntry, MoveLine moveLine) {
    moveLine.clearAnalyticMoveLineList();
    if (CollectionUtils.isNotEmpty(moveLineMassEntry.getAnalyticMoveLineMassEntryList())) {
      for (AnalyticMoveLine analyticMoveLine :
          moveLineMassEntry.getAnalyticMoveLineMassEntryList()) {
        AnalyticMoveLine copyAnalyticMoveLine =
            analyticMoveLineRepository.copy(analyticMoveLine, false);
        moveLine.addAnalyticMoveLineListItem(copyAnalyticMoveLine);
      }
    }
  }

  @Override
  public void fillAnalyticMoveLineMassEntryList(MoveLineMassEntry moveLineMassEntry) {
    moveLineMassEntry.clearAnalyticMoveLineMassEntryList();
    if (ObjectUtils.notEmpty(moveLineMassEntry.getAnalyticMoveLineList())) {
      moveLineMassEntry
          .getAnalyticMoveLineList()
          .forEach(
              analyticMoveLine -> {
                moveLineMassEntry.addAnalyticMoveLineMassEntryListItem(
                    analyticMoveLineRepository.copy(analyticMoveLine, false));
              });
      moveLineMassEntry.clearAnalyticMoveLineList();
    }
  }
}
