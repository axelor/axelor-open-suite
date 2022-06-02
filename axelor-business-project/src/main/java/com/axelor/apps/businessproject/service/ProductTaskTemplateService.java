/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductTaskTemplateService {

  /**
   * Convert task template list to project task list. This method is recursive.
   *
   * @param templates List of task template to use for convert.
   * @param project Project to set for each project task.
   * @param parent Parent task
   * @param startDate The start date for tasks.
   * @param qty The number copy of the task.
   * @return List of project task convert.
   */
  List<ProjectTask> convert(
      List<? extends TaskTemplate> templates,
      Project project,
      ProjectTask parent,
      LocalDateTime startDate,
      BigDecimal qty,
      SaleOrderLine saleOrderLine)
      throws AxelorException;
}
