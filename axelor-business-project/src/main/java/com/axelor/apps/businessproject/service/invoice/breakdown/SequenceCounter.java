package com.axelor.apps.businessproject.service.invoice.breakdown;

/**
 * The Sequence counter passed through breakdown section processors.
 *
 * <p>Note: Only REGULAR lines should consume a sequence number. TOTAL and SPACING lines do not get
 * a sequence.
 */
public class SequenceCounter {

  private int value;

  public SequenceCounter() {
    this.value = 1;
  }

  public SequenceCounter(int start) {
    this.value = start;
  }

  /**
   * Returns the current value then increments. To be used when assigning a sequence number to a
   * line.
   */
  public int next() {
    return value++;
  }

  /**
   * Returns the current value without incrementing. To be used when there's need to read the
   * current position without consuming it.
   */
  public int current() {
    return value;
  }
}
