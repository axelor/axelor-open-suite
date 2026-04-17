package com.axelor.apps.businessproject.service.invoice.breakdown.sectionprocessors;

import com.axelor.apps.businessproject.service.invoice.breakdown.BreakdownDisplayLine;
import com.axelor.apps.businessproject.service.invoice.breakdown.SequenceCounter;
import com.axelor.apps.businessproject.service.invoice.breakdown.classify.InvoiceLineClassification;
import java.util.List;

/**
 * Contract for all invoice breakdown section processors.
 *
 * <p>Adding a new section to the breakdown requires the following:
 *
 * <p>1. Create a class implementing this interface
 *
 * <p>2. Add a SectionType value for it representing where on the breakdown it should appear.
 *
 * <p>3. Register it in the orchestrator (InvoiceBreakdownDisplayService)
 */
public interface InvoiceBreakdownSectionProcessor {

  /**
   * Indicates if this processor has anything to render for the given classification. If false the
   * orchestrator skips this processor, and the section which it was designed for does not show on
   * the breakdown.
   */
  boolean supports(InvoiceLineClassification classification);

  /**
   * The section this processor belongs to. Attached onto every DisplayLine this processor produces
   * so the print service knows where each line belongs.
   */
  BreakdownDisplayLine.SectionType getSectionType();

  /**
   * Produce the display lines for this section. Implementations should:
   *
   * <p>- Call sequence.next() for every REGULAR line
   *
   * <p>- Not call sequence.next() for TOTAL or SPACING lines
   *
   * <p>- Always close the section with a TOTAL line if the section has a subtotal
   *
   * @param classification the classified invoice lines for the whole invoice
   * @param sequence the shared sequence counter. Advances across all sections
   * @return ordered list of display lines for this section
   */
  List<BreakdownDisplayLine> process(
      InvoiceLineClassification classification, SequenceCounter sequence);
}
