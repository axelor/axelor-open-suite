package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineComputeAnalyticService {

  MoveLine computeAnalyticDistribution(MoveLine moveLine);

  MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine);

  void updateAccountTypeOnAnalytic(MoveLine moveLine, List<AnalyticMoveLine> analyticMoveLineList);

  void generateAnalyticMoveLines(MoveLine moveLine);

  MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException;

  boolean compareNbrOfAnalyticAxisSelect(int position, Move move) throws AxelorException;

  List<Long> setAxisDomains(MoveLine moveLine, Move move, int position) throws AxelorException;

  MoveLine analyzeMoveLine(MoveLine moveLine, Company company) throws AxelorException;
}
