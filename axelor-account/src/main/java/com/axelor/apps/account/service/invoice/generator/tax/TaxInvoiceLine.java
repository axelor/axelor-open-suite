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
package com.axelor.apps.account.service.invoice.generator.tax;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxInvoiceLine extends TaxGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AppBaseService appBaseService;
  protected TaxService taxService;
  protected CurrencyScaleService currencyScaleService;
  protected TaxAccountToolService taxAccountToolService;

  public TaxInvoiceLine(Invoice invoice, List<InvoiceLine> invoiceLines) {
    super(invoice, invoiceLines);

    this.appBaseService = Beans.get(AppBaseService.class);
    this.taxService = Beans.get(TaxService.class);
    this.taxAccountToolService = Beans.get(TaxAccountToolService.class);
    this.currencyScaleService = Beans.get(CurrencyScaleService.class);
    this.taxAccountToolService = Beans.get(TaxAccountToolService.class);
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

    List<InvoiceLineTax> invoiceLineTaxList = new ArrayList<>();

    if (invoiceLines != null && !invoiceLines.isEmpty()) {

      LOG.debug("Creation of lines with taxes for the invoices lines");

      for (InvoiceLine invoiceLine : invoiceLines) {
        // map is updated with created invoice line taxes
        createInvoiceLineTaxes(invoiceLine, invoiceLineTaxList);
      }
    }

    FiscalPosition fiscalPosition = invoice.getFiscalPosition();

    if (fiscalPosition == null || !fiscalPosition.getCustomerSpecificNote()) {
      if (invoiceLines != null) {
        invoice.setSpecificNotes(
            invoiceLines.stream()
                .map(InvoiceLine::getTaxEquivSet)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(TaxEquiv::getSpecificNote)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("\n")));
      }
    } else {
      invoice.setSpecificNotes(invoice.getPartner().getSpecificTaxNote());
    }

    return finalizeInvoiceLineTaxes(invoiceLineTaxList);
  }

  protected void createInvoiceLineTaxes(
      InvoiceLine invoiceLine, List<InvoiceLineTax> invoiceLineTaxList) throws AxelorException {
    Set<TaxLine> taxLineSet = invoiceLine.getTaxLineSet();
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    int vatSystem = 0;

    if (CollectionUtils.isEmpty(taxLineSet)) {
      return;
    }

    for (TaxLine taxLine : taxLineSet) {
      if (taxLine.getValue().signum() != 0) {
        vatSystem =
            taxAccountToolService.calculateVatSystem(
                invoice.getPartner(),
                invoice.getCompany(),
                invoiceLine.getAccount(),
                isPurchase,
                !isPurchase);
      }
      createInvoiceLineTax(invoiceLine, taxLine, vatSystem, invoiceLineTaxList);
    }

    LocalDate todayDate =
        appBaseService.getTodayDate(
            Optional.ofNullable(invoiceLine.getInvoice()).map(Invoice::getCompany).orElse(null));

    Set<TaxEquiv> taxEquivSet = invoiceLine.getTaxEquivSet();

    if (CollectionUtils.isEmpty(taxEquivSet)) {
      return;
    }

    for (TaxEquiv taxEquiv : taxEquivSet) {
      if (taxEquiv != null && taxEquiv.getReverseCharge()) {
        // We get active tax line if it exist, else we fetch one in taxLine list of reverse
        // charge
        // tax
        TaxLine taxLineRC =
            Optional.ofNullable(taxEquiv.getReverseChargeTax())
                .map(Tax::getActiveTaxLine)
                .orElse(taxService.getTaxLine(taxEquiv.getReverseChargeTax(), todayDate));

        if (taxLineRC != null) {
          createInvoiceLineTaxRc(invoiceLine, taxLineRC, vatSystem, invoiceLineTaxList);
        }
      }
    }
  }

  protected void createInvoiceLineTax(
      InvoiceLine invoiceLine,
      TaxLine taxLine,
      int vatSystem,
      List<InvoiceLineTax> invoiceLineTaxList) {
    LOG.debug("Tax {}", taxLine);

    InvoiceLineTax invoiceLineTax = createInvoiceLineTax(invoiceLine, taxLine, vatSystem);
    invoiceLineTax.setReverseCharged(false);
    invoiceLineTax.setInvoiceLine(invoiceLine);
    invoiceLineTaxList.add(invoiceLineTax);
  }

  protected void createInvoiceLineTaxRc(
      InvoiceLine invoiceLine,
      TaxLine taxLineRC,
      int vatSystem,
      List<InvoiceLineTax> invoiceLineTaxList) {
    InvoiceLineTax invoiceLineTaxRC = createInvoiceLineTax(invoiceLine, taxLineRC, vatSystem);
    invoiceLineTaxRC.setReverseCharged(true);
    invoiceLineTaxRC.setInvoiceLine(invoiceLine);
    invoiceLineTaxList.add(invoiceLineTaxRC);
  }

  protected InvoiceLineTax createInvoiceLineTax(
      InvoiceLine invoiceLine, TaxLine taxLine, int vatSystem) {
    InvoiceLineTax invoiceLineTax = new InvoiceLineTax();
    invoiceLineTax.setInvoice(invoice);

    // Dans la devise de la facture
    invoiceLineTax.setExTaxBase(invoiceLine.getExTaxTotal());
    // Dans la devise de la comptabilité du tiers
    invoiceLineTax.setCompanyExTaxBase(
        currencyScaleService.getCompanyScaledValue(invoice, invoiceLine.getCompanyExTaxTotal()));

    if (!invoiceLine.getFixedAssets()) {
      invoiceLineTax.setSubTotalExcludingFixedAssets(
          currencyScaleService.getScaledValue(invoice, invoiceLine.getExTaxTotal()));
      invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
          currencyScaleService.getCompanyScaledValue(
              invoice,
              invoiceLineTax
                  .getCompanySubTotalExcludingFixedAssets()
                  .add(invoiceLine.getCompanyExTaxTotal())));
    }
    invoiceLineTax.setVatSystemSelect(vatSystem);
    invoiceLineTax.setTaxLine(taxLine);
    invoiceLineTax.setCoefficient(invoiceLine.getCoefficient());
    invoiceLineTax.setTaxType(
        Optional.ofNullable(taxLine.getTax()).map(Tax::getTaxType).orElse(null));
    return invoiceLineTax;
  }

  protected List<InvoiceLineTax> finalizeInvoiceLineTaxes(List<InvoiceLineTax> invoiceLineTaxList) {
    for (InvoiceLineTax invoiceLineTax : invoiceLineTaxList) {

      BigDecimal taxValue = invoiceLineTax.getTaxLine().getValue().divide(new BigDecimal(100));
      // Dans la devise de la facture
      BigDecimal exTaxBase =
          (invoiceLineTax.getReverseCharged())
              ? invoiceLineTax.getExTaxBase().negate()
              : invoiceLineTax.getExTaxBase();
      BigDecimal taxTotal =
          computeAmount(
              exTaxBase,
              taxValue,
              currencyScaleService.getScale(invoiceLineTax.getInvoice()),
              null);

      invoiceLineTax.setTaxTotal(taxTotal);
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
      invoiceLineTax.setCompanyInTaxTotal(
          invoiceLineTax.getCompanyExTaxBase().add(companyTaxTotal));

      BigDecimal subTotalExcludingFixedAssets =
          invoiceLineTax.getReverseCharged()
              ? invoiceLineTax.getSubTotalExcludingFixedAssets().negate()
              : invoiceLineTax.getSubTotalExcludingFixedAssets();
      invoiceLineTax.setSubTotalExcludingFixedAssets(
          computeAmount(
              subTotalExcludingFixedAssets,
              taxValue,
              currencyScaleService.getScale(invoiceLineTax.getInvoice()),
              null));

      invoiceLineTax.setSubTotalOfFixedAssets(
          currencyScaleService.getScaledValue(
              invoiceLineTax.getInvoice(),
              taxTotal.subtract(invoiceLineTax.getSubTotalExcludingFixedAssets())));

      BigDecimal companySubTotalExcludingFixedAssets =
          invoiceLineTax.getReverseCharged()
              ? invoiceLineTax.getCompanySubTotalExcludingFixedAssets().negate()
              : invoiceLineTax.getCompanySubTotalExcludingFixedAssets();
      invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
          computeAmount(
              companySubTotalExcludingFixedAssets,
              taxValue,
              currencyScaleService.getCompanyScale(invoiceLineTax.getInvoice()),
              null));
      invoiceLineTax.setCompanySubTotalOfFixedAssets(
          currencyScaleService.getCompanyScaledValue(
              invoiceLineTax.getInvoice(),
              companyTaxTotal.subtract(invoiceLineTax.getCompanySubTotalExcludingFixedAssets())));

      LOG.debug(
          "Tax line : Tax total => {}, Total W.T. => {}",
          invoiceLineTax.getTaxTotal(),
          invoiceLineTax.getInTaxTotal());
    }

    return invoiceLineTaxList;
  }

  class TaxLineByVatSystem {

    protected TaxLine taxline;
    protected int vatSystem;

    public TaxLineByVatSystem(TaxLine taxline, int vatSystem) {
      this.taxline = taxline;
      this.vatSystem = vatSystem;
    }

    public int hashCode() {
      return (int) (this.taxline.getId() * 10 + this.vatSystem);
    }

    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof TaxLineByVatSystem)) {
        return false;
      }
      TaxLineByVatSystem other = (TaxLineByVatSystem) o;
      return this.vatSystem == other.vatSystem && this.taxline.equals(other.taxline);
    }
  }
}
