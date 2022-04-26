package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import java.util.List;

public interface AnalyticLine {

  public Account getAccount();

  public AnalyticDistributionTemplate getAnalyticDistributionTemplate();

  public List<AnalyticMoveLine> getAnalyticMoveLineList();

  public AnalyticAccount getAxis1AnalyticAccount();

  public void setAxis1AnalyticAccount(AnalyticAccount axis1AnalyticAccount);

  public AnalyticAccount getAxis2AnalyticAccount();

  public void setAxis2AnalyticAccount(AnalyticAccount axis2AnalyticAccount);

  public AnalyticAccount getAxis3AnalyticAccount();

  public void setAxis3AnalyticAccount(AnalyticAccount axis3AnalyticAccount);

  public AnalyticAccount getAxis4AnalyticAccount();

  public void setAxis4AnalyticAccount(AnalyticAccount axis4AnalyticAccount);

  public AnalyticAccount getAxis5AnalyticAccount();

  public void setAxis5AnalyticAccount(AnalyticAccount axis5AnalyticAccount);
}
