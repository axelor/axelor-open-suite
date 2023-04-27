package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
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
  protected AppBaseService appBaseService;

  @Inject
  public IndexRevaluationServiceImpl(
      IndexValueRepository indexValueRepository, AppBaseService appBaseService) {
    this.indexValueRepository = indexValueRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  public IndexValue getIndexValue(IndexRevaluation indexRevaluation, LocalDate date)
      throws AxelorException {
    return indexRevaluation.getIndexValueList().stream()
        .filter(
            index ->
                (date.isAfter(index.getStartDate()) && date.isBefore(index.getEndDate()))
                    || index.getStartDate().isEqual(date)
                    || (index.getEndDate() == null && date.isAfter(index.getStartDate())))
        .max(Comparator.comparing(IndexValue::getStartDate))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get(ContractExceptionMessage.CONTRACT_INDEX_VALUE_NO_DATA),
                    IndexValue.class));
  }

  @Override
  public IndexValue getMostRecentIndexValue(IndexRevaluation indexRevaluation)
      throws AxelorException {
    return getIndexValue(indexRevaluation, appBaseService.getTodayDate(null));
  }

  @Override
  public IndexValue getLastYearIndexValue(IndexRevaluation index) throws AxelorException {
    IndexValue mostRecentIndexValue = getMostRecentIndexValue(index);
    return getIndexValue(index, mostRecentIndexValue.getStartDate().minusYears(1));
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
