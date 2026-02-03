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
package com.axelor.apps.account.service.move.template;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.MoveTemplateType;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.MoveTemplateLineRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.MoveTemplateTypeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
  protected AnalyticLineService analyticLineService;
  protected PartnerRepository partnerRepo;
  protected TaxService taxService;
  protected BankDetailsService bankDetailsService;
  protected MoveTemplateRepository moveTemplateRepo;
  protected MoveLineTaxService moveLineTaxService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveLineToolService moveLineToolService;
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
      AnalyticLineService analyticLineService,
      BankDetailsService bankDetailsService,
      MoveTemplateRepository moveTemplateRepo,
      MoveLineTaxService moveLineTaxService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveLineToolService moveLineToolService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRepo = moveRepo;
    this.moveLineCreateService = moveLineCreateService;
    this.partnerRepo = partnerRepo;
    this.taxService = taxService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.analyticLineService = analyticLineService;
    this.bankDetailsService = bankDetailsService;
    this.moveTemplateRepo = moveTemplateRepo;
    this.moveLineTaxService = moveLineTaxService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveRecordUpdateService = moveRecordUpdateService;
    this.moveLineToolService = moveLineToolService;

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
                moveTemplate.getFunctionalOriginSelect(),
                origin,
                moveTemplate.getDescription(),
                companyBankDetails);

        int counter = 1;
        Map<MoveLine, TaxLine> linesToSetTaxLineAfterAutoGen = new HashMap<>();

        // Check if template has explicit tax lines - if so, don't apply computeTaxAtCreation
        boolean hasExplicitTaxLines =
            moveTemplate.getMoveTemplateLineList().stream().anyMatch(this::isTaxAccountLine);

        for (MoveTemplateLine moveTemplateLine : moveTemplate.getMoveTemplateLineList()) {
          boolean isTaxLine = isTaxAccountLine(moveTemplateLine);

          if (!isTaxLine) {
            // Base line (non-tax account)
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

            amount =
                computeBaseAmountExcludingTax(
                    moveTemplateLine, moveDate, amount, hasExplicitTaxLines);

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

            setTaxInfoOnMoveLine(
                move, moveLine, moveTemplateLine, moveDate, linesToSetTaxLineAfterAutoGen);

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

            analyticLineService.setAnalyticAccount(moveLine, move.getCompany());
            moveLineToolService.setDecimals(moveLine, move);
            moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, false);
            moveRecordUpdateService.updateDueDate(move, false, false);

            counter++;
          } else {
            // Tax line - create or update
            BigDecimal percentage =
                moveTemplateLine.getPercentage() != null
                    ? moveTemplateLine.getPercentage()
                    : BigDecimal.ZERO;
            if (percentage.compareTo(BigDecimal.ZERO) == 0) {
              continue;
            }

            BigDecimal amount =
                moveBalance.multiply(percentage).divide(hundred, 2, RoundingMode.HALF_UP);
            isDebit =
                MoveTemplateLineRepository.DEBIT.equals(moveTemplateLine.getDebitCreditSelect());

            counter =
                createOrUpdateTaxMoveLine(
                    move,
                    moveTemplate,
                    moveTemplateLine,
                    moveDate,
                    amount,
                    isDebit,
                    counter,
                    origin);
          }
        }

        if (!hasExplicitTaxLines) {
          // No explicit tax lines in template, use autoTaxLineGenerate
          boolean hasComputeTaxAtCreation =
              moveTemplate.getMoveTemplateLineList().stream()
                  .anyMatch(line -> Boolean.TRUE.equals(line.getComputeTaxAtCreation()));
          moveLineTaxService.autoTaxLineGenerate(move, null, !hasComputeTaxAtCreation);
        }

        // Set taxLineSet on lines with computeTaxAtCreation=false
        for (Map.Entry<MoveLine, TaxLine> entry : linesToSetTaxLineAfterAutoGen.entrySet()) {
          entry.getKey().setTaxLineSet(Sets.newHashSet(entry.getValue()));
        }

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
                moveTemplate.getFunctionalOriginSelect(),
                moveTemplate.getFullName(),
                moveTemplate.getDescription(),
                companyBankDetails);

        int counter = 1;
        Map<MoveLine, TaxLine> linesToSetTaxLineAfterAutoGen = new HashMap<>();

        // Check if template has explicit tax lines - if so, don't apply computeTaxAtCreation
        boolean hasExplicitTaxLines =
            moveTemplate.getMoveTemplateLineList().stream().anyMatch(this::isTaxAccountLine);

        for (MoveTemplateLine moveTemplateLine : moveTemplate.getMoveTemplateLineList()) {
          if (isTaxAccountLine(moveTemplateLine)) {
            taxLineDescription = moveTemplateLine.getName();
          }
          counter =
              generateMoveLine(
                  moveTemplate,
                  moveTemplateLine,
                  move,
                  moveDate,
                  hasExplicitTaxLines,
                  linesToSetTaxLineAfterAutoGen,
                  counter);
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

        if (!hasExplicitTaxLines) {
          // No explicit tax lines in template, use autoTaxLineGenerate
          moveLineTaxService.autoTaxLineGenerate(move, null, false);
        }

        // Set taxLineSet on lines with computeTaxAtCreation=false
        for (Map.Entry<MoveLine, TaxLine> entry : linesToSetTaxLineAfterAutoGen.entrySet()) {
          entry.getKey().setTaxLineSet(Sets.newHashSet(entry.getValue()));
        }

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

  protected int generateMoveLine(
      MoveTemplate moveTemplate,
      MoveTemplateLine moveTemplateLine,
      Move move,
      LocalDate moveDate,
      boolean hasExplicitTaxLines,
      Map<MoveLine, TaxLine> linesToSetTaxLineAfterAutoGen,
      int counter)
      throws AxelorException {
    boolean isTaxLine = isTaxAccountLine(moveTemplateLine);

    if (!isTaxLine) {
      // Base line (non-tax account)
      BigDecimal amount = moveTemplateLine.getDebit().add(moveTemplateLine.getCredit());

      amount =
          computeBaseAmountExcludingTax(moveTemplateLine, moveDate, amount, hasExplicitTaxLines);

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

      setTaxInfoOnMoveLine(
          move, moveLine, moveTemplateLine, moveDate, linesToSetTaxLineAfterAutoGen);

      List<AnalyticMoveLine> analyticMoveLineList =
          CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())
              ? new ArrayList<>()
              : new ArrayList<>(moveLine.getAnalyticMoveLineList());
      moveLine.clearAnalyticMoveLineList();
      moveLine.setAnalyticDistributionTemplate(moveTemplateLine.getAnalyticDistributionTemplate());

      moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, false);
      moveRecordUpdateService.updateDueDate(move, false, false);
      moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);
      moveLineToolService.setDecimals(moveLine, move);

      if (CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
        moveLine.setAnalyticMoveLineList(analyticMoveLineList);
      }
      analyticLineService.setAnalyticAccount(moveLine, move.getCompany());
      counter++;
    } else {
      // Tax line - create or update
      BigDecimal debit =
          moveTemplateLine.getDebit() != null ? moveTemplateLine.getDebit() : BigDecimal.ZERO;
      BigDecimal credit =
          moveTemplateLine.getCredit() != null ? moveTemplateLine.getCredit() : BigDecimal.ZERO;
      BigDecimal amount = debit.add(credit);
      if (amount.compareTo(BigDecimal.ZERO) == 0) {
        return counter;
      }
      boolean isDebit = debit.compareTo(BigDecimal.ZERO) > 0;

      counter =
          createOrUpdateTaxMoveLine(
              move,
              moveTemplate,
              moveTemplateLine,
              moveDate,
              amount,
              isDebit,
              counter,
              moveTemplate.getFullName());
    }

    return counter;
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
  public boolean checkValidity(MoveTemplate moveTemplate) throws AxelorException {

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

  protected boolean checkValidityInPercentage(MoveTemplate moveTemplate) throws AxelorException {
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

      if (!checkPartnerConsistency(moveTemplate)) {
        return false;
      }
      if (!checkMixedComputeTaxAtCreation(moveTemplate)) {
        return false;
      }
      if (!checkTaxAmountCoherence(moveTemplate)) {
        return false;
      }

      this.validateMoveTemplateLine(moveTemplate);
      return true;
    } else {
      return false;
    }
  }

  protected boolean checkValidityInAmount(MoveTemplate moveTemplate) throws AxelorException {
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

      if (!checkPartnerConsistency(moveTemplate)) {
        return false;
      }
      if (!checkMixedComputeTaxAtCreation(moveTemplate)) {
        return false;
      }
      if (!checkTaxAmountCoherence(moveTemplate)) {
        return false;
      }

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

  /**
   * Checks that all lines have the same partner (or no partner).
   *
   * <p>Valid cases:
   *
   * <ul>
   *   <li>All lines without partner
   *   <li>All lines with the same partner
   *   <li>Mix of lines with a single partner and lines without partner
   * </ul>
   *
   * <p>Invalid case: Lines with different partners
   */
  protected boolean checkPartnerConsistency(MoveTemplate moveTemplate) throws AxelorException {
    // Only check partner consistency for journals requiring partners (Expense=1, Sale=2)
    if (moveTemplate.getJournal() == null
        || moveTemplate.getJournal().getJournalType() == null
        || !java.util.Arrays.asList(
                JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
                JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)
            .contains(moveTemplate.getJournal().getJournalType().getTechnicalTypeSelect())) {
      return true;
    }

    List<MoveTemplateLine> lines = moveTemplate.getMoveTemplateLineList();
    if (CollectionUtils.isEmpty(lines)) {
      return true;
    }

    Set<Partner> distinctPartners =
        lines.stream()
            .map(MoveTemplateLine::getPartner)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (distinctPartners.size() > 1) {
      String partnerNames =
          distinctPartners.stream().map(Partner::getName).collect(Collectors.joining(", "));
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_PARTNER_INCONSISTENT),
          partnerNames);
    }
    return true;
  }

  /**
   * Checks tax amount coherence. For each tax, calculates: sum(base_amounts) × rate and compares
   * with the actual tax line amount.
   */
  protected boolean checkTaxAmountCoherence(MoveTemplate moveTemplate) throws AxelorException {
    List<MoveTemplateLine> lines = moveTemplate.getMoveTemplateLineList();
    if (CollectionUtils.isEmpty(lines)) {
      return true;
    }

    MoveTemplateType type = moveTemplate.getMoveTemplateType();
    if (type == null) {
      return true;
    }

    if (type.getTypeSelect() == MoveTemplateTypeRepository.TYPE_AMOUNT) {
      return checkTaxAmountCoherenceInAmount(lines);
    } else if (type.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
      return checkTaxAmountCoherenceInPercentage(lines);
    }
    return true;
  }

  /**
   * Checks that lines with the same account and tax have consistent computeTaxAtCreation values.
   */
  protected boolean checkMixedComputeTaxAtCreation(MoveTemplate moveTemplate)
      throws AxelorException {
    List<MoveTemplateLine> lines = moveTemplate.getMoveTemplateLineList();
    if (CollectionUtils.isEmpty(lines)) {
      return true;
    }

    // Group lines by account and tax, excluding tax account lines
    Map<String, List<MoveTemplateLine>> groupedLines =
        lines.stream()
            .filter(line -> !isTaxAccountLine(line) && line.getTax() != null)
            .collect(
                Collectors.groupingBy(
                    line ->
                        line.getAccount().getId()
                            + "-"
                            + (line.getTax() != null ? line.getTax().getId() : "null")));

    for (Map.Entry<String, List<MoveTemplateLine>> entry : groupedLines.entrySet()) {
      List<MoveTemplateLine> groupLines = entry.getValue();
      if (groupLines.size() > 1) {
        boolean firstValue = Boolean.TRUE.equals(groupLines.get(0).getComputeTaxAtCreation());
        boolean hasMixedValues =
            groupLines.stream()
                .anyMatch(
                    line -> Boolean.TRUE.equals(line.getComputeTaxAtCreation()) != firstValue);

        if (hasMixedValues) {
          MoveTemplateLine firstLine = groupLines.get(0);
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_MIXED_COMPUTE_TAX_AT_CREATION),
              firstLine.getAccount().getCode(),
              firstLine.getTax().getName());
        }
      }
    }
    return true;
  }

  protected boolean isTaxAccountLine(MoveTemplateLine line) {
    return line.getAccount() != null
        && line.getAccount().getAccountType() != null
        && AccountTypeRepository.TYPE_TAX.equals(
            line.getAccount().getAccountType().getTechnicalTypeSelect());
  }

  /**
   * Computes base amount excluding tax if computeTaxAtCreation is enabled.
   *
   * @param moveTemplateLine the template line containing tax configuration
   * @param moveDate the move date for tax line lookup
   * @param amount the original amount (TTC)
   * @param hasExplicitTaxLines whether the template has explicit tax lines
   * @return the computed base amount (HT if computeTaxAtCreation, unchanged otherwise)
   */
  protected BigDecimal computeBaseAmountExcludingTax(
      MoveTemplateLine moveTemplateLine,
      LocalDate moveDate,
      BigDecimal amount,
      boolean hasExplicitTaxLines)
      throws AxelorException {

    Tax tax = moveTemplateLine.getTax();

    // If computeTaxAtCreation is true and NO explicit tax lines in template,
    // compute base amount (excluding tax)
    if (Boolean.TRUE.equals(moveTemplateLine.getComputeTaxAtCreation())
        && tax != null
        && !hasExplicitTaxLines) {
      TaxLine taxLine = taxService.getTaxLine(tax, moveDate);
      if (taxLine != null && taxLine.getValue().compareTo(BigDecimal.ZERO) > 0) {
        // amount is TTC, compute HT: baseAmount = amount / (1 + rate/100)
        BigDecimal rate = taxLine.getValue();
        BigDecimal divisor =
            BigDecimal.ONE.add(rate.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP));
        amount = amount.divide(divisor, 2, RoundingMode.HALF_UP);
      }
    }

    return amount;
  }

  /**
   * Sets tax information on a base MoveLine.
   *
   * @param move the Move
   * @param moveLine the MoveLine to configure
   * @param moveTemplateLine the template line containing tax configuration
   * @param moveDate the move date for tax line lookup
   * @param linesToSetTaxLineAfterAutoGen map to store lines needing taxLineSet after auto
   *     generation
   */
  protected void setTaxInfoOnMoveLine(
      Move move,
      MoveLine moveLine,
      MoveTemplateLine moveTemplateLine,
      LocalDate moveDate,
      Map<MoveLine, TaxLine> linesToSetTaxLineAfterAutoGen)
      throws AxelorException {

    Tax tax = moveTemplateLine.getTax();

    if (tax != null) {
      TaxLine taxLine = taxService.getTaxLine(tax, moveDate);
      if (taxLine != null) {
        // Only set taxLineSet if computeTaxAtCreation is true
        // Otherwise autoTaxLineGenerate would create duplicate tax lines
        if (Boolean.TRUE.equals(moveTemplateLine.getComputeTaxAtCreation())) {
          moveLine.setTaxLineSet(Sets.newHashSet(taxLine));
        } else {
          // Store for later - set taxLineSet after autoTaxLineGenerate
          linesToSetTaxLineAfterAutoGen.put(moveLine, taxLine);
        }
        moveLine.setTaxRate(taxLine.getValue());
        moveLine.setTaxCode(tax.getCode());
        moveLine.setVatSystemSelect(moveLineTaxService.getVatSystem(move, moveLine));
      }
    }
  }

  /**
   * Creates or updates a tax MoveLine from a tax template line.
   *
   * @param move the Move
   * @param moveTemplate the MoveTemplate
   * @param taxTemplateLine the tax template line
   * @param moveDate the move date
   * @param amount the tax amount
   * @param isDebit whether this is a debit line
   * @param counter the current line counter
   * @param origin the origin string for the MoveLine
   * @return the updated counter value
   */
  protected int createOrUpdateTaxMoveLine(
      Move move,
      MoveTemplate moveTemplate,
      MoveTemplateLine taxTemplateLine,
      LocalDate moveDate,
      BigDecimal amount,
      boolean isDebit,
      int counter,
      String origin)
      throws AxelorException {

    // Get tax info from template or infer from base line
    Tax tax = taxTemplateLine.getTax();
    TaxLine taxLine = null;
    Integer vatSystemSelect = null;

    if (tax == null) {
      // Find base line with Tax to infer tax info
      MoveTemplateLine baseLineWithTax =
          moveTemplate.getMoveTemplateLineList().stream()
              .filter(line -> !isTaxAccountLine(line) && line.getTax() != null)
              .findFirst()
              .orElse(null);
      if (baseLineWithTax != null) {
        tax = baseLineWithTax.getTax();
        taxLine = taxService.getTaxLine(tax, moveDate);
        // Get vatSystemSelect from generated base MoveLine
        MoveLine baseMoveLineGenerated =
            move.getMoveLineList().stream()
                .filter(
                    ml ->
                        ml.getAccount() != null
                            && ml.getAccount().equals(baseLineWithTax.getAccount()))
                .findFirst()
                .orElse(null);
        if (baseMoveLineGenerated != null) {
          vatSystemSelect = baseMoveLineGenerated.getVatSystemSelect();
        }
      }
    } else {
      taxLine = taxService.getTaxLine(tax, moveDate);
    }

    if (vatSystemSelect == null) {
      vatSystemSelect = moveLineTaxService.getVatSystem(move, null);
    }

    final TaxLine finalTaxLine = taxLine;
    final Integer finalVatSystem = vatSystemSelect;

    // Find existing MoveLine with same account, taxLine and vatSystem
    MoveLine existingLine =
        move.getMoveLineList().stream()
            .filter(
                ml ->
                    ml.getAccount() != null
                        && ml.getAccount().equals(taxTemplateLine.getAccount())
                        && Objects.equals(finalVatSystem, ml.getVatSystemSelect())
                        && (finalTaxLine == null
                            || (ml.getSourceTaxLineSet() != null
                                && ml.getSourceTaxLineSet().contains(finalTaxLine))))
            .findFirst()
            .orElse(null);

    if (existingLine != null) {
      // Update existing line
      if (isDebit) {
        existingLine.setDebit(existingLine.getDebit().add(amount));
      } else {
        existingLine.setCredit(existingLine.getCredit().add(amount));
      }

      existingLine.setCurrencyAmount(existingLine.getCurrencyAmount().add(amount));
      // Set tax attributes if missing
      if (taxLine != null) {
        if (existingLine.getSourceTaxLineSet() == null
            || existingLine.getSourceTaxLineSet().isEmpty()) {
          existingLine.setSourceTaxLineSet(Sets.newHashSet(taxLine));
        }
        if (existingLine.getTaxLineSet() == null || existingLine.getTaxLineSet().isEmpty()) {
          existingLine.setTaxLineSet(Sets.newHashSet(taxLine));
        }
        if (existingLine.getTaxRate() == null) {
          existingLine.setTaxRate(taxLine.getValue());
        }
        if (existingLine.getTaxCode() == null && tax != null) {
          existingLine.setTaxCode(tax.getCode());
        }
      }
    } else {
      // Create new tax line
      MoveLine newTaxMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              taxTemplateLine.getPartner(),
              taxTemplateLine.getAccount(),
              amount,
              isDebit,
              moveDate,
              moveDate,
              counter,
              origin,
              taxTemplateLine.getName());

      if (taxLine != null) {
        newTaxMoveLine.setSourceTaxLineSet(Sets.newHashSet(taxLine));
        newTaxMoveLine.setTaxLineSet(Sets.newHashSet(taxLine));
        newTaxMoveLine.setTaxRate(taxLine.getValue());
        newTaxMoveLine.setTaxCode(tax.getCode());
      }
      newTaxMoveLine.setVatSystemSelect(vatSystemSelect);
      moveLineToolService.setDecimals(newTaxMoveLine, move);

      move.getMoveLineList().add(newTaxMoveLine);
      counter++;
    }

    return counter;
  }

  protected BigDecimal getTaxRate(Tax tax) {
    if (tax == null || tax.getActiveTaxLine() == null) {
      return null;
    }
    return tax.getActiveTaxLine().getValue();
  }

  protected boolean checkTaxAmountCoherenceInAmount(List<MoveTemplateLine> lines)
      throws AxelorException {
    // Lignes sur compte de taxe (avec ou sans Tax liée)
    List<MoveTemplateLine> taxAccountLines =
        lines.stream().filter(this::isTaxAccountLine).collect(Collectors.toList());

    // Lignes de base avec Tax (excluding lines with computeTaxAtCreation)
    List<MoveTemplateLine> baseLines =
        lines.stream()
            .filter(
                line ->
                    !isTaxAccountLine(line)
                        && line.getTax() != null
                        && !Boolean.TRUE.equals(line.getComputeTaxAtCreation()))
            .collect(Collectors.toList());

    if (baseLines.isEmpty()) {
      return true;
    }

    // If we have base lines with tax but no tax account lines, throw error
    if (taxAccountLines.isEmpty()) {
      MoveTemplateLine firstBaseLine = baseLines.get(0);
      Tax tax = firstBaseLine.getTax();
      BigDecimal rate = getTaxRate(tax);
      BigDecimal baseAmount = firstBaseLine.getDebit().add(firstBaseLine.getCredit());
      BigDecimal expectedTax =
          baseAmount.multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_TAX_AMOUNT_MISMATCH),
          tax.getName(),
          expectedTax,
          baseAmount,
          rate,
          BigDecimal.ZERO);
    }

    // Taxes distinctes des lignes de base
    Set<Tax> distinctTaxes =
        baseLines.stream().map(MoveTemplateLine::getTax).collect(Collectors.toSet());

    // Calculer taxe attendue par Tax avec les montants de base
    Map<Tax, BigDecimal> expectedTax = new HashMap<>();
    Map<Tax, BigDecimal> baseTotals = new HashMap<>();
    for (MoveTemplateLine base : baseLines) {
      Tax tax = base.getTax();
      BigDecimal rate = getTaxRate(tax);
      if (rate == null) {
        continue;
      }

      BigDecimal baseAmount = base.getDebit().add(base.getCredit());
      baseTotals.merge(tax, baseAmount, BigDecimal::add);
      BigDecimal expected =
          baseAmount.multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
      expectedTax.merge(tax, expected, BigDecimal::add);
    }

    // Montants réels des lignes de taxe
    Map<Tax, BigDecimal> actualTax = new HashMap<>();
    for (MoveTemplateLine taxLine : taxAccountLines) {
      BigDecimal amount = taxLine.getDebit().add(taxLine.getCredit());
      Tax tax = taxLine.getTax();
      // Si pas de Tax liée et une seule taxe dans les bases -> associer à cette taxe
      if (tax == null && distinctTaxes.size() == 1) {
        tax = distinctTaxes.iterator().next();
      }
      if (tax != null) {
        actualTax.merge(tax, amount, BigDecimal::add);
      }
    }

    // Comparer avec tolérance de 0.01
    for (Map.Entry<Tax, BigDecimal> entry : expectedTax.entrySet()) {
      Tax tax = entry.getKey();
      BigDecimal expected = entry.getValue();
      BigDecimal actual = actualTax.getOrDefault(tax, BigDecimal.ZERO);
      if (expected.subtract(actual).abs().compareTo(new BigDecimal("0.01")) > 0) {
        BigDecimal rate = getTaxRate(tax);
        BigDecimal baseTotal = baseTotals.getOrDefault(tax, BigDecimal.ZERO);
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_TAX_AMOUNT_MISMATCH),
            tax.getName(),
            expected,
            baseTotal,
            rate,
            actual);
      }
    }
    return true;
  }

  protected boolean checkTaxAmountCoherenceInPercentage(List<MoveTemplateLine> lines)
      throws AxelorException {
    // Lignes sur compte de taxe (avec ou sans Tax liée)
    List<MoveTemplateLine> taxAccountLines =
        lines.stream().filter(this::isTaxAccountLine).collect(Collectors.toList());

    // Lignes de base avec Tax (excluding lines with computeTaxAtCreation)
    List<MoveTemplateLine> baseLines =
        lines.stream()
            .filter(
                line ->
                    !isTaxAccountLine(line)
                        && line.getTax() != null
                        && !Boolean.TRUE.equals(line.getComputeTaxAtCreation()))
            .collect(Collectors.toList());

    if (baseLines.isEmpty()) {
      return true;
    }

    // If we have base lines with tax but no tax account lines, throw error
    if (taxAccountLines.isEmpty()) {
      MoveTemplateLine firstBaseLine = baseLines.get(0);
      Tax tax = firstBaseLine.getTax();
      BigDecimal rate = getTaxRate(tax);
      BigDecimal basePct = firstBaseLine.getPercentage();
      BigDecimal expectedTax =
          basePct.multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_TAX_AMOUNT_MISMATCH),
          tax.getName(),
          expectedTax,
          basePct,
          rate,
          BigDecimal.ZERO);
    }

    // Taxes distinctes des lignes de base
    Set<Tax> distinctTaxes =
        baseLines.stream().map(MoveTemplateLine::getTax).collect(Collectors.toSet());

    Map<Tax, BigDecimal> expectedTaxPct = new HashMap<>();
    Map<Tax, BigDecimal> basePctTotals = new HashMap<>();
    for (MoveTemplateLine base : baseLines) {
      Tax tax = base.getTax();
      BigDecimal rate = getTaxRate(tax);
      if (rate == null) {
        continue;
      }

      basePctTotals.merge(tax, base.getPercentage(), BigDecimal::add);
      BigDecimal expectedPct =
          base.getPercentage().multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
      expectedTaxPct.merge(tax, expectedPct, BigDecimal::add);
    }

    Map<Tax, BigDecimal> actualTaxPct = new HashMap<>();
    for (MoveTemplateLine taxLine : taxAccountLines) {
      Tax tax = taxLine.getTax();
      // Si pas de Tax liée et une seule taxe dans les bases -> associer à cette taxe
      if (tax == null && distinctTaxes.size() == 1) {
        tax = distinctTaxes.iterator().next();
      }
      if (tax != null) {
        actualTaxPct.merge(tax, taxLine.getPercentage(), BigDecimal::add);
      }
    }

    for (Map.Entry<Tax, BigDecimal> entry : expectedTaxPct.entrySet()) {
      Tax tax = entry.getKey();
      BigDecimal expected = entry.getValue();
      BigDecimal actual = actualTaxPct.getOrDefault(tax, BigDecimal.ZERO);
      if (expected.subtract(actual).abs().compareTo(new BigDecimal("0.01")) > 0) {
        BigDecimal rate = getTaxRate(tax);
        BigDecimal basePctTotal = basePctTotals.getOrDefault(tax, BigDecimal.ZERO);
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_TAX_AMOUNT_MISMATCH),
            tax.getName(),
            expected,
            basePctTotal,
            rate,
            actual);
      }
    }
    return true;
  }
}
