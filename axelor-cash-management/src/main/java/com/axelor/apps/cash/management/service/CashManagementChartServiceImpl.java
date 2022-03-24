package com.axelor.apps.cash.management.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.ForecastRecapLine;
import com.axelor.apps.cash.management.db.repo.ForecastRecapLineRepository;
import com.axelor.apps.cash.management.db.repo.ForecastRecapRepository;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.auth.db.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class CashManagementChartServiceImpl implements CashManagementChartService {

  protected ForecastRecapRepository forecastRepo;
  protected ForecastRecapLineRepository forecastLineRepo;

  @Inject
  public CashManagementChartServiceImpl(
      ForecastRecapRepository forecastRepo, ForecastRecapLineRepository forecastLineRepo) {
    this.forecastRepo = forecastRepo;
    this.forecastLineRepo = forecastLineRepo;
  }

  @Override
  public List<Map<String, Object>> getCashBalanceData(User user, BankDetails bankDetails) {
    List<Map<String, Object>> dataList = new ArrayList<>();
    ForecastRecap recap =
        forecastRepo
            .all()
            .filter(
                "self.isReport = true AND (self.userRecap = :user OR :user is null) AND (:bankDetails is null OR :bankDetails MEMBER OF self.bankDetailsSet)")
            .bind("user", user)
            .bind("bankDetails", bankDetails)
            .fetchOne();

    if (recap == null) {
      return dataList;
    }

    List<ForecastRecapLine> recapLineList =
        forecastLineRepo
            .all()
            .filter("self.estimatedDate is not null AND self.forecastRecap = ?1", recap)
            .fetch();

    LocalDate startDate = recap.getFromDate();
    LocalDate toDate = recap.getToDate();
    final String keyWeek = "week";
    final String keyBalance = "balance";

    int i = 1;

    Map<String, Object> firstEntry = new HashMap<>();
    firstEntry.put(keyWeek, i++);
    firstEntry.put(keyBalance, recap.getStartingBalance());
    dataList.add(firstEntry);

    LocalDate date = startDate;
    while (date.isBefore(toDate)) {
      Map<String, Object> entry = new HashMap<>();
      entry.put(keyWeek, i++);
      entry.put(keyBalance, getRecapLinesTotal(recap, recapLineList, startDate, date.plusWeeks(1)));
      dataList.add(entry);
      date = date.plusWeeks(1);
    }

    return dataList;
  }

  protected BigDecimal getRecapLinesTotal(
      ForecastRecap recap,
      List<ForecastRecapLine> recapLineList,
      LocalDate fromDate,
      LocalDate toDate) {
    return recapLineList.stream()
        .filter(line -> DateTool.isBetween(fromDate, toDate, line.getEstimatedDate()))
        .map(ForecastRecapLine::getAmount)
        .reduce(recap.getStartingBalance(), BigDecimal::add);
  }
}
