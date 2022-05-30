package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface AnalyticLineService {

  List<Long> getAxisDomains(AnalyticLine line, Company company, int position)
      throws AxelorException;

  boolean isAxisRequired(AnalyticLine line, Company company, int position) throws AxelorException;

  AnalyticLine checkAnalyticLineForAxis(AnalyticLine line);

  AnalyticLine printAnalyticAccount(AnalyticLine line, Company company) throws AxelorException;
}
