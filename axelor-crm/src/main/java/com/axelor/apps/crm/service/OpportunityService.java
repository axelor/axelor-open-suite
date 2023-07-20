/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.meta.CallMethod;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;

public interface OpportunityService {

  @Transactional
  public void saveOpportunity(Opportunity opportunity);

  public void setSequence(Opportunity opportunity) throws AxelorException;

  public OpportunityStatus getDefaultOpportunityStatus() throws AxelorException;

  void setOpportunityStatusStagedClosedWon(Opportunity opportunity) throws AxelorException;

  void setOpportunityStatusStagedClosedLost(Opportunity opportunity) throws AxelorException;

  void setOpportunityStatusNextStage(Opportunity opportunity);

  @CallMethod
  public List<Long> getClosedOpportunityStatusIdList();

  public List<Opportunity> winningProcess(Opportunity opportunity, Map<String, Boolean> map)
      throws AxelorException;

  public void lostProcess(
      List<Opportunity> otherOpportunities, LostReason lostReason, String lostReasonStr)
      throws AxelorException;

  public void kanbanOpportunityOnMove(Opportunity opportunity) throws AxelorException;
}
