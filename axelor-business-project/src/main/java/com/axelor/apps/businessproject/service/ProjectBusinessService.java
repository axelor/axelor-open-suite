/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.meta.db.MetaFile;
import java.util.List;
import java.util.Map;

public interface ProjectBusinessService extends ProjectService {

  SaleOrder generateQuotation(Project project) throws AxelorException;

  Project generateProject(SaleOrder saleOrder) throws AxelorException;

  Project generatePhaseProject(SaleOrderLine saleOrderLine, Project parent) throws AxelorException;

  void computeProjectTotals(Project project) throws AxelorException;

  void backupToProjectHistory(Project project);

  Map<String, Object> processRequestToDisplayTimeReporting(Long id) throws AxelorException;

  Map<String, Object> processRequestToDisplayFinancialReporting(Long id) throws AxelorException;

  void transitionBetweenPaidStatus(Project project) throws AxelorException;

  List<String> checkPercentagesOver1000OnTasks(Project project);

  Project findProjectFromModel(String modelClassName, Long modelId);

  boolean readyToInvoice(Project project);

  boolean allTimesheetLinesValidated(Project project);

  boolean allExpensesValidated(Project project);

  boolean allTasksHaveTimesheetLines(Project project);

  /**
   * Attaches a MetaFile to any model instance by creating a DMSFile link.
   *
   * @param modelId The ID of the model to attach the file to
   * @param modelClassName Fully qualified class name of the model
   * @param metaFile The MetaFile to attach
   * @throws IllegalArgumentException if the model is not found
   * @throws IllegalStateException if the MetaFile is not persisted
   */
  void attachMetaFileToModel(Long modelId, String modelClassName, MetaFile metaFile);
}
