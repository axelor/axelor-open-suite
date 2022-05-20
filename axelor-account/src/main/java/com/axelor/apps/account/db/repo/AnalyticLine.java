package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import java.util.List;

public interface AnalyticLine {

  public Account getAccount();

  public AnalyticDistributionTemplate getAnalyticDistributionTemplate();

  public List<AnalyticMoveLine> getAnalyticMoveLineList();
}
