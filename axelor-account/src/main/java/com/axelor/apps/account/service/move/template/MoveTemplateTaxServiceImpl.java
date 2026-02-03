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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.common.collect.Sets;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public class MoveTemplateTaxServiceImpl implements MoveTemplateTaxService {

  protected TaxService taxService;
  protected MoveLineTaxService moveLineTaxService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveTemplateTaxServiceImpl(
      TaxService taxService,
      MoveLineTaxService moveLineTaxService,
      MoveLineCreateService moveLineCreateService,
      MoveLineToolService moveLineToolService) {
    this.taxService = taxService;
    this.moveLineTaxService = moveLineTaxService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  public boolean isTaxAccountLine(MoveTemplateLine line) {
    return line.getAccount() != null
        && line.getAccount().getAccountType() != null
        && AccountTypeRepository.TYPE_TAX.equals(
            line.getAccount().getAccountType().getTechnicalTypeSelect());
  }

  @Override
  public BigDecimal computeBaseAmountExcludingTax(
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

  @Override
  public void setTaxInfoOnMoveLine(
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

  @Override
  public int createOrUpdateTaxMoveLine(
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
}
