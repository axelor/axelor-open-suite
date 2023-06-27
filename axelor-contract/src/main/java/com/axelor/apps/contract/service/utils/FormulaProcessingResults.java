package com.axelor.apps.contract.service.utils;

import java.math.BigDecimal;

public class FormulaProcessingResults {
  private final BigDecimal ind1i;
  private final BigDecimal ind2i;
  private final BigDecimal ind1f;
  private final BigDecimal ind2f;
  private final BigDecimal ind1nm1;
  private final BigDecimal ind2nm1;
  private final BigDecimal initialUnitPrice;
  private final BigDecimal price;

  public FormulaProcessingResults(
      BigDecimal ind1i,
      BigDecimal ind2i,
      BigDecimal ind1f,
      BigDecimal ind2f,
      BigDecimal ind1nm1,
      BigDecimal ind2nm1,
      BigDecimal initialUnitPrice,
      BigDecimal price) {
    this.ind1i = ind1i;
    this.ind2i = ind2i;
    this.ind1f = ind1f;
    this.ind2f = ind2f;
    this.ind1nm1 = ind1nm1;
    this.ind2nm1 = ind2nm1;
    this.initialUnitPrice = initialUnitPrice;
    this.price = price;
  }

  public FormulaProcessingResults() {
    this.ind1i = null;
    this.ind2i = null;
    this.ind1f = null;
    this.ind2f = null;
    this.ind1nm1 = null;
    this.ind2nm1 = null;
    this.initialUnitPrice = null;
    this.price = null;
  }

  public BigDecimal getInd1i() {
    return ind1i;
  }

  public BigDecimal getInd2i() {
    return ind2i;
  }

  public BigDecimal getInd1f() {
    return ind1f;
  }

  public BigDecimal getInd2f() {
    return ind2f;
  }

  public BigDecimal getInd1nm1() {
    return ind1nm1;
  }

  public BigDecimal getInd2nm1() {
    return ind2nm1;
  }

  public BigDecimal getInitialUnitPrice() {
    return initialUnitPrice;
  }

  public BigDecimal getPrice() {
    return price;
  }
}
