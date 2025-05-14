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
package com.axelor.apps.businessproject.service.projectgenerator.factory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.sale.db.SaleOrder;

public interface ProjectGeneratorSaleService {

  /**
   * Create the project from sale order.
   *
   * @param saleOrder Sale order to be use for create project.
   * @param projectTemplate The project template that can be used to create project structure
   * @return The new project create.
   * @throws AxelorException If a error occur on creating.
   */
  Project create(SaleOrder saleOrder, ProjectTemplate projectTemplate) throws AxelorException;
}
