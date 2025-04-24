package com.axelor.apps.account.util.helpers;

import com.axelor.apps.account.util.helpers.models.AccountInvoiceChartConstants;
import com.axelor.apps.account.util.helpers.models.ChartMonthTurnoverDto;
import com.axelor.apps.account.util.helpers.models.ChartYearTurnoverDto;
import com.axelor.apps.account.util.helpers.models.RecordDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountInvoiceChartHelper {

  public static List<ChartYearTurnoverDto> getRevenueByYears(String companyCode) {
    return AccountInvoiceRevenueCostHelper.getRevenueByYears(companyCode);
  }

  public static List<ChartYearTurnoverDto> getCostByYears(String companyCode) {
    return AccountInvoiceRevenueCostHelper.getCostByYears(companyCode);
  }

  public static List<ChartMonthTurnoverDto> getRevenueByMonths(String companyCode) {
    return AccountInvoiceRevenueCostHelper.getRevenueByMonths(companyCode);
  }

  public static List<ChartMonthTurnoverDto> getCostByMonths(String companyCode) {
    return AccountInvoiceRevenueCostHelper.getCostByMonths(companyCode);
  }

  public static List<ChartYearTurnoverDto> getWorkforceCostByYears(
      Set<Integer> years, boolean isForecast) {
    return AccountInvoiceWorkforceCostsHelper.getWorkforceCostByYears(years, isForecast);
  }

  public static List<ChartMonthTurnoverDto> getWorkforceCostByMonths(
      Set<Integer> years, boolean isForecast) {
    return AccountInvoiceWorkforceCostsHelper.getWorkforceCostByMonths(years, isForecast);
  }

  public static List<ChartYearTurnoverDto> getProfitDTOByYear(String companyCode) {
    return ChartYearTurnoverDto.mapMonthDtoToListToYearDto(getProfitDTOByMonth(companyCode));
  }

  public static List<ChartYearTurnoverDto> getEBITDADTOByYear() {

    return ChartYearTurnoverDto.mapMonthDtoToListToYearDto(getEBITDADTOByMonth());
  }

  public static List<ChartMonthTurnoverDto> getProfitDTOByMonth(String companyCode) {
    return calculateProfitDTOByMonth(companyCode);
  }

  public static List<ChartMonthTurnoverDto> getEBITDADTOByMonth() {

    return calculateEBITDADTOByMonth();
  }

  /**
   * Calculate gross profit and get data in DTOs
   *
   * @param companyCode what category profit should be selected
   * @return List of dto with calculated profit
   */
  private static List<ChartMonthTurnoverDto> calculateProfitDTOByMonth(String companyCode) {
    // Get data
    List<ChartMonthTurnoverDto> costsDTO = AccountInvoiceChartHelper.getCostByMonths(companyCode);
    List<ChartMonthTurnoverDto> revenueDTO =
        AccountInvoiceChartHelper.getRevenueByMonths(companyCode);

    // Combine data
    List<RecordDto> combined = RecordDto.generateRecordDtoList(revenueDTO, costsDTO, null);

    return combined.stream().map(RecordDto::getProfitDTO).collect(Collectors.toList());
  }

  /**
   * Calculate EBITDA and get data in DTOs
   *
   * @return List of dto with calculated EBITDA in it
   */
  private static List<ChartMonthTurnoverDto> calculateEBITDADTOByMonth() {
    // Get data
    List<ChartMonthTurnoverDto> costsDTO =
        AccountInvoiceChartHelper.getCostByMonths(AccountInvoiceChartConstants.CATEGORY_CODE_ALL);
    List<ChartMonthTurnoverDto> revenueDTO =
        AccountInvoiceChartHelper.getRevenueByMonths(
            AccountInvoiceChartConstants.CATEGORY_CODE_ALL);

    // Get years for workforce cost calculations
    Set<Integer> yearSet = revenueDTO.stream().map(d -> d.year).collect(Collectors.toSet());

    // Calculate workforce cost
    List<ChartMonthTurnoverDto> workforceCostsDTO =
        AccountInvoiceChartHelper.getWorkforceCostByMonths(yearSet, false);

    // Combine everything
    List<RecordDto> combined =
        RecordDto.generateRecordDtoList(revenueDTO, costsDTO, workforceCostsDTO);

    // Returned mapped data
    return combined.stream().map(RecordDto::getEbitdaDTO).collect(Collectors.toList());
  }
}
