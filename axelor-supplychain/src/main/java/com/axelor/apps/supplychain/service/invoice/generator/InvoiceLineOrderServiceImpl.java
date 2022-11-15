package com.axelor.apps.supplychain.service.invoice.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class InvoiceLineOrderServiceImpl implements InvoiceLineOrderService {
  public InvoiceLineGenerator getInvoiceLineGeneratorWithComputedTaxPrice(
      Invoice invoice, Product invoicingProduct, BigDecimal lineAmountToInvoice, TaxLine taxLine) {
    BigDecimal lineAmountToInvoiceInclTax =
        (taxLine != null)
            ? lineAmountToInvoice.add(
                lineAmountToInvoice.multiply(
                    taxLine
                        .getValue()
                        .divide(
                            new BigDecimal(100),
                            AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                            RoundingMode.HALF_UP)))
            : lineAmountToInvoice;

    return new InvoiceLineGenerator(
        invoice,
        invoicingProduct,
        invoicingProduct.getName(),
        lineAmountToInvoice,
        lineAmountToInvoiceInclTax,
        invoice.getInAti() ? lineAmountToInvoiceInclTax : lineAmountToInvoice,
        invoicingProduct.getDescription(),
        BigDecimal.ONE,
        invoicingProduct.getUnit(),
        taxLine,
        InvoiceLineGenerator.DEFAULT_SEQUENCE,
        BigDecimal.ZERO,
        PriceListLineRepository.AMOUNT_TYPE_NONE,
        lineAmountToInvoice,
        null,
        false) {
      @Override
      public List<InvoiceLine> creates() throws AxelorException {

        InvoiceLine invoiceLine = this.createInvoiceLine();

        List<InvoiceLine> invoiceLines = new ArrayList<>();
        invoiceLines.add(invoiceLine);

        return invoiceLines;
      }
    };
  }
}
