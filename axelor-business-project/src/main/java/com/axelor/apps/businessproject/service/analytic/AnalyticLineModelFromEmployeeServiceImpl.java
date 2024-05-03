package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.google.inject.Inject;
import java.util.ArrayList;

public class AnalyticLineModelFromEmployeeServiceImpl
    implements AnalyticLineModelFromEmployeeService {

  protected AnalyticMoveLineRepository analyticMoveLineRepo;

  @Inject
  public AnalyticLineModelFromEmployeeServiceImpl(AnalyticMoveLineRepository analyticMoveLineRepo) {
    this.analyticMoveLineRepo = analyticMoveLineRepo;
  }

  @Override
  public void copyAnalyticsDataFromEmployee(
      Employee employee, AnalyticLineModel analyticLineModel) {
    if (analyticLineModel.getAnalyticMoveLineList() == null) {
      analyticLineModel.setAnalyticMoveLineList(new ArrayList<>());
    }

    analyticLineModel.setAnalyticDistributionTemplate(employee.getAnalyticDistributionTemplate());

    analyticLineModel.setAxis1AnalyticAccount(employee.getAxis1AnalyticAccount());
    analyticLineModel.setAxis2AnalyticAccount(employee.getAxis2AnalyticAccount());
    analyticLineModel.setAxis3AnalyticAccount(employee.getAxis3AnalyticAccount());
    analyticLineModel.setAxis4AnalyticAccount(employee.getAxis4AnalyticAccount());
    analyticLineModel.setAxis5AnalyticAccount(employee.getAxis5AnalyticAccount());

    for (AnalyticMoveLine originalAnalyticMoveLine : employee.getAnalyticMoveLineList()) {
      AnalyticMoveLine analyticMoveLine =
          analyticMoveLineRepo.copy(originalAnalyticMoveLine, false);

      analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE);
      analyticMoveLine.setEmployee(null);
      analyticLineModel.addAnalyticMoveLineListItem(analyticMoveLine);
    }
    analyticLineModel.copyToModel();
  }
}
