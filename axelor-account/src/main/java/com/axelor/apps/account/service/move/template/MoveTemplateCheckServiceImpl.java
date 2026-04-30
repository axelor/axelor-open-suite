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

import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.MoveTemplateType;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveTemplateLineRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.MoveTemplateTypeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveTemplateCheckServiceImpl implements MoveTemplateCheckService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveTemplateRepository moveTemplateRepo;
  protected MoveTemplateTaxService moveTemplateTaxService;

  @Inject
  public MoveTemplateCheckServiceImpl(
      MoveTemplateRepository moveTemplateRepo, MoveTemplateTaxService moveTemplateTaxService) {
    this.moveTemplateRepo = moveTemplateRepo;
    this.moveTemplateTaxService = moveTemplateTaxService;
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
   * Checks tax amount coherence. For each tax, calculates: sum(base_amounts) x rate and compares
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
            .filter(line -> !moveTemplateTaxService.isTaxAccountLine(line) && line.getTax() != null)
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

  @Transactional
  protected void validateMoveTemplateLine(MoveTemplate moveTemplate) {
    moveTemplate.setIsValid(true);

    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      line.setIsValid(true);
    }

    moveTemplateRepo.save(moveTemplate);
  }

  protected BigDecimal getTaxRate(Tax tax) {
    if (tax == null || tax.getActiveTaxLine() == null) {
      return null;
    }
    return tax.getActiveTaxLine().getValue();
  }

  protected boolean checkTaxAmountCoherenceInAmount(List<MoveTemplateLine> lines)
      throws AxelorException {
    // Lignes sur compte de taxe (avec ou sans Tax liee)
    List<MoveTemplateLine> taxAccountLines =
        lines.stream()
            .filter(moveTemplateTaxService::isTaxAccountLine)
            .collect(Collectors.toList());

    // Lignes de base avec Tax (excluding lines with computeTaxAtCreation)
    List<MoveTemplateLine> baseLines =
        lines.stream()
            .filter(
                line ->
                    !moveTemplateTaxService.isTaxAccountLine(line)
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

    // Montants reels des lignes de taxe
    Map<Tax, BigDecimal> actualTax = new HashMap<>();
    for (MoveTemplateLine taxLine : taxAccountLines) {
      BigDecimal amount = taxLine.getDebit().add(taxLine.getCredit());
      Tax tax = taxLine.getTax();
      // Si pas de Tax liee et une seule taxe dans les bases -> associer a cette taxe
      if (tax == null && distinctTaxes.size() == 1) {
        tax = distinctTaxes.iterator().next();
      }
      if (tax != null) {
        actualTax.merge(tax, amount, BigDecimal::add);
      }
    }

    // Comparer avec tolerance de 0.01
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
    // Lignes sur compte de taxe (avec ou sans Tax liee)
    List<MoveTemplateLine> taxAccountLines =
        lines.stream()
            .filter(moveTemplateTaxService::isTaxAccountLine)
            .collect(Collectors.toList());

    // Lignes de base avec Tax (excluding lines with computeTaxAtCreation)
    List<MoveTemplateLine> baseLines =
        lines.stream()
            .filter(
                line ->
                    !moveTemplateTaxService.isTaxAccountLine(line)
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
      // Si pas de Tax liee et une seule taxe dans les bases -> associer a cette taxe
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
