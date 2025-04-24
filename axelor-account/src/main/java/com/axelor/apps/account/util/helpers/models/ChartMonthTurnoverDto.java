package com.axelor.apps.account.util.helpers.models;

import java.math.BigDecimal;

public class ChartMonthTurnoverDto {

  public BigDecimal turnOver;
  public Integer year;
  public Integer monthNo;

  public ChartMonthTurnoverDto(BigDecimal turnOver, Integer year, Integer monthNo) {
    this.turnOver = turnOver;
    this.year = year;
    this.monthNo = monthNo;
  }

  // Generates DTO out of string
  public static ChartMonthTurnoverDto generateDTO(String turnOver, String year, String monthNo) {
    return new ChartMonthTurnoverDto(
        new BigDecimal(turnOver), Integer.valueOf(year), Integer.valueOf(monthNo));
  }

  public Integer getYear() {
    return year;
  }

  public BigDecimal getTurnOver() {
    return turnOver;
  }

  public Integer getMonthNo() {
    return monthNo;
  }

  // get date code
  public String getDTOYearMonthCode() {
    return year.toString() + "#" + monthNo.toString();
  }

  @Override
  public String toString() {
    return "DTO{" + "turnOver=" + turnOver + ", year=" + year + ", monthNo=" + monthNo + '}';
  }
}
