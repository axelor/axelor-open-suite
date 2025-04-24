package com.axelor.apps.account.util.helpers.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecordDto {
  public BigDecimal cost = BigDecimal.ZERO;
  public BigDecimal revenue = BigDecimal.ZERO;
  public BigDecimal workforceCost = BigDecimal.ZERO;
  public Integer year;
  public Integer monthNo;

  public RecordDto(Integer year, Integer month) {
    this.monthNo = month;
    this.year = year;
  }

  public void setWorkforceCost(BigDecimal workforceCost) {
    this.workforceCost = workforceCost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }

  public void setRevenue(BigDecimal revenue) {
    this.revenue = revenue;
  }

  // Gets DTO with calculated Gross profit
  public ChartMonthTurnoverDto getProfitDTO() {
    return new ChartMonthTurnoverDto(revenue.subtract(cost), year, monthNo);
  }

  // Gets DTO with calculated Ebitda
  public ChartMonthTurnoverDto getEbitdaDTO() {
    return new ChartMonthTurnoverDto(revenue.subtract(cost).subtract(workforceCost), year, monthNo);
  }

  /**
   * Function which combines revenue, cost and workforce cost DTOs data into one RecordDto object
   *
   * @param revenueDTO List of revenue DTO
   * @param costsDTO List of costs DTO
   * @param workforceCostsDTO List of workforce costs DTO (May be null)
   * @return List of recordDTO objects
   */
  public static List<RecordDto> generateRecordDtoList(
      List<ChartMonthTurnoverDto> revenueDTO,
      List<ChartMonthTurnoverDto> costsDTO,
      List<ChartMonthTurnoverDto> workforceCostsDTO) {
    // Use hashmap to navigate, key is combination of year and month
    HashMap<String, RecordDto> dictionary = new HashMap<>();

    // Revenue
    for (ChartMonthTurnoverDto revenue : revenueDTO) {
      // Create record dto
      RecordDto recordDTO = new RecordDto(revenue.year, revenue.monthNo);
      // set revenue value
      recordDTO.setRevenue(revenue.turnOver);
      // put created record into hashmap
      dictionary.put(revenue.getDTOYearMonthCode(), recordDTO);
    }

    // Costs
    for (ChartMonthTurnoverDto cost : costsDTO) {
      // If record for that date is not found
      if (!dictionary.containsKey(cost.getDTOYearMonthCode())) {
        // Create new, set cost value, put into hashmap
        RecordDto recordDTO = new RecordDto(cost.year, cost.monthNo);
        recordDTO.setCost(cost.turnOver);
        dictionary.put(cost.getDTOYearMonthCode(), recordDTO);
        // If record for that date is found
      } else {
        // Get record, set cost value, replace with updated object
        RecordDto recordDTO = dictionary.get(cost.getDTOYearMonthCode());
        recordDTO.setCost(cost.turnOver);
        dictionary.replace(cost.getDTOYearMonthCode(), recordDTO);
      }
    }
    // If workforce costs are null then return values
    if (workforceCostsDTO == null) {
      return new ArrayList<>(dictionary.values());
    }

    // Workforce cost
    for (ChartMonthTurnoverDto workforceCost : workforceCostsDTO) {
      // If record for that date is not found
      if (!dictionary.containsKey(workforceCost.getDTOYearMonthCode())) {
        // Create record, set workforce cost value, put into hashmap
        RecordDto recordDTO = new RecordDto(workforceCost.year, workforceCost.monthNo);
        recordDTO.setWorkforceCost(workforceCost.turnOver);
        dictionary.put(workforceCost.getDTOYearMonthCode(), recordDTO);
        // If record for that date is found
      } else {
        // Get record, set workforce cost value, replace with updated object
        RecordDto recordDTO = dictionary.get(workforceCost.getDTOYearMonthCode());
        recordDTO.setWorkforceCost(workforceCost.turnOver);
        dictionary.replace(workforceCost.getDTOYearMonthCode(), recordDTO);
      }
    }

    // Return values
    return new ArrayList<>(dictionary.values());
  }
}
