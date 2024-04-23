package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.google.inject.Inject;
import java.util.ArrayList;

public class AnalyticLineModelFromContractServiceImpl
    implements AnalyticLineModelFromContractService {

  protected AnalyticMoveLineRepository analyticMoveLineRepo;

  @Inject
  public AnalyticLineModelFromContractServiceImpl(AnalyticMoveLineRepository analyticMoveLineRepo) {
    this.analyticMoveLineRepo = analyticMoveLineRepo;
  }

  @Override
  public void copyAnalyticsDataFromContractLine(
      ContractLine contractLine, AnalyticLineModel analyticLineModel) {
    if (analyticLineModel.getAnalyticMoveLineList() == null) {
      analyticLineModel.setAnalyticMoveLineList(new ArrayList<>());
    }

    analyticLineModel.setAnalyticDistributionTemplate(
        contractLine.getAnalyticDistributionTemplate());

    analyticLineModel.setAxis1AnalyticAccount(contractLine.getAxis1AnalyticAccount());
    analyticLineModel.setAxis2AnalyticAccount(contractLine.getAxis2AnalyticAccount());
    analyticLineModel.setAxis3AnalyticAccount(contractLine.getAxis3AnalyticAccount());
    analyticLineModel.setAxis4AnalyticAccount(contractLine.getAxis4AnalyticAccount());
    analyticLineModel.setAxis5AnalyticAccount(contractLine.getAxis5AnalyticAccount());

    for (AnalyticMoveLine originalAnalyticMoveLine : contractLine.getAnalyticMoveLineList()) {
      AnalyticMoveLine analyticMoveLine =
          analyticMoveLineRepo.copy(originalAnalyticMoveLine, false);

      analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);
      analyticMoveLine.setContractLine(null);
      analyticLineModel.addAnalyticMoveLineListItem(analyticMoveLine);
    }
    analyticLineModel.copyToModel();
  }
}
