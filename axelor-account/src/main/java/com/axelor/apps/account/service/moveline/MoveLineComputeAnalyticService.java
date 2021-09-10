package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineComputeAnalyticService {

  MoveLine computeAnalyticDistribution(MoveLine moveLine);

  MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine);

  void updateAccountTypeOnAnalytic(MoveLine moveLine, List<AnalyticMoveLine> analyticMoveLineList);

  void generateAnalyticMoveLines(MoveLine moveLine);

  MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException;

  List<Long> setAxisDomains(MoveLine moveline, int position) throws AxelorException;

  boolean compareNbrOfAnalyticAxisSelect(int position, MoveLine moveLine) throws AxelorException;

  public MoveLine analyzeMoveLine(MoveLine moveLine) throws AxelorException;
}
