package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.IndexRevaluation;
import com.axelor.apps.contract.db.IndexValue;
import com.axelor.apps.contract.db.repo.IndexValueRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Comparator;

public class IndexRevaluationServiceImpl implements IndexRevaluationService {

  protected IndexValueRepository indexValueRepository;

  @Inject
  public IndexRevaluationServiceImpl(IndexValueRepository indexValueRepository) {
    this.indexValueRepository = indexValueRepository;
  }

  public IndexValue getIndexValue(IndexRevaluation indexRevaluation, LocalDate date)
      throws AxelorException {
    return indexRevaluation.getIndexValueList().stream()
        .filter(index -> index.getStartDate().isBefore(date) || index.getStartDate().isEqual(date))
        .max(Comparator.comparing(IndexValue::getStartDate))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get(ContractExceptionMessage.CONTRACT_INDEX_VALUE_NO_DATA),
                    IndexValue.class));
  }

  public IndexValue getMostRecentIndexValue(IndexRevaluation indexRevaluation)
      throws AxelorException {
    return indexRevaluation.getIndexValueList().stream()
        .max(Comparator.comparing(IndexValue::getStartDate))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get(ContractExceptionMessage.CONTRACT_INDEX_VALUE_NO_DATA),
                    IndexValue.class));
  }

  public IndexValue getLastYearIndexValue(IndexRevaluation index) throws AxelorException {
    IndexValue mostRecentIndexValue = getMostRecentIndexValue(index);
    return getIndexValue(index, mostRecentIndexValue.getStartDate().minusYears(1));
  }
}
