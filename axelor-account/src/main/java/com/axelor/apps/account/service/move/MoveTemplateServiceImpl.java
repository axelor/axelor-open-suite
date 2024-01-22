/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.MoveTemplateType;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.MoveTemplateLineRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.MoveTemplateTypeRepository;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveTemplateServiceImpl implements MoveTemplateService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepo;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PartnerRepository partnerRepo;
  protected TaxService taxService;
  protected BankDetailsService bankDetailsService;
  protected MoveTemplateRepository moveTemplateRepo;
  protected MoveLineTaxService moveLineTaxService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveRecordUpdateService moveRecordUpdateService;

  protected List<String> exceptionsList;

  @Inject
  public MoveTemplateServiceImpl(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepo,
      MoveLineCreateService moveLineCreateService,
      PartnerRepository partnerRepo,
      TaxService taxService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      BankDetailsService bankDetailsService,
      MoveTemplateRepository moveTemplateRepo,
      MoveLineTaxService moveLineTaxService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveRecordUpdateService moveRecordUpdateService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRepo = moveRepo;
    this.moveLineCreateService = moveLineCreateService;
    this.partnerRepo = partnerRepo;
    this.taxService = taxService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.bankDetailsService = bankDetailsService;
    this.moveTemplateRepo = moveTemplateRepo;
    this.moveLineTaxService = moveLineTaxService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveRecordUpdateService = moveRecordUpdateService;

    this.exceptionsList = Lists.newArrayList();
  }

  @Override
  public List<String> getExceptionsList() {
    return exceptionsList;
  }

  @Transactional
  protected void validateMoveTemplateLine(MoveTemplate moveTemplate) {
    moveTemplate.setIsValid(true);

    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      line.setIsValid(true);
    }

    moveTemplateRepo.save(moveTemplate);
  }

  @Transactional(rollbackOn = {Exception.class})
  public List<Long> generateMove(
      MoveTemplateType moveTemplateType,
      MoveTemplate moveTemplate,
      List<HashMap<String, Object>> dataList,
      LocalDate date,
      List<HashMap<String, Object>> moveTemplateList)
      throws AxelorException {

    if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
      return this.generateMove(moveTemplate, dataList);
    } else if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_AMOUNT) {
      return this.generateMove(date, moveTemplateList);
    }
    return new ArrayList<>();
  }

  protected List<Long> generateMove(
      MoveTemplate moveTemplate, List<HashMap<String, Object>> dataList) throws AxelorException {
    List<Long> moveList = new ArrayList<>();
    BigDecimal hundred = new BigDecimal(100);

    if (CollectionUtils.isEmpty(dataList)) {
      return moveList;
    }

    for (HashMap<String, Object> data : dataList) {
      LocalDate moveDate = LocalDate.parse(data.get("date").toString(), DateTimeFormatter.ISO_DATE);
      boolean isDebit = false;
      Partner debitPartner = null;
      Partner creditPartner = null;
      String origin = moveTemplate.getCode();
      BigDecimal moveBalance = new BigDecimal(data.get("moveBalance").toString());
      Partner partner = null;
      if (data.get("debitPartner") != null) {
        debitPartner =
            partnerRepo.find(
                Long.parseLong(
                    ((HashMap<String, Object>) data.get("debitPartner")).get("id").toString()));
        partner = debitPartner;
      }
      if (data.get("creditPartner") != null) {
        creditPartner =
            partnerRepo.find(
                Long.parseLong(
                    ((HashMap<String, Object>) data.get("creditPartner")).get("id").toString()));
        partner = creditPartner;
      }
      if (moveTemplate.getJournal().getCompany() != null) {
        int[] functionalOriginTab = new int[0];
        if (!ObjectUtils.isEmpty(moveTemplate.getJournal().getAuthorizedFunctionalOriginSelect())) {
          functionalOriginTab =
              Arrays.stream(
                      moveTemplate
                          .getJournal()
                          .getAuthorizedFunctionalOriginSelect()
                          .replace(" ", "")
                          .split(","))
                  .mapToInt(Integer::parseInt)
                  .toArray();
        }
        BankDetails companyBankDetails = null;
        if (moveTemplate != null
            && moveTemplate.getJournal() != null
            && moveTemplate.getJournal().getCompany() != null) {
          companyBankDetails =
              bankDetailsService.getDefaultCompanyBankDetails(
                  moveTemplate.getJournal().getCompany(), null, partner, null);
        }
        Move move =
            moveCreateService.createMove(
                moveTemplate.getJournal(),
                moveTemplate.getJournal().getCompany(),
                null,
                partner,
                moveDate,
                moveDate,
                null,
                partner != null ? partner.getFiscalPosition() : null,
                MoveRepository.TECHNICAL_ORIGIN_TEMPLATE,
                !ObjectUtils.isEmpty(functionalOriginTab) ? functionalOriginTab[0] : 0,
                origin,
                moveTemplate.getDescription(),
                companyBankDetails);

        int counter = 1;

        for (MoveTemplateLine moveTemplateLine : moveTemplate.getMoveTemplateLineList()) {
          if (!AccountTypeRepository.TYPE_TAX.equals(
              moveTemplateLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
            partner = null;
            if (moveTemplateLine.getDebitCreditSelect().equals(MoveTemplateLineRepository.DEBIT)) {
              isDebit = true;
              if (moveTemplateLine.getHasPartnerToDebit()) {
                partner = debitPartner;
              }
            } else if (moveTemplateLine
                .getDebitCreditSelect()
                .equals(MoveTemplateLineRepository.CREDIT)) {
              isDebit = false;
              if (moveTemplateLine.getHasPartnerToCredit()) {
                partner = creditPartner;
              }
            }

            BigDecimal amount =
                moveBalance
                    .multiply(moveTemplateLine.getPercentage())
                    .divide(hundred, RoundingMode.HALF_UP);

            MoveLine moveLine =
                moveLineCreateService.createMoveLine(
                    move,
                    partner,
                    moveTemplateLine.getAccount(),
                    amount,
                    isDebit,
                    moveDate,
                    moveDate,
                    counter,
                    origin,
                    moveTemplateLine.getName());
            move.getMoveLineList().add(moveLine);

            Tax tax = moveTemplateLine.getTax();

            if (tax != null) {
              TaxLine taxLine = taxService.getTaxLine(tax, moveDate);
              if (taxLine != null) {
                moveLine.setTaxLine(taxLine);
                moveLine.setTaxRate(taxLine.getValue());
                moveLine.setTaxCode(tax.getCode());
                moveLine.setVatSystemSelect(moveLineTaxService.getVatSystem(move, moveLine));
              }
            }
            List<AnalyticMoveLine> analyticMoveLineList =
                CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())
                    ? new ArrayList<>()
                    : new ArrayList<>(moveLine.getAnalyticMoveLineList());
            moveLine.clearAnalyticMoveLineList();
            moveLine.setAnalyticDistributionTemplate(
                moveTemplateLine.getAnalyticDistributionTemplate());

            moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);

            if (CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
              moveLine.setAnalyticMoveLineList(analyticMoveLineList);
            }

            moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, false);
            moveRecordUpdateService.updateDueDate(move, false, false);

            counter++;
          }
        }

        moveLineTaxService.autoTaxLineGenerate(move, null, true);
        manageAccounting(moveTemplate, move);

        moveList.add(move.getId());
      }
    }
    return moveList;
  }

  protected List<Long> generateMove(
      LocalDate moveDate, List<HashMap<String, Object>> moveTemplateList) throws AxelorException {
    List<Long> moveList = new ArrayList<>();
    String taxLineDescription = "";

    for (HashMap<String, Object> moveTemplateMap : moveTemplateList) {

      MoveTemplate moveTemplate =
          moveTemplateRepo.find(Long.valueOf((Integer) moveTemplateMap.get("id")));

      if (moveTemplate.getJournal().getCompany() != null) {
        int[] functionalOriginTab = new int[0];
        if (!ObjectUtils.isEmpty(moveTemplate.getJournal().getAuthorizedFunctionalOriginSelect())) {
          functionalOriginTab =
              Arrays.stream(
                      moveTemplate
                          .getJournal()
                          .getAuthorizedFunctionalOriginSelect()
                          .replace(" ", "")
                          .split(","))
                  .mapToInt(Integer::parseInt)
                  .toArray();
        }
        Partner moveTemplatePartner = fillPartnerWithMoveTemplate(moveTemplate);

        BankDetails companyBankDetails = null;
        if (moveTemplate != null
            && moveTemplate.getJournal() != null
            && moveTemplate.getJournal().getCompany() != null) {
          companyBankDetails =
              bankDetailsService.getDefaultCompanyBankDetails(
                  moveTemplate.getJournal().getCompany(),
                  moveTemplatePartner == null || moveTemplatePartner.getOutPaymentMode() == null
                      ? null
                      : moveTemplatePartner.getOutPaymentMode(),
                  moveTemplatePartner,
                  null);
        }

        Move move =
            moveCreateService.createMove(
                moveTemplate.getJournal(),
                moveTemplate.getJournal().getCompany(),
                null,
                moveTemplatePartner,
                moveDate,
                moveDate,
                moveTemplatePartner == null || moveTemplatePartner.getOutPaymentMode() == null
                    ? null
                    : moveTemplatePartner.getOutPaymentMode(),
                null,
                MoveRepository.TECHNICAL_ORIGIN_TEMPLATE,
                !ObjectUtils.isEmpty(functionalOriginTab) ? functionalOriginTab[0] : 0,
                moveTemplate.getFullName(),
                moveTemplate.getDescription(),
                companyBankDetails);

        int counter = 1;

        for (MoveTemplateLine moveTemplateLine : moveTemplate.getMoveTemplateLineList()) {
          if (!AccountTypeRepository.TYPE_TAX.equals(
              moveTemplateLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
            BigDecimal amount = moveTemplateLine.getDebit().add(moveTemplateLine.getCredit());

            MoveLine moveLine =
                moveLineCreateService.createMoveLine(
                    move,
                    moveTemplateLine.getPartner(),
                    moveTemplateLine.getAccount(),
                    amount,
                    moveTemplateLine.getDebit().compareTo(BigDecimal.ZERO) > 0,
                    moveDate,
                    moveDate,
                    counter,
                    moveTemplate.getFullName(),
                    moveTemplateLine.getName());
            move.getMoveLineList().add(moveLine);

            Tax tax = moveTemplateLine.getTax();

            if (tax != null) {
              TaxLine taxLine = taxService.getTaxLine(tax, moveDate);
              if (taxLine != null) {
                moveLine.setTaxLine(taxLine);
                moveLine.setTaxRate(taxLine.getValue());
                moveLine.setTaxCode(tax.getCode());
                moveLine.setVatSystemSelect(moveLineTaxService.getVatSystem(move, moveLine));
              }
            }

            List<AnalyticMoveLine> analyticMoveLineList =
                CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())
                    ? new ArrayList<>()
                    : new ArrayList<>(moveLine.getAnalyticMoveLineList());
            moveLine.clearAnalyticMoveLineList();
            moveLine.setAnalyticDistributionTemplate(
                moveTemplateLine.getAnalyticDistributionTemplate());

            moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, false);
            moveRecordUpdateService.updateDueDate(move, false, false);
            moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);

            if (CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
              moveLine.setAnalyticMoveLineList(analyticMoveLineList);
            }
            counter++;
          } else {
            taxLineDescription = moveTemplateLine.getName();
          }
        }

        if (ObjectUtils.notEmpty(move)
            && ObjectUtils.notEmpty(move.getMoveLineList())
            && ObjectUtils.notEmpty(move.getMoveLineList().get(0).getPartner())) {
          move.getMoveLineList().get(0).getPartner().getBankDetailsList().stream()
              .filter(it -> it.getIsDefault() && it.getActive())
              .findFirst()
              .ifPresent(move::setPartnerBankDetails);
        }

        move.setDescription(taxLineDescription);
        moveLineTaxService.autoTaxLineGenerate(move, null, false);
        manageAccounting(moveTemplate, move);

        move.setDescription(moveTemplate.getDescription());
        if (!Strings.isNullOrEmpty(move.getDescription())) {
          for (MoveLine moveline : move.getMoveLineList()) {
            moveline.setDescription(move.getDescription());
          }
        }
        moveList.add(move.getId());
      }
    }
    return moveList;
  }

  protected void manageAccounting(MoveTemplate moveTemplate, Move move) {
    if (!moveTemplate.getAutomaticallyValidate()) {
      return;
    }
    try {
      moveValidateService.accounting(move);
    } catch (AxelorException e) {
      String message = e.getMessage();
      if (!exceptionsList.contains(message)) {
        exceptionsList.add(message);
      }
    }
  }

  @Override
  public boolean checkValidity(MoveTemplate moveTemplate) {

    MoveTemplateType moveTemplateType = moveTemplate.getMoveTemplateType();

    if (moveTemplateType == null) {
      return false;
    }

    if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
      return this.checkValidityInPercentage(moveTemplate);
    } else if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_AMOUNT) {
      return this.checkValidityInAmount(moveTemplate);
    } else {
      return false;
    }
  }

  protected boolean checkValidityInPercentage(MoveTemplate moveTemplate) {
    BigDecimal debitPercent = BigDecimal.ZERO;
    BigDecimal creditPercent = BigDecimal.ZERO;
    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      LOG.debug("Adding percent: {}", line.getPercentage());
      if (MoveTemplateLineRepository.DEBIT.equals(line.getDebitCreditSelect())) {
        debitPercent = debitPercent.add(line.getPercentage());
      } else {
        creditPercent = creditPercent.add(line.getPercentage());
      }
    }

    LOG.debug("Debit percent: {}, Credit percent: {}", debitPercent, creditPercent);
    if (debitPercent.compareTo(BigDecimal.ZERO) != 0
        && creditPercent.compareTo(BigDecimal.ZERO) != 0
        && debitPercent.compareTo(creditPercent) == 0) {
      this.validateMoveTemplateLine(moveTemplate);
      return true;
    } else {
      return false;
    }
  }

  protected boolean checkValidityInAmount(MoveTemplate moveTemplate) {
    BigDecimal debit = BigDecimal.ZERO;
    BigDecimal credit = BigDecimal.ZERO;
    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      debit = debit.add(line.getDebit());
      credit = credit.add(line.getCredit());
    }

    LOG.debug("Debit : {}, Credit : {}", debit, credit);
    if (debit.compareTo(BigDecimal.ZERO) != 0
        && credit.compareTo(BigDecimal.ZERO) != 0
        && debit.compareTo(credit) == 0) {
      this.validateMoveTemplateLine(moveTemplate);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Map<String, Object> computeTotals(MoveTemplate moveTemplate) {
    Map<String, Object> values = new HashMap<>();

    if (moveTemplate.getMoveTemplateLineList() == null
        || moveTemplate.getMoveTemplateLineList().isEmpty()) {
      return values;
    }
    values.put("$totalLines", moveTemplate.getMoveTemplateLineList().size());

    BigDecimal totalDebit =
        moveTemplate.getMoveTemplateLineList().stream()
            .map(MoveTemplateLine::getDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalDebit", totalDebit);

    BigDecimal totalCredit =
        moveTemplate.getMoveTemplateLineList().stream()
            .map(MoveTemplateLine::getCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalCredit", totalCredit);

    BigDecimal difference = totalDebit.subtract(totalCredit);
    values.put("$difference", difference);

    return values;
  }

  protected Partner fillPartnerWithMoveTemplate(MoveTemplate moveTemplate) {
    Partner partner = null;

    if (moveTemplate.getMoveTemplateLineList().stream()
            .filter(it -> it.getPartner() != null)
            .map(it -> it.getPartner())
            .distinct()
            .count()
        == 1) {
      partner = moveTemplate.getMoveTemplateLineList().get(0).getPartner();
    }
    return partner;
  }
}
