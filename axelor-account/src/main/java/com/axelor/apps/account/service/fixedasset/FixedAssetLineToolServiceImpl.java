package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.tool.date.DateTool;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FixedAssetLineToolServiceImpl implements FixedAssetLineToolService {

  @Override
  public LinkedHashMap<LocalDate, List<FixedAssetLine>> groupAndSortByDateFixedAssetLine(
      FixedAsset fixedAsset) {
    Objects.requireNonNull(fixedAsset);

    // Preparation of data needed for computation
    List<FixedAssetLine> allFixedAssetLineList = new ArrayList<>();
    // This method will only compute line that are not realized.
    allFixedAssetLineList.addAll(
        fixedAsset.getFiscalFixedAssetLineList().stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .collect(Collectors.toList()));
    allFixedAssetLineList.addAll(
        fixedAsset.getFixedAssetLineList().stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .collect(Collectors.toList()));

    allFixedAssetLineList.sort(Comparator.comparing(FixedAssetLine::getDepreciationDate));

    return groupByPeriodicityInMonth(allFixedAssetLineList, fixedAsset.getPeriodicityInMonth());
  }

  protected LinkedHashMap<LocalDate, List<FixedAssetLine>> groupByPeriodicityInMonth(
      List<FixedAssetLine> fixedAssetLineList, int periodicityInMonth) {
    LinkedHashMap<LocalDate, List<FixedAssetLine>> returnedHashMap = new LinkedHashMap<>();
    if (fixedAssetLineList.isEmpty()) {
      return returnedHashMap;
    }
    // depreciation date is required and fixed asset line list is not empty, so we can get()
    LocalDate startDate =
        fixedAssetLineList.stream().map(FixedAssetLine::getDepreciationDate).findFirst().get();
    LocalDate endDate = startDate;

    while (!fixedAssetLineList.isEmpty()) {
      LocalDate currentStartDate = startDate;
      LocalDate currentEndDate = endDate;
      List<FixedAssetLine> subFixedAssetLineList =
          fixedAssetLineList.stream()
              .filter(
                  fixedAssetLine ->
                      DateTool.isBetween(
                          currentStartDate, currentEndDate, fixedAssetLine.getDepreciationDate()))
              .collect(Collectors.toList());
      if (subFixedAssetLineList.isEmpty()) {
        break;
      }
      fixedAssetLineList.removeAll(subFixedAssetLineList);
      // depreciation date is required and sub fixed asset line list is not empty, so we can get()
      LocalDate maxDateInSubList =
          subFixedAssetLineList.stream()
              .map(FixedAssetLine::getDepreciationDate)
              .max(Comparator.naturalOrder())
              .get();
      returnedHashMap.put(maxDateInSubList, subFixedAssetLineList);
      startDate = endDate.plusDays(1);
      endDate = startDate.plusMonths(periodicityInMonth).minusDays(1);
    }
    return returnedHashMap;
  }
}
