package com.axelor.apps.businessproject.service.invoice.breakdown;

import java.math.BigDecimal;

/**
 * Object representing a single line in the invoice breakdown.
 *
 * <p>Every line is one of three types:
 *
 * <p>- Regular: A normal line with sequence, description, quantity, unit, price, amount
 *
 * <p>- Total: A bold summary line closing a section. No sequence, no quantity, no price
 *
 * <p>- Spacing: An empty visual separator between sections. Carries no data
 *
 * <p>Every line also carries its SectionType so the print service knows where it belongs without
 * any implicit ordering assumptions.
 */
public class BreakdownDisplayLine {

  /** Enum representing the different line types */
  public enum LineType {
    REGULAR,
    TOTAL,
    SPACING
  }

  /** Enum to represent the different invoice breakdown sections */
  public enum SectionType {
    TIMESHEET,
    EXTRA_CHARGE,
    NIGHT_SHIFT,
    MATERIAL, // Expenses. Includes all lines generated from Expenses
    EXTRA_EXPENSE,
    EXPENSE,
    TOTALS
  }

  private final Integer sequence;
  private final String description;
  private final BigDecimal quantity;
  private final String unit;
  private final BigDecimal price;
  private final BigDecimal amount;
  private final String billingDetails;
  private final String sourceDetails;
  private final LineType lineType;
  private final SectionType sectionType;

  private BreakdownDisplayLine(
      Integer sequence,
      String description,
      BigDecimal quantity,
      String unit,
      BigDecimal price,
      BigDecimal amount,
      String billingDetails,
      String sourceDetails,
      LineType lineType,
      SectionType sectionType) {
    this.sequence = sequence;
    this.description = description != null ? description : "";
    this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
    this.unit = unit != null ? unit : "";
    this.price = price != null ? price : BigDecimal.ZERO;
    this.amount = amount != null ? amount : BigDecimal.ZERO;
    this.billingDetails = billingDetails != null ? billingDetails : "";
    this.sourceDetails = sourceDetails != null ? sourceDetails : "";
    this.lineType = lineType != null ? lineType : LineType.REGULAR;
    this.sectionType = sectionType;
  }

  /** Create a regular display line */
  public static BreakdownDisplayLine regular(
      Integer sequence,
      String description,
      BigDecimal quantity,
      String unit,
      BigDecimal price,
      BigDecimal amount,
      String billingDetails,
      String sourceDetails,
      SectionType sectionType) {

    return new BreakdownDisplayLine(
        sequence,
        description,
        quantity,
        unit,
        price,
        amount,
        billingDetails,
        sourceDetails,
        LineType.REGULAR,
        sectionType);
  }

  /** Create a total display line */
  public static BreakdownDisplayLine total(
      String description, BigDecimal amount, SectionType sectionType) {
    return new BreakdownDisplayLine(
        null, description, null, "", null, amount, "", "", LineType.TOTAL, sectionType);
  }

  /** Create a space display line */
  public static BreakdownDisplayLine spacing(SectionType sectionType) {
    return new BreakdownDisplayLine(
        null, "", null, "", null, null, "", "", LineType.SPACING, sectionType);
  }

  public SectionType getSectionType() {
    return sectionType;
  }

  public LineType getLineType() {
    return lineType;
  }

  public String getSourceDetails() {
    return sourceDetails;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getBillingDetails() {
    return billingDetails;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public String getUnit() {
    return unit;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public String getDescription() {
    return description;
  }

  public Integer getSequence() {
    return sequence;
  }
}
