package com.axelor.apps.businessproject.service.invoice.breakdown.display;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.service.invoice.breakdown.BreakdownDisplayLine;
import com.axelor.apps.businessproject.service.invoice.breakdown.SequenceCounter;
import com.axelor.apps.businessproject.service.invoice.breakdown.classify.InvoiceLineClassification;
import com.axelor.apps.businessproject.service.invoice.breakdown.classify.InvoiceLineClassifier;
import com.axelor.apps.businessproject.service.invoice.breakdown.sectionprocessors.*;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceBreakdownDisplayServiceImpl implements InvoiceBreakdownDisplayService {

  private static final Logger log =
      LoggerFactory.getLogger(InvoiceBreakdownDisplayServiceImpl.class);
  private final InvoiceLineClassifier classifier;
  private final TotalsBreakdownSectionProcessor totalsProcessor;
  private final List<InvoiceBreakdownSectionProcessor> sectionProcessors;

  @Inject
  public InvoiceBreakdownDisplayServiceImpl(
      InvoiceLineClassifier classifier,
      ExpenseBreakdownSectionProcessor expenseProcessor,
      TotalsBreakdownSectionProcessor totalsProcessor) {
    this.classifier = classifier;
    this.totalsProcessor = totalsProcessor;

    // Order here defines order in the breakdown
    this.sectionProcessors = List.of(expenseProcessor);
  }

  @Override
  public List<BreakdownDisplayLine> generateBreakdownFromInvoice(Invoice invoice) {
    if (invoice == null || invoice.getInvoiceLineList() == null) {
      return Collections.emptyList();
    }

    InvoiceLineClassification classification = classifier.classify(invoice.getInvoiceLineList());
    if (classification == null) {
      return Collections.emptyList();
    }

    List<BreakdownDisplayLine> lines = new ArrayList<>();
    SequenceCounter sequence = new SequenceCounter();

    for (InvoiceBreakdownSectionProcessor processor : sectionProcessors) {
      if (processor.supports(classification)) {
        lines.addAll(processor.process(classification, sequence));
      }
    }

    lines.addAll(totalsProcessor.process(invoice, sequence));

    return lines;
  }
}
