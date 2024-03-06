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
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxInvoiceLine extends TaxGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected CurrencyScaleService currencyScaleService;

  public TaxInvoiceLine(Invoice invoice, List<InvoiceLine> invoiceLines) {
    super(invoice, invoiceLines);

    this.currencyScaleService = Beans.get(CurrencyScaleService.class);
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

    Map<TaxLineByVatSystem, InvoiceLineTax> map = new HashMap<>();

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

    return finalizeInvoiceLineTaxes(map);
  }

  protected void createInvoiceLineTaxes(
      InvoiceLine invoiceLine, Map<TaxLineByVatSystem, InvoiceLineTax> map) throws AxelorException {
    TaxLine taxLine = invoiceLine.getTaxLine();
    TaxEquiv taxEquiv = invoiceLine.getTaxEquiv();
    TaxLine taxLineRC = null;

    if (taxEquiv != null && taxEquiv.getReverseCharge()) {
      // We get active tax line if it exist, else we fetch one in taxLine list of reverse charge tax
      taxLineRC =
          Optional.ofNullable(taxEquiv.getReverseChargeTax())
              .map(Tax::getActiveTaxLine)
              .orElse(
                  Beans.get(TaxService.class)
                      .getTaxLine(
                          taxEquiv.getReverseChargeTax(),
                          Beans.get(AppBaseService.class)
                              .getTodayDate(
                                  Optional.ofNullable(invoiceLine.getInvoice())
                                      .map(Invoice::getCompany)
                                      .orElse(null))));
    }

    int vatSystem = 0;

    if (taxLine != null) {
      if (taxLine.getValue().signum() != 0) {
        vatSystem =
            Beans.get(TaxAccountToolService.class)
                .calculateVatSystem(
                    invoice.getPartner(),
                    invoice.getCompany(),
                    invoiceLine.getAccount(),
                    (invoice.getOperationTypeSelect()
                            == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                        || invoice.getOperationTypeSelect()
                            == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND),
                    (invoice.getOperationTypeSelect()
                            == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
                        || invoice.getOperationTypeSelect()
                            == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND));
      }
      createOrUpdateInvoiceLineTax(invoiceLine, taxLine, vatSystem, map);
    }
    if (taxLineRC != null) {
      createOrUpdateInvoiceLineTaxRc(invoiceLine, taxLineRC, taxEquiv, vatSystem, map);
    }
  }

  protected void createOrUpdateInvoiceLineTax(
      InvoiceLine invoiceLine,
      TaxLine taxLine,
      int vatSystem,
      Map<TaxLineByVatSystem, InvoiceLineTax> map)
      throws AxelorException {
    LOG.debug("Tax {}", taxLine);

    TaxLineByVatSystem taxLineByVatSystem = new TaxLineByVatSystem(taxLine, vatSystem);
    InvoiceLineTax invoiceLineTax = map.get(taxLineByVatSystem);
    if (invoiceLineTax != null) {
      updateInvoiceLineTax(invoiceLine, invoiceLineTax, vatSystem);
      invoiceLineTax.setReverseCharged(false);
    } else {
      invoiceLineTax = createInvoiceLineTax(invoiceLine, taxLine, vatSystem);
      invoiceLineTax.setReverseCharged(false);
      map.put(taxLineByVatSystem, invoiceLineTax);
    }
  }

  protected void createOrUpdateInvoiceLineTaxRc(
      InvoiceLine invoiceLine,
      TaxLine taxLineRC,
      TaxEquiv taxEquiv,
      int vatSystem,
      Map<TaxLineByVatSystem, InvoiceLineTax> map) {
    TaxLineByVatSystem taxLineByVatSystem = new TaxLineByVatSystem(taxLineRC, vatSystem);
    if (map.containsKey(taxLineByVatSystem)) {
      TaxLineByVatSystem taxLineByVatSystemEquiv =
          new TaxLineByVatSystem(taxEquiv.getReverseChargeTax().getActiveTaxLine(), vatSystem);
      InvoiceLineTax invoiceLineTaxRC = map.get(taxLineByVatSystemEquiv);
      updateInvoiceLineTax(invoiceLine, invoiceLineTaxRC, vatSystem);
      invoiceLineTaxRC.setReverseCharged(true);
    } else {
      InvoiceLineTax invoiceLineTaxRC = createInvoiceLineTax(invoiceLine, taxLineRC, vatSystem);
      invoiceLineTaxRC.setReverseCharged(true);
      map.put(taxLineByVatSystem, invoiceLineTaxRC);
    }
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

    if (!invoiceLine.getFixedAssets()) {
      invoiceLineTax.setSubTotalExcludingFixedAssets(
          currencyScaleService.getScaledValue(
              invoiceLine,
              invoiceLineTax.getSubTotalExcludingFixedAssets().add(invoiceLine.getExTaxTotal())));
      invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
          currencyScaleService.getCompanyScaledValue(
              invoiceLine,
              invoiceLineTax
                  .getCompanySubTotalExcludingFixedAssets()
                  .add(invoiceLine.getCompanyExTaxTotal())));
    }
    invoiceLineTax.setVatSystemSelect(vatSystem);
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
    return invoiceLineTax;
  }

  protected List<InvoiceLineTax> finalizeInvoiceLineTaxes(
      Map<TaxLineByVatSystem, InvoiceLineTax> map) {

    List<InvoiceLineTax> invoiceLineTaxList = new ArrayList<>();

    for (InvoiceLineTax invoiceLineTax : map.values()) {

      BigDecimal taxValue = invoiceLineTax.getTaxLine().getValue().divide(new BigDecimal(100));
      // Dans la devise de la facture
      BigDecimal exTaxBase =
          (invoiceLineTax.getReverseCharged())
              ? invoiceLineTax.getExTaxBase().negate()
              : invoiceLineTax.getExTaxBase();
      BigDecimal taxTotal =
          computeAmount(
              exTaxBase, taxValue, currencyScaleService.getScale(invoiceLineTax.getInvoice()));

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
              currencyScaleService.getCompanyScale(invoiceLineTax.getInvoice()));

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
              currencyScaleService.getScale(invoiceLineTax.getInvoice())));

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
              currencyScaleService.getCompanyScale(invoiceLineTax.getInvoice())));
      invoiceLineTax.setCompanySubTotalOfFixedAssets(
          currencyScaleService.getCompanyScaledValue(
              invoiceLineTax.getInvoice(),
              companyTaxTotal.subtract(invoiceLineTax.getCompanySubTotalExcludingFixedAssets())));
      invoiceLineTaxList.add(invoiceLineTax);

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
