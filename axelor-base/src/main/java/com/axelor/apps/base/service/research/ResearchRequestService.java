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
package com.axelor.apps.base.service.research;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ResearchRequest;
import com.axelor.apps.base.db.ResearchResultLine;
import java.util.List;
import java.util.Map;

public interface ResearchRequestService {

  public List<ResearchResultLine> searchObject(
      Map<String, Object> searchParams, ResearchRequest researchRequest) throws AxelorException;

  public String getStringResearchKeyDomain(ResearchRequest researchRequest);

  public String getDateResearchKeyDomain(ResearchRequest researchRequest);

  Map<String, Object> getResultObjectView(ResearchResultLine researchResultLine)
      throws AxelorException;
}
