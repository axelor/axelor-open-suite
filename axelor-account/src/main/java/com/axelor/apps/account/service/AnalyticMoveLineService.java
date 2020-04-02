/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.List;

public interface AnalyticMoveLineService {
  public BigDecimal chooseComputeWay(Context context, AnalyticMoveLine analyticMoveLine);

  public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine);

  public List<AnalyticMoveLine> generateLines(
      Partner partner, Product product, Company company, BigDecimal total) throws AxelorException;

  public List<AnalyticMoveLine> generateLinesFromPartner(Partner partner, BigDecimal total);

  public List<AnalyticMoveLine> generateLinesFromProduct(
      Product product, Company company, BigDecimal total) throws AxelorException;

  public List<AnalyticMoveLine> generateLinesWithTemplate(
      AnalyticDistributionTemplate template, BigDecimal total);

  public boolean validateLines(List<AnalyticDistributionLine> analyticDistributionLineList);
}
