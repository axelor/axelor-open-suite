package com.axelor.apps.account.util.helpers.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartYearTurnoverDto {

  public Integer year;
  public BigDecimal turnOver;

  public ChartYearTurnoverDto(Integer year, BigDecimal turnOver) {
    this.year = year;
    this.turnOver = turnOver;
  }

  @Override
  public String toString() {
    return "DTO{" + "turnOver=" + turnOver + ", year=" + year + '}';
  }

  public static List<ChartYearTurnoverDto> mapMonthDtoToListToYearDto(
      List<ChartMonthTurnoverDto> monthTurnoverDtoList) {

    // Group dtos by year, then sum it up and collect it to the map, where Key is Year and value is
    // total turnover
    Map<Integer, BigDecimal> entities =
        monthTurnoverDtoList.stream()
            .collect(
                Collectors.groupingBy(
                    ChartMonthTurnoverDto::getYear,
                    Collectors.mapping(
                        ChartMonthTurnoverDto::getTurnOver,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

    // Map to returning type
    return entities.entrySet().stream()
        .map(entity -> new ChartYearTurnoverDto(entity.getKey(), entity.getValue()))
        .collect(Collectors.toList());
  }
}
