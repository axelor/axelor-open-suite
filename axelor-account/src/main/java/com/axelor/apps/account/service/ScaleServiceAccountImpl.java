package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.service.ScaleServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ScaleServiceAccountImpl extends ScaleServiceImpl implements ScaleServiceAccount {

  private static final int DEFAULT_SCALE = AppBaseService.DEFAULT_NB_DECIMAL_DIGITS;
  private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

  @Override
  public BigDecimal getScaledValue(BigDecimal value) {
    return super.getScaledValue(value);
  }

  @Override
  public BigDecimal getScaledValue(BigDecimal value, int customizedScale) {
    return super.getScaledValue(value, customizedScale);
  }

  @Override
  public BigDecimal getScaledValue(Object object, BigDecimal amount, boolean isCompanyAmount) {
    return super.getScaledValue(amount, this.getScale(object, isCompanyAmount));
  }

  protected int getScale(Object object, boolean isCompanyAmount) {
    int scale = DEFAULT_SCALE;

    if (object.getClass().getSuperclass().equals(Move.class)) {
      scale = this.getMoveScale((Move) object, isCompanyAmount);
    } else if (object.getClass().getSuperclass().equals(MoveLine.class)) {
      scale = this.getMoveLineScale((MoveLine) object, isCompanyAmount);
    } else if (object.getClass().getSuperclass().equals(Invoice.class)) {
      scale = this.getInvoiceScale((Invoice) object, isCompanyAmount);
    }

    return scale;
  }

  @Override
  public BigDecimal getDivideScaledValue(
      Object object, BigDecimal amount, BigDecimal divisor, boolean isCompanyAmount) {
    return amount.divide(divisor, this.getScale(object, isCompanyAmount), DEFAULT_ROUNDING_MODE);
  }

  // Get scale from invoice
  protected int getInvoiceScale(Invoice invoice, boolean isCompanyAmount) {
    return isCompanyAmount
        ? this.getInvoiceCompanyScale(invoice)
        : this.getInvoiceCurrencyScale(invoice);
  }

  protected int getInvoiceCompanyScale(Invoice invoice) {
    return invoice.getCompany() != null && invoice.getCompany().getCurrency() != null
        ? invoice.getCompany().getCurrency().getNumberOfDecimals()
        : DEFAULT_SCALE;
  }

  protected int getInvoiceCurrencyScale(Invoice invoice) {
    return invoice.getCurrency() != null
        ? invoice.getCurrency().getNumberOfDecimals()
        : DEFAULT_SCALE;
  }

  // get scale from move
  protected int getMoveScale(Move move, boolean isCompanyAmount) {
    return isCompanyAmount ? this.getMoveCompanyScale(move) : this.getMoveCurrencyScale(move);
  }

  protected int getMoveLineScale(MoveLine moveLine, boolean isCompanyAmount) {
    int scale = DEFAULT_SCALE;

    if (moveLine.getMove() != null) {
      scale = this.getMoveScale(moveLine.getMove(), isCompanyAmount);
    }

    return scale;
  }

  protected int getMoveCompanyScale(Move move) {
    return move.getCompany() != null && move.getCompany().getCurrency() != null
        ? move.getCompany().getCurrency().getNumberOfDecimals()
        : DEFAULT_SCALE;
  }

  protected int getMoveCurrencyScale(Move move) {
    return move.getCurrency() != null ? move.getCurrency().getNumberOfDecimals() : DEFAULT_SCALE;
  }
}
