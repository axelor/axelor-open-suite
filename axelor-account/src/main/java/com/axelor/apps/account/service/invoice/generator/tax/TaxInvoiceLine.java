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
package com.axelor.apps.account.service.invoice.generator.tax;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.invoice.InvoiceJournalService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;
import com.axelor.apps.account.service.invoice.tax.InvoiceLineTaxToolService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.account.util.TaxConfiguration;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxInvoiceLine extends TaxGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected CurrencyScaleService currencyScaleService;
  protected TaxAccountService taxAccountService;
  protected TaxAccountToolService taxAccountToolService;
  protected InvoiceJournalService invoiceJournalService;
  protected InvoiceLineTaxToolService invoiceLineTaxToolService;

  public TaxInvoiceLine(Invoice invoice, List<InvoiceLine> invoiceLines) {
    super(invoice, invoiceLines);

    this.currencyScaleService = Beans.get(CurrencyScaleService.class);
    this.taxAccountService = Beans.get(TaxAccountService.class);
    this.taxAccountToolService = Beans.get(TaxAccountToolService.class);
    this.invoiceJournalService = Beans.get(InvoiceJournalService.class);
    this.invoiceLineTaxToolService = Beans.get(InvoiceLineTaxToolService.class);
  }

  /**
   * Créer les lignes de TVA de la facure. La création des lignes de TVA se basent sur les lignes de
   * factures
   *
   * @return La liste des lignes de TVA de la facture.
   * @throws AxelorException
   */
  @Override
  public List<InvoiceLineTax> creates() throws AxelorException {

    Map<TaxConfiguration, InvoiceLineTax> map = new HashMap<>();

    List<InvoiceLineTax> updatedInvoiceLineTaxList =
        new ArrayList<>(invoice.getInvoiceLineTaxList());
    invoice.getInvoiceLineTaxList().clear();

    if (invoiceLines != null && !invoiceLines.isEmpty()) {

      LOG.debug("Creation of lines with taxes for the invoices lines");

      for (InvoiceLine invoiceLine : invoiceLines) {
        // map is updated with created invoice line taxes
        createInvoiceLineTaxes(invoiceLine, map);
      }
    }

    FiscalPosition fiscalPosition = invoice.getFiscalPosition();

    if (fiscalPosition == null || !fiscalPosition.getCustomerSpecificNote()) {
      if (invoiceLines != null) {
        invoice.setSpecificNotes(
            invoiceLines.stream()
                .map(InvoiceLine::getTaxEquiv)
                .filter(Objects::nonNull)
                .map(TaxEquiv::getSpecificNote)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("\n")));
      }
    } else {
      invoice.setSpecificNotes(invoice.getPartner().getSpecificTaxNote());
    }

    return finalizeInvoiceLineTaxes(map, updatedInvoiceLineTaxList);
  }

  protected void createInvoiceLineTaxes(
      InvoiceLine invoiceLine, Map<TaxConfiguration, InvoiceLineTax> map) throws AxelorException {
    Set<TaxLine> taxLineSet = invoiceLine.getTaxLineSet();
    int vatSystem = 0;
    Account imputedAccount;

    if (CollectionUtils.isNotEmpty(taxLineSet)) {
      for (TaxLine taxLine : taxLineSet) {
        if (taxLine.getValue().signum() != 0) {
          vatSystem =
              taxAccountToolService.calculateVatSystem(
                  invoice.getPartner(),
                  invoice.getCompany(),
                  invoiceLine.getAccount(),
                  (invoice.getOperationTypeSelect()
                          == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                      || invoice.getOperationTypeSelect()
                          == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND),
                  (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
                      || invoice.getOperationTypeSelect()
                          == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND));

          imputedAccount = getImputedAccount(invoiceLine, taxLine, vatSystem);
        } else {
          vatSystem = 0;
          imputedAccount = null;
        }

        createOrUpdateInvoiceLineTax(invoiceLine, taxLine, imputedAccount, vatSystem, map);
      }

      TaxEquiv taxEquiv = invoiceLine.getTaxEquiv();
      AppBaseService appBaseService = Beans.get(AppBaseService.class);
      TaxService taxService = Beans.get(TaxService.class);
      Set<TaxLine> taxLineRCSet = new HashSet<>();
      if (taxEquiv != null && taxEquiv.getReverseCharge()) {
        // We get active tax line if it exist, else we fetch one in taxLine list of reverse charge
        // tax
        taxLineRCSet =
            taxService.getTaxLineSet(
                taxEquiv.getReverseChargeTaxSet(),
                appBaseService.getTodayDate(
                    Optional.ofNullable(invoiceLine.getInvoice())
                        .map(Invoice::getCompany)
                        .orElse(null)));
      }
      if (CollectionUtils.isNotEmpty(taxLineRCSet)) {
        for (TaxLine taxLineRC : taxLineRCSet) {
          imputedAccount = getImputedAccount(invoiceLine, taxLineRC, vatSystem);
          createOrUpdateInvoiceLineTaxRc(invoiceLine, taxLineRC, imputedAccount, vatSystem, map);
        }
      }
    }
  }

  protected Account getImputedAccount(InvoiceLine invoiceLine, TaxLine taxLine, int vatSystem)
      throws AxelorException {
    return taxAccountService.getAccount(
        taxLine.getTax(),
        invoice.getCompany(),
        invoiceJournalService.getJournal(invoice),
        invoiceLine.getAccount(),
        vatSystem,
        invoiceLine.getFixedAssets(),
        InvoiceToolService.getFunctionalOrigin(invoice));
  }

  protected void createOrUpdateInvoiceLineTax(
      InvoiceLine invoiceLine,
      TaxLine taxLine,
      Account imputedAccount,
      int vatSystem,
      Map<TaxConfiguration, InvoiceLineTax> map) {
    LOG.debug("Tax {}", taxLine);

    TaxConfiguration taxConfiguration = new TaxConfiguration(taxLine, imputedAccount, vatSystem);
    InvoiceLineTax invoiceLineTax = map.get(taxConfiguration);

    if (invoiceLineTax != null) {
      updateInvoiceLineTax(invoiceLine, invoiceLineTax, vatSystem);
      invoiceLineTax.setReverseCharged(false);
    } else {
      invoiceLineTax = createInvoiceLineTax(invoiceLine, taxLine, imputedAccount, vatSystem);
      invoiceLineTax.setReverseCharged(false);
      map.put(taxConfiguration, invoiceLineTax);
    }
  }

  protected void createOrUpdateInvoiceLineTaxRc(
      InvoiceLine invoiceLine,
      TaxLine taxLineRC,
      Account imputedAccount,
      int vatSystem,
      Map<TaxConfiguration, InvoiceLineTax> map) {
    TaxConfiguration taxConfiguration = new TaxConfiguration(taxLineRC, imputedAccount, vatSystem);
    InvoiceLineTax invoiceLineTaxRC = map.get(taxConfiguration);
    if (invoiceLineTaxRC != null) {
      updateInvoiceLineTax(invoiceLine, invoiceLineTaxRC, vatSystem);
    } else {
      invoiceLineTaxRC = createInvoiceLineTax(invoiceLine, taxLineRC, imputedAccount, vatSystem);
      map.put(taxConfiguration, invoiceLineTaxRC);
    }
    invoiceLineTaxRC.setReverseCharged(true);
  }

  protected void updateInvoiceLineTax(
      InvoiceLine invoiceLine, InvoiceLineTax invoiceLineTax, int vatSystem) {
    // Dans la devise de la facture
    invoiceLineTax.setExTaxBase(invoiceLineTax.getExTaxBase().add(invoiceLine.getExTaxTotal()));
    // Dans la devise de la société
    invoiceLineTax.setCompanyExTaxBase(
        currencyScaleService.getCompanyScaledValue(
            invoiceLine,
            invoiceLineTax.getCompanyExTaxBase().add(invoiceLine.getCompanyExTaxTotal())));

    invoiceLineTax.setVatSystemSelect(vatSystem);
  }

  protected InvoiceLineTax createInvoiceLineTax(
      InvoiceLine invoiceLine, TaxLine taxLine, Account imputedAccount, int vatSystem) {
    InvoiceLineTax invoiceLineTax = new InvoiceLineTax();
    invoiceLineTax.setInvoice(invoice);

    // Dans la devise de la facture
    invoiceLineTax.setExTaxBase(invoiceLine.getExTaxTotal());
    // Dans la devise de la comptabilité du tiers
    invoiceLineTax.setCompanyExTaxBase(
        currencyScaleService.getCompanyScaledValue(invoice, invoiceLine.getCompanyExTaxTotal()));

    invoiceLineTax.setImputedAccount(imputedAccount);
    invoiceLineTax.setVatSystemSelect(vatSystem);
    invoiceLineTax.setTaxLine(taxLine);
    invoiceLineTax.setCoefficient(invoiceLine.getCoefficient());
    invoiceLineTax.setTaxType(
        Optional.ofNullable(taxLine.getTax()).map(Tax::getTaxType).orElse(null));

    return invoiceLineTax;
  }

  protected List<InvoiceLineTax> finalizeInvoiceLineTaxes(
      Map<TaxConfiguration, InvoiceLineTax> map, List<InvoiceLineTax> updatedInvoiceLineTaxList) {
    List<InvoiceLineTax> invoiceLineTaxList = new ArrayList<>();

    List<InvoiceLineTax> deductibleTaxList =
        map.values().stream()
            .filter(it -> !this.isNonDeductibleTax(it))
            .collect(Collectors.toList());
    List<InvoiceLineTax> nonDeductibleTaxList =
        map.values().stream().filter(this::isNonDeductibleTax).collect(Collectors.toList());

    nonDeductibleTaxList.forEach(
        it ->
            computeAndAddInvoiceLineTax(
                it, updatedInvoiceLineTaxList, invoiceLineTaxList, deductibleTaxList));
    deductibleTaxList.forEach(
        it ->
            computeAndAddInvoiceLineTax(
                it, updatedInvoiceLineTaxList, invoiceLineTaxList, nonDeductibleTaxList));

    return invoiceLineTaxList;
  }

  protected void computeAndAddInvoiceLineTax(
      InvoiceLineTax invoiceLineTax,
      List<InvoiceLineTax> updatedInvoiceLineTaxList,
      List<InvoiceLineTax> invoiceLineTaxList,
      List<InvoiceLineTax> nonDeductibleTaxList) {
    TaxLine taxLine = invoiceLineTax.getTaxLine();
    BigDecimal taxValue =
        taxLine
            .getValue()
            .divide(
                BigDecimal.valueOf(100), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    if (taxLine.getTax().getIsNonDeductibleTax()) {
      taxValue = this.getAdjustedNonDeductibleTaxValue(taxValue, nonDeductibleTaxList);
    } else {
      taxValue = this.getAdjustedTaxValue(taxValue, nonDeductibleTaxList);
    }

    // Dans la devise de la facture
    BigDecimal exTaxBase =
        (invoiceLineTax.getReverseCharged())
            ? invoiceLineTax.getExTaxBase().negate()
            : invoiceLineTax.getExTaxBase();
    BigDecimal taxTotal =
        computeAmount(
            exTaxBase, taxValue, currencyScaleService.getScale(invoiceLineTax.getInvoice()), null);

    if (!ObjectUtils.isEmpty(updatedInvoiceLineTaxList)) {
      for (InvoiceLineTax updatedInvoiceLineTax : updatedInvoiceLineTaxList) {
        if (invoiceLineTaxToolService.isManageByAmount(invoiceLineTax)
            && Objects.equals(
                updatedInvoiceLineTax.getVatSystemSelect(), invoiceLineTax.getVatSystemSelect())
            && updatedInvoiceLineTax.getImputedAccount() == invoiceLineTax.getImputedAccount()
            && updatedInvoiceLineTax.getPercentageTaxTotal().compareTo(taxTotal) == 0
            && updatedInvoiceLineTax.getExTaxBase().compareTo(invoiceLineTax.getExTaxBase()) == 0) {
          invoiceLineTaxList.add(updatedInvoiceLineTax);

          LOG.debug(
              "Tax line : Tax total => {}, Total W.T. => {}",
              invoiceLineTax.getTaxTotal(),
              invoiceLineTax.getInTaxTotal());
          return;
        }
      }
    }

    invoiceLineTax.setTaxTotal(taxTotal);
    invoiceLineTax.setPercentageTaxTotal(taxTotal);
    invoiceLineTax.setInTaxTotal(invoiceLineTax.getExTaxBase().add(taxTotal));

    // Dans la devise de la société
    BigDecimal companyExTaxBase =
        (invoiceLineTax.getReverseCharged())
            ? invoiceLineTax.getCompanyExTaxBase().negate()
            : invoiceLineTax.getCompanyExTaxBase();
    BigDecimal companyTaxTotal =
        computeAmount(
            companyExTaxBase,
            taxValue,
            currencyScaleService.getCompanyScale(invoiceLineTax.getInvoice()),
            null);

    invoiceLineTax.setCompanyTaxTotal(companyTaxTotal);
    invoiceLineTax.setCompanyInTaxTotal(invoiceLineTax.getCompanyExTaxBase().add(companyTaxTotal));

    invoiceLineTaxList.add(invoiceLineTax);

    LOG.debug(
        "Tax line : Tax total => {}, Total W.T. => {}",
        invoiceLineTax.getTaxTotal(),
        invoiceLineTax.getInTaxTotal());
  }

  protected boolean isNonDeductibleTax(InvoiceLineTax invoiceLineTax) {
    return Optional.of(invoiceLineTax.getTaxLine().getTax().getIsNonDeductibleTax()).orElse(false);
  }

  protected BigDecimal getAdjustedTaxValue(
      BigDecimal taxValue, List<InvoiceLineTax> nonDeductibleTaxList) {
    BigDecimal deductibleTaxValue =
        nonDeductibleTaxList.stream()
            .map(InvoiceLineTax::getTaxLine)
            .map(TaxLine::getValue)
            .reduce(BigDecimal::multiply)
            .orElse(BigDecimal.ZERO)
            .divide(
                BigDecimal.valueOf(100), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);

    return BigDecimal.ONE
        .subtract(deductibleTaxValue)
        .multiply(taxValue)
        .setScale(AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  protected BigDecimal getAdjustedNonDeductibleTaxValue(
      BigDecimal taxValue, List<InvoiceLineTax> deductibleTaxList) {
    BigDecimal nonDeductibleTaxValue = BigDecimal.ZERO;

    for (InvoiceLineTax invoiceLineTax : deductibleTaxList) {
      nonDeductibleTaxValue =
          nonDeductibleTaxValue.add(
              taxValue.multiply(
                  invoiceLineTax
                      .getTaxLine()
                      .getValue()
                      .divide(
                          BigDecimal.valueOf(100),
                          AppBaseService.COMPUTATION_SCALING,
                          RoundingMode.HALF_UP)));
    }
    return nonDeductibleTaxValue.setScale(AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
