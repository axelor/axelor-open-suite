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
package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QITask;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class QIAnalysisServiceImpl implements QIAnalysisService {

  @Override
  public int setAdvancement(QIAnalysis qiAnalysis) {
    List<QITask> qiTasksList = qiAnalysis.getQiTasksList();
    if (CollectionUtils.isEmpty(qiTasksList)) {
      return 0;
    }
    int totalAdvancement =
        qiTasksList.stream().map(QITask::getAdvancement).reduce((x, y) -> (x + y)).orElse(0);
    return totalAdvancement / qiTasksList.size();
  }
}
