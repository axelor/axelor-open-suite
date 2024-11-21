/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import java.math.BigDecimal;
import java.util.List;

public interface AnalyticLine {

  public BigDecimal getLineAmount();

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
