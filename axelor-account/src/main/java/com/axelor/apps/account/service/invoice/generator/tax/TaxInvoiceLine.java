/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;
import com.google.common.base.Joiner;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxInvoiceLine extends TaxGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public TaxInvoiceLine(Invoice invoice, List<InvoiceLine> invoiceLines) {

    super(invoice, invoiceLines);
  }

  /**
   * Créer les lignes de TVA de la facure. La création des lignes de TVA se basent sur les lignes de
   * factures
   *
   * @param invoice La facture.
   * @param invoiceLines Les lignes de facture.
   * @return La liste des lignes de TVA de la facture.
   */
  @Override
  public List<InvoiceLineTax> creates() {

    List<InvoiceLineTax> invoiceLineTaxList = new ArrayList<InvoiceLineTax>();
    Map<TaxLine, InvoiceLineTax> map = new HashMap<TaxLine, InvoiceLineTax>();
    Set<String> specificNotes = new HashSet<String>();

    boolean customerSpecificNote = false;

    if (invoice.getPartner().getFiscalPosition() != null) {
      customerSpecificNote = invoice.getPartner().getFiscalPosition().getCustomerSpecificNote();
    }

    if (invoiceLines != null && !invoiceLines.isEmpty()) {

      LOG.debug("Création des lignes de tva pour les lignes de factures.");

      for (InvoiceLine invoiceLine : invoiceLines) {

        TaxLine taxLine = invoiceLine.getTaxLine();
        TaxEquiv taxEquiv = invoiceLine.getTaxEquiv();
        TaxLine taxLineRC =
            (taxEquiv != null
                    && taxEquiv.getReverseCharge()
                    && taxEquiv.getReverseChargeTax() != null)
                ? taxEquiv.getReverseChargeTax().getActiveTaxLine()
                : null;

        if (taxLine != null) {
          LOG.debug("TVA {}", taxLine);
          InvoiceLineTax invoiceLineTax = map.get(taxLine);
          if (invoiceLineTax != null) {

            // Dans la devise de la facture
            invoiceLineTax.setExTaxBase(
                invoiceLineTax.getExTaxBase().add(invoiceLine.getExTaxTotal()));
            // Dans la devise de la société
            invoiceLineTax.setCompanyExTaxBase(
                invoiceLineTax
                    .getCompanyExTaxBase()
                    .add(invoiceLine.getCompanyExTaxTotal())
                    .setScale(2, RoundingMode.HALF_UP));

            invoiceLineTax.setReverseCharged(false);
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
          } else {
            invoiceLineTax = new InvoiceLineTax();
            invoiceLineTax.setInvoice(invoice);

            // Dans la devise de la facture
            invoiceLineTax.setExTaxBase(invoiceLine.getExTaxTotal());
            // Dans la devise de la comptabilité du tiers
            invoiceLineTax.setCompanyExTaxBase(
                invoiceLine.getCompanyExTaxTotal().setScale(2, RoundingMode.HALF_UP));

            invoiceLineTax.setReverseCharged(false);

            if (!invoiceLine.getFixedAssets()) {
              invoiceLineTax.setSubTotalExcludingFixedAssets(
                  invoiceLine.getCompanyExTaxTotal().setScale(2, RoundingMode.HALF_UP));
              invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
                  invoiceLineTax
                      .getCompanySubTotalExcludingFixedAssets()
                      .add(invoiceLine.getCompanyExTaxTotal())
                      .setScale(2, RoundingMode.HALF_UP));
            }

            invoiceLineTax.setTaxLine(taxLine);
            map.put(taxLine, invoiceLineTax);
          }
        }

        if (taxLineRC != null) {
          if (map.containsKey(taxLineRC)) {
            InvoiceLineTax invoiceLineTaxRC =
                map.get(taxEquiv.getReverseChargeTax().getActiveTaxLine());

            // Dans la devise de la facture
            invoiceLineTaxRC.setExTaxBase(
                invoiceLineTaxRC.getExTaxBase().add(invoiceLine.getExTaxTotal()));
            // Dans la devise de la comptabilité du tiers
            invoiceLineTaxRC.setCompanyExTaxBase(
                invoiceLineTaxRC
                    .getCompanyExTaxBase()
                    .add(invoiceLine.getCompanyExTaxTotal())
                    .setScale(2, RoundingMode.HALF_UP));

            invoiceLineTaxRC.setReverseCharged(true);
          } else {
            InvoiceLineTax invoiceLineTaxRC = new InvoiceLineTax();
            invoiceLineTaxRC.setInvoice(invoice);

            // Dans la devise de la facture
            invoiceLineTaxRC.setExTaxBase(invoiceLine.getExTaxTotal());
            // Dans la devise de la comptabilité du tiers
            invoiceLineTaxRC.setCompanyExTaxBase(
                invoiceLine.getCompanyExTaxTotal().setScale(2, RoundingMode.HALF_UP));

            invoiceLineTaxRC.setReverseCharged(true);

            invoiceLineTaxRC.setTaxLine(taxLineRC);
            map.put(taxLineRC, invoiceLineTaxRC);
          }
        }

        if (!customerSpecificNote) {
          if (taxEquiv != null && taxEquiv.getSpecificNote() != null) {
            specificNotes.add(taxEquiv.getSpecificNote());
          }
        }
      }
    }

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

      invoiceLineTax.setSubTotalExcludingFixedAssets(
          computeAmount(invoiceLineTax.getSubTotalExcludingFixedAssets(), taxValue));
      invoiceLineTax.setSubTotalOfFixedAssets(
          taxTotal
              .subtract(invoiceLineTax.getSubTotalExcludingFixedAssets())
              .setScale(2, RoundingMode.HALF_UP));

      invoiceLineTax.setCompanySubTotalExcludingFixedAssets(
          computeAmount(invoiceLineTax.getCompanySubTotalExcludingFixedAssets(), taxValue));
      invoiceLineTax.setCompanySubTotalOfFixedAssets(
          companyTaxTotal
              .subtract(invoiceLineTax.getCompanySubTotalExcludingFixedAssets())
              .setScale(2, RoundingMode.HALF_UP));
      invoiceLineTaxList.add(invoiceLineTax);

      LOG.debug(
          "Ligne de TVA : Total TVA => {}, Total HT => {}",
          new Object[] {invoiceLineTax.getTaxTotal(), invoiceLineTax.getInTaxTotal()});
    }

    if (!customerSpecificNote) {
      invoice.setSpecificNotes(Joiner.on('\n').join(specificNotes));
    } else {
      invoice.setSpecificNotes(invoice.getPartner().getSpecificTaxNote());
    }

    return invoiceLineTaxList;
  }
}
