package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class AccountConfigAnalyticServiceImpl implements AccountConfigAnalyticService {

  protected AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public AccountConfigAnalyticServiceImpl(AnalyticMoveLineRepository analyticMoveLineRepository) {
    this.analyticMoveLineRepository = analyticMoveLineRepository;
  }

  @Override
  public void checkChangesInAnalytic(
      List<AnalyticAxisByCompany> initialList, List<AnalyticAxisByCompany> modifiedList)
      throws AxelorException {
    if (checkChangesInAnalyticConfig(initialList, modifiedList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ACCOUNT_CONFIG_ANALYTIC_CHANGE_IN_CONFIG));
    }
  }

  public boolean checkChangesInAnalyticConfig(
      List<AnalyticAxisByCompany> initialList, List<AnalyticAxisByCompany> modifiedList)
      throws AxelorException {
    if (!CollectionUtils.isEmpty(initialList)) {
      List<AnalyticAxis> analyticAxisUsedList = getUsedAnalyticAxis(initialList);
      if (initialList.size() != modifiedList.size()
          && !CollectionUtils.isEmpty(analyticAxisUsedList)) {
        return true;
      } else if (axisChangedInConfig(initialList, modifiedList) != null
          && analyticAxisUsedList.contains(axisChangedInConfig(initialList, modifiedList))) {
        return true;
      } else if (orderChangedInConfig(initialList, modifiedList)
          && !CollectionUtils.isEmpty(analyticAxisUsedList)) {
        return true;
      }
    }
    return false;
  }

  public List<AnalyticAxis> getUsedAnalyticAxis(List<AnalyticAxisByCompany> initialList) {
    List<AnalyticAxis> analyticAxisList = new ArrayList<AnalyticAxis>();
    for (AnalyticAxisByCompany analyticAxisByCompany : initialList) {
      if (analyticAxisByCompany.getAnalyticAxis() != null
          && analyticMoveLineRepository
                  .findByAnalyticAxis(analyticAxisByCompany.getAnalyticAxis())
                  .count()
              > 0) {
        analyticAxisList.add(analyticAxisByCompany.getAnalyticAxis());
      }
    }
    return analyticAxisList;
  }

  public AnalyticAxis axisChangedInConfig(
      List<AnalyticAxisByCompany> initialList, List<AnalyticAxisByCompany> modifiedList) {
    List<AnalyticAxis> analyticAxisList = new ArrayList<AnalyticAxis>();
    initialList.forEach((axis) -> analyticAxisList.add(axis.getAnalyticAxis()));
    boolean isIn = false;
    for (AnalyticAxis analyticAxis : analyticAxisList) {
      isIn = false;
      for (AnalyticAxisByCompany analyticAxisByCompany : modifiedList) {
        if (analyticAxisByCompany.getAnalyticAxis().equals(analyticAxis)) {
          isIn = true;
        }
      }
      if (!isIn) {
        return analyticAxis;
      }
    }
    return null;
  }

  public boolean orderChangedInConfig(
      List<AnalyticAxisByCompany> initialList, List<AnalyticAxisByCompany> modifiedList) {
    for (AnalyticAxisByCompany analyticAxisByCompanyInit : initialList) {
      for (AnalyticAxisByCompany analyticAxisByCompany : modifiedList) {
        if (analyticAxisByCompanyInit.getOrderSelect() == analyticAxisByCompany.getOrderSelect()
            && !analyticAxisByCompanyInit
                .getAnalyticAxis()
                .equals(analyticAxisByCompany.getAnalyticAxis())) {
          return true;
        }
      }
    }
    return false;
  }
}
