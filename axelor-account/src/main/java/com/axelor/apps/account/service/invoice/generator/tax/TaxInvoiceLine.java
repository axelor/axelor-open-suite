/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice.generator.tax;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.exception.AxelorException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxInvoiceLine extends TaxGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected TaxAccountToolService taxAccountToolService;

  @Inject
  public TaxInvoiceLine(
      Invoice invoice,
      List<InvoiceLine> invoiceLines,
      TaxAccountToolService taxAccountToolService) {

    super(invoice, invoiceLines);
    this.taxAccountToolService = taxAccountToolService;
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

    Map<List<Object>, InvoiceLineTax> map = new HashMap<>();

    if (invoiceLines != null && !invoiceLines.isEmpty()) {

      LOG.debug("Création des lignes de tva pour les lignes de factures.");

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
                .collect(Collectors.joining("\n")));
      }
    } else {
      invoice.setSpecificNotes(invoice.getPartner().getSpecificTaxNote());
    }

    return finalizeInvoiceLineTaxes(map);
  }

  protected void createInvoiceLineTaxes(
      InvoiceLine invoiceLine, Map<List<Object>, InvoiceLineTax> map) throws AxelorException {
    TaxLine taxLine = invoiceLine.getTaxLine();
    TaxEquiv taxEquiv = invoiceLine.getTaxEquiv();
    TaxLine taxLineRC =
        (taxEquiv != null && taxEquiv.getReverseCharge() && taxEquiv.getReverseChargeTax() != null)
            ? taxEquiv.getReverseChargeTax().getActiveTaxLine()
            : null;
    int vatSystem =
        taxAccountToolService.calculateVatSystem(
            invoice.getPartner(),
            invoice.getCompany(),
            invoiceLine.getAccount(),
            (invoice.getOperationTypeSelect()
                == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE),
            (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE));

    if (taxLine != null) {
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
      Map<List<Object>, InvoiceLineTax> map)
      throws AxelorException {
    LOG.debug("TVA {}", taxLine);

    List<Object> keys = new ArrayList<Object>();
    keys.add(taxLine);
    keys.add(vatSystem);
    InvoiceLineTax invoiceLineTax = map.get(keys);
    if (invoiceLineTax != null) {
      updateInvoiceLineTax(invoiceLine, invoiceLineTax, vatSystem);
      invoiceLineTax.setReverseCharged(false);
    } else {
      invoiceLineTax = createInvoiceLineTax(invoiceLine, taxLine, vatSystem);
      invoiceLineTax.setReverseCharged(false);
      map.put(keys, invoiceLineTax);
    }
  }

  protected void createOrUpdateInvoiceLineTaxRc(
      InvoiceLine invoiceLine,
      TaxLine taxLineRC,
      TaxEquiv taxEquiv,
      int vatSystem,
      Map<List<Object>, InvoiceLineTax> map)
      throws AxelorException {
    List<Object> keys = new ArrayList<Object>();
    keys.add(taxLineRC);
    keys.add(vatSystem);
    if (map.containsKey(keys)) {
      List<Object> keysEquiv = new ArrayList<Object>();
      keysEquiv.add(taxEquiv.getReverseChargeTax().getActiveTaxLine());
      keysEquiv.add(vatSystem);
      InvoiceLineTax invoiceLineTaxRC = map.get(keysEquiv);
      updateInvoiceLineTax(invoiceLine, invoiceLineTaxRC, vatSystem);
      invoiceLineTaxRC.setReverseCharged(true);
    } else {
      InvoiceLineTax invoiceLineTaxRC = createInvoiceLineTax(invoiceLine, taxLineRC, vatSystem);
      invoiceLineTaxRC.setReverseCharged(true);
      map.put(keys, invoiceLineTaxRC);
    }
  }

  protected void updateInvoiceLineTax(
      InvoiceLine invoiceLine, InvoiceLineTax invoiceLineTax, int vatSystem)
      throws AxelorException {

    // Dans la devise de la facture
    invoiceLineTax.setExTaxBase(invoiceLineTax.getExTaxBase().add(invoiceLine.getExTaxTotal()));
    // Dans la devise de la société
    invoiceLineTax.setCompanyExTaxBase(
        invoiceLineTax
            .getCompanyExTaxBase()
            .add(invoiceLine.getCompanyExTaxTotal())
            .setScale(2, RoundingMode.HALF_UP));

    if (!invoiceLine.getFixedAssets()) {
      invoiceLineTax.setSubTotalExcludingFixedAssets(
          invoiceLineTax
              .getSubTotalExcludingFixedAssets()
              .add(invoiceLine.getExTaxTotal())
              .setScale(2, RoundingMode.HALF_UP));
      invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
          invoiceLineTax
              .getCompanySubTotalExcludingFixedAssets()
              .add(invoiceLine.getCompanyExTaxTotal())
              .setScale(2, RoundingMode.HALF_UP));
    }
    invoiceLineTax.setVatSystemSelect(vatSystem);
  }

  protected InvoiceLineTax createInvoiceLineTax(
      InvoiceLine invoiceLine, TaxLine taxLine, int vatSystem) throws AxelorException {
    InvoiceLineTax invoiceLineTax = new InvoiceLineTax();
    invoiceLineTax.setInvoice(invoice);

    // Dans la devise de la facture
    invoiceLineTax.setExTaxBase(invoiceLine.getExTaxTotal());
    // Dans la devise de la comptabilité du tiers
    invoiceLineTax.setCompanyExTaxBase(
        invoiceLine.getCompanyExTaxTotal().setScale(2, RoundingMode.HALF_UP));

    if (!invoiceLine.getFixedAssets()) {
      invoiceLineTax.setSubTotalExcludingFixedAssets(
          invoiceLine.getExTaxTotal().setScale(2, RoundingMode.HALF_UP));
      invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
          invoiceLineTax
              .getCompanySubTotalExcludingFixedAssets()
              .add(invoiceLine.getCompanyExTaxTotal())
              .setScale(2, RoundingMode.HALF_UP));
    }
    invoiceLineTax.setVatSystemSelect(vatSystem);
    invoiceLineTax.setTaxLine(taxLine);
    return invoiceLineTax;
  }

  protected List<InvoiceLineTax> finalizeInvoiceLineTaxes(Map<List<Object>, InvoiceLineTax> map) {

    List<InvoiceLineTax> invoiceLineTaxList = new ArrayList<>();

    for (InvoiceLineTax invoiceLineTax : map.values()) {

      BigDecimal taxValue = invoiceLineTax.getTaxLine().getValue();
      // Dans la devise de la facture
      BigDecimal exTaxBase =
          (invoiceLineTax.getReverseCharged())
              ? invoiceLineTax.getExTaxBase().negate()
              : invoiceLineTax.getExTaxBase();
      BigDecimal taxTotal = computeAmount(exTaxBase, taxValue);

      invoiceLineTax.setTaxTotal(taxTotal);
      invoiceLineTax.setInTaxTotal(invoiceLineTax.getExTaxBase().add(taxTotal));

      // Dans la devise de la société
      BigDecimal companyExTaxBase =
          (invoiceLineTax.getReverseCharged())
              ? invoiceLineTax.getCompanyExTaxBase().negate()
              : invoiceLineTax.getCompanyExTaxBase();
      BigDecimal companyTaxTotal = computeAmount(companyExTaxBase, taxValue);

      invoiceLineTax.setCompanyTaxTotal(companyTaxTotal);
      invoiceLineTax.setCompanyInTaxTotal(
          invoiceLineTax.getCompanyExTaxBase().add(companyTaxTotal));

      BigDecimal subTotalExcludingFixedAssets =
          invoiceLineTax.getReverseCharged()
              ? invoiceLineTax.getSubTotalExcludingFixedAssets().negate()
              : invoiceLineTax.getSubTotalExcludingFixedAssets();
      invoiceLineTax.setSubTotalExcludingFixedAssets(
          computeAmount(subTotalExcludingFixedAssets, taxValue));

      invoiceLineTax.setSubTotalOfFixedAssets(
          taxTotal
              .subtract(invoiceLineTax.getSubTotalExcludingFixedAssets())
              .setScale(2, RoundingMode.HALF_UP));

      BigDecimal companySubTotalExcludingFixedAssets =
          invoiceLineTax.getReverseCharged()
              ? invoiceLineTax.getCompanySubTotalExcludingFixedAssets().negate()
              : invoiceLineTax.getCompanySubTotalExcludingFixedAssets();
      invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
          computeAmount(companySubTotalExcludingFixedAssets, taxValue));
      invoiceLineTax.setCompanySubTotalOfFixedAssets(
          companyTaxTotal
              .subtract(invoiceLineTax.getCompanySubTotalExcludingFixedAssets())
              .setScale(2, RoundingMode.HALF_UP));
      invoiceLineTaxList.add(invoiceLineTax);

      LOG.debug(
          "Ligne de TVA : Total TVA => {}, Total HT => {}",
          invoiceLineTax.getTaxTotal(),
          invoiceLineTax.getInTaxTotal());
    }

    return invoiceLineTaxList;
  }
}
