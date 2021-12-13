/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.service;

import com.axelor.apps.bpm.db.WkfModel;
import java.util.List;
import java.util.Map;

public interface BpmDashboardService {

  public Map<String, Object> getData(int offset);

  public List<Map<String, Object>> getChartData(WkfModel wkfModel, String type);

  public List<Map<String, Object>> getInstances();

  public Map<String, Object> getStatusRecords(
      WkfModel wkfModel, String status, String assignedType);

  public List<Long> getInstanceRecords(String status);
}
