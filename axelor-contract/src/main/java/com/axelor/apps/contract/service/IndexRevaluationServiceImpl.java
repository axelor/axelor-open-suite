package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.IndexRevaluation;
import com.axelor.apps.contract.db.IndexValue;
import com.axelor.apps.contract.db.repo.IndexValueRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class IndexRevaluationServiceImpl implements IndexRevaluationService {

  protected IndexValueRepository indexValueRepository;

  @Inject
  public IndexRevaluationServiceImpl(IndexValueRepository indexValueRepository) {
    this.indexValueRepository = indexValueRepository;
  }

  @Override
  public IndexValue getIndexValue(IndexRevaluation indexRevaluation, LocalDate date)
      throws AxelorException {
    return indexRevaluation.getIndexValueList().stream()
        .filter(index -> indexDateFilter(index, date))
        .max(Comparator.comparing(IndexValue::getStartDate))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get(ContractExceptionMessage.CONTRACT_INDEX_VALUE_NO_DATA),
                    date.toString()));
  }

  protected boolean indexDateFilter(IndexValue indexValue, LocalDate date) {
    LocalDate startDate = indexValue.getStartDate();
    LocalDate endDate = indexValue.getEndDate();

    return startDate.isEqual(date)
        || date.isAfter(startDate)
        || (endDate != null
            && ((date.isAfter(startDate) && date.isBefore(endDate)) || endDate.isEqual(date)));
  }

  @Override
  public IndexValue getLastYearIndexValue(IndexRevaluation index, LocalDate date)
      throws AxelorException {
    return getIndexValue(index, date.minusYears(1));
  }

  @Transactional
  public void setIndexValueEndDate(IndexRevaluation indexRevaluation) {
    List<IndexValue> indexValueList = indexRevaluation.getIndexValueList();
    if (indexValueList != null) {
      if (indexValueList.size() < 2) {
        return;
      }

      List<IndexValue> sortedIndexValueList =
          indexValueList.stream()
              .sorted(Comparator.comparing(IndexValue::getStartDate).reversed())
              .collect(Collectors.toList());
      IndexValue mostRecentValue = sortedIndexValueList.get(0);
      IndexValue secondMostRecentValue = sortedIndexValueList.get(1);

      if (secondMostRecentValue != null && secondMostRecentValue.getEndDate() == null) {
        secondMostRecentValue.setEndDate(mostRecentValue.getStartDate());
      }
    }
  }
}
