package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.repo.AnalyticAxisRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AnalyticAxisControlServiceImpl implements AnalyticAxisControlService {

  protected AnalyticAxisRepository analyticAxisRepository;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public AnalyticAxisControlServiceImpl(
      AnalyticAxisRepository analyticAxisRepository,
      AnalyticMoveLineRepository analyticMoveLineRepository) {
    this.analyticAxisRepository = analyticAxisRepository;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
  }

  @Override
  public void controlUnicity(AnalyticAxis analyticAxis) throws AxelorException {
    Objects.requireNonNull(analyticAxis);

    StringBuilder query = new StringBuilder("self.code = :code");
    Map<String, Object> params = new HashMap<>();
    params.put("code", analyticAxis.getCode());

    if (analyticAxis.getId() != null) {
      query.append(" AND self.id != :analyticAxisId");
      params.put("analyticAxisId", analyticAxis.getId());
    }
    if (analyticAxis.getCompany() == null) {
      query.append(" AND self.company is null");
    } else {
      query.append(" AND self.company = :company");
      params.put("company", analyticAxis.getCompany());
    }
    if (analyticAxisRepository.all().filter(query.toString()).bind(params).count() > 0) {
      if (analyticAxis.getCompany() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
            I18n.get(AccountExceptionMessage.NOT_UNIQUE_CODE_ANALYTIC_AXIS_NULL_COMPANY));
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
          I18n.get(AccountExceptionMessage.NOT_UNIQUE_CODE_ANALYTIC_AXIS_WITH_COMPANY),
          analyticAxis.getCompany().getName());
    }
  }

  @Override
  public boolean isInAnalyticMoveLine(AnalyticAxis analyticAxis) {
    return analyticMoveLineRepository.all().filter("self.analyticAxis = ?", analyticAxis).count()
        > 0;
  }
}
