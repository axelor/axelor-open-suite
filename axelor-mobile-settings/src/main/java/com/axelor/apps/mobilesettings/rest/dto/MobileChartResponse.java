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
package com.axelor.apps.mobilesettings.rest.dto;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.utils.api.ResponseStructure;
import java.util.List;

public class MobileChartResponse extends ResponseStructure {
  protected Long chartId;
  protected String chartName;
  protected String chartType;
  protected List<MobileChartValueResponse> valueList;

  public MobileChartResponse(
      MobileChart mobileChart, String chartName, List<MobileChartValueResponse> valueList) {
    super(mobileChart.getVersion());
    this.chartId = mobileChart.getId();
    this.chartName = chartName;
    this.chartType = mobileChart.getChartTypeSelect();
    this.valueList = valueList;
  }

  public Long getChartId() {
    return chartId;
  }

  public String getChartName() {
    return chartName;
  }

  public String getChartType() {
    return chartType;
  }

  public List<MobileChartValueResponse> getValueList() {
    return valueList;
  }
}
