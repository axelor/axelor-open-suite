package com.axelor.apps.account.util.helpers;

import com.axelor.apps.account.util.helpers.models.ChartMonthTurnoverDto;
import com.axelor.apps.account.util.helpers.models.ChartYearTurnoverDto;
import com.axelor.db.JPA;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.Query;

public class AccountInvoiceWorkforceCostsHelper {

  private static final String SQL_GET_CONTRACTS =
      "SELECT employee ,monthly_global_cost, end_date, start_date FROM hr_employment_contract";

  /**
   * Calculates workforce cost for selected years, may forecast if 'isForecast' is true
   *
   * @param years Year set to get workforce cost for
   * @param isForecast If is true, then method returns forecasted workforce costs too
   * @return DTO's wheres turnover is calculated workforce cost
   */
  public static List<ChartYearTurnoverDto> getWorkforceCostByYears(
      Set<Integer> years, boolean isForecast) {
    List<ChartMonthTurnoverDto> monthTurnoverDtos = getWorkforceCostByMonths(years, isForecast);

    // Map to returning type
    return ChartYearTurnoverDto.mapMonthDtoToListToYearDto(monthTurnoverDtos);
  }

  /**
   * Calculates workforce cost by months for selected years, may forecast if 'isForecast' is true
   *
   * @param years Year set to get workforce cost for
   * @param isForecast If is true, then method returns forecasted workforce costs too
   * @return DTO's wheres turnover is calculated workforce cost
   */
  public static List<ChartMonthTurnoverDto> getWorkforceCostByMonths(
      Set<Integer> years, boolean isForecast) {
    List<Contract> contracts = getContractsData();

    List<ChartMonthTurnoverDto> result = new ArrayList<>();
    for (int year : years) {
      result.addAll(calculateWorkforceCostByMonths(contracts, year));
    }
    // Filter only these data, which date is before current date

    if (!isForecast) {
      LocalDateTime currentDate = LocalDateTime.now();

      result =
          result.stream()
              .filter(
                  chartMonthTurnoverDto ->
                      chartMonthTurnoverDto.year != currentDate.getYear()
                          || chartMonthTurnoverDto.monthNo <= currentDate.getMonthValue())
              .collect(Collectors.toList());
    }
    return result;
  }

  /**
   * @param contracts Contracts to get data and calculate projects from it
   * @param year Year of work costs calculation
   * @return Month dto object wheres amoalount is workforce cost, year and month are workforce cost
   *     year and month
   */
  private static List<ChartMonthTurnoverDto> calculateWorkforceCostByMonths(
      List<Contract> contracts, int year) {

    List<ChartMonthTurnoverDto> result = new ArrayList<>();

    for (int monthCountNumber = 1; monthCountNumber <= 12; monthCountNumber++) {
      try {

        DateFormat format = new SimpleDateFormat("yyyy-MM-d");
        // Get date to compare with
        String dateString =
            year + "-" + String.format("%02d", monthCountNumber) + "-" + String.format("%02d", 1);
        Date dateForCheck = format.parse(dateString);
        Date dateWithLastDayOfMonth = getDateWithLastDayOfTheMonth(format.parse(dateString));

        // filter contracts by it's status in selected date
        List<Contract> filteredContracts =
            contracts.stream()
                .filter(
                    contract ->
                        contract.isContractActiveInDates(dateWithLastDayOfMonth, dateForCheck))
                .collect(Collectors.toList());

        // Create dto with calculated amount
        ChartMonthTurnoverDto dto =
            new ChartMonthTurnoverDto(
                filteredContracts.stream()
                    .map(c -> c.monthlyGlobalCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add),
                year,
                monthCountNumber);
        result.add(dto);

      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

  private static Date getDateWithLastDayOfTheMonth(Date date) {

    Calendar c = Calendar.getInstance();
    c.setTime(date);
    date.setDate(c.getActualMaximum(Calendar.DAY_OF_MONTH));

    return date;
  }
  /**
   * Get contracts data
   *
   * @return List of contracts
   */
  private static List<Contract> getContractsData() {
    List<Contract> contracts = new ArrayList<>();

    Query nativeQuery = JPA.em().createNativeQuery(SQL_GET_CONTRACTS);

    // Execute query and get data
    List<Object[]> resultList = nativeQuery.getResultList();

    // Fill yearDataMap with data
    // For each
    for (Object[] record : resultList) {
      // Create DTO object out of record data (

      try {

        // Create contract object
        Contract contract = Contract.noDatesContract(record[0].toString(), record[1].toString());
        DateFormat format = new SimpleDateFormat("yyyy-MM-d");
        // If end date is not null
        if (record[2] != null) {
          Date endDate = format.parse(record[2].toString());
          contract.setEndDate(endDate);
        }
        if (record[3] != null) {
          Date startDate = format.parse(record[3].toString());
          contract.setStartDate(startDate);
        }

        contracts.add(contract);

      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    return contracts;
  }

  static class Contract {
    public int employee;
    public BigDecimal monthlyGlobalCost;
    public Date endDate = null;
    public Date startDate = null;

    public Contract(int employee, BigDecimal monthlyGlobalCost) {
      this.employee = employee;
      this.monthlyGlobalCost = monthlyGlobalCost;
    }

    // Generates new contract without start_date and end_date
    public static Contract noDatesContract(String employee, String monthly_global_cost) {
      return new Contract(Integer.parseInt(employee), new BigDecimal(monthly_global_cost));
    }

    public void setStartDate(Date start_date) {
      this.startDate = start_date;
    }

    public void setEndDate(Date end_date) {
      this.endDate = end_date;
    }

    // Date dateForStartDateCheck == 01, Date dateForEndDateCheck == 31
    public boolean isContractActiveInDates(Date startDateBeforeDate, Date endDateAfterDate) {
      // 00 startDate and endDate is not set
      if (startDate == null && endDate == null) {
        return true;
      }
      // 10 startDate <= dateForStartDateCheck && endDate not set
      if (startDate != null && endDate == null) {
        return startDate.equals(startDateBeforeDate) || startDate.before(startDateBeforeDate);
      }
      // 01 -- startDate not set && endDate >= dateForEndDateCheck
      if (startDate == null && endDate != null) {
        return endDate.equals(endDateAfterDate) || endDate.after(endDateAfterDate);
      }
      // 11 startDate <= dateForStartDateCheck && endDate >= dateForEndDateCheck
      if (startDate != null && endDate != null) {
        return (startDate.equals(startDateBeforeDate) || startDate.before(startDateBeforeDate))
            && (endDate.equals(endDateAfterDate) || endDate.after(endDateAfterDate));
      }
      return false;
    }

    public String toString() {
      return "employee: "
          + employee
          + ", cost: "
          + monthlyGlobalCost.toString()
          + ", end_date: "
          + endDate
          + ", start_date: "
          + startDate;
    }
  }
}
