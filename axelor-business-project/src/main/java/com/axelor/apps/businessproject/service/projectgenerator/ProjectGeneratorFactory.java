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
package com.axelor.apps.businessproject.service.projectgenerator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryAlone;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactorySubProject;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryTask;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryTaskTemplate;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectGeneratorType;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import java.time.LocalDateTime;

public interface ProjectGeneratorFactory {

  /**
   * Create and fill project from sale order.
   *
   * @param saleOrder Use for generate project.
   * @param localDateTime The date to use for create project's elements.
   * @return The project generate.
   * @throws AxelorException If a error occur on creating or filling.
   */
  default Project generate(SaleOrder saleOrder, LocalDateTime localDateTime)
      throws AxelorException {
    Project project = create(saleOrder);
    fill(project, saleOrder, localDateTime);
    return project;
  }

  /**
   * Return the factory associate to generator type.
   *
   * @param type The type of factory.
   * @return The factory associate to type.
   * @throws AxelorException If none factory is found for type.
   */
  static ProjectGeneratorFactory getFactory(ProjectGeneratorType type) throws AxelorException {
    switch (type) {
      case PROJECT_ALONE:
        return Beans.get(ProjectGeneratorFactoryAlone.class);
      case TASK_BY_LINE:
        return Beans.get(ProjectGeneratorFactoryTask.class);
      case SUB_PROJECT_BY_LINE:
        return Beans.get(ProjectGeneratorFactorySubProject.class);
      case TASK_TEMPLATE:
        return Beans.get(ProjectGeneratorFactoryTaskTemplate.class);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BusinessProjectExceptionMessage.FACTORY_NO_FOUND));
    }
  }

  /**
   * Create the project from sale order.
   *
   * @param saleOrder Sale order to be use for create project.
   * @return The new project create.
   * @throws AxelorException If a error occur on creating.
   */
  Project create(SaleOrder saleOrder) throws AxelorException;

  /**
   * Fill the project with elements from sale order.
   *
   * @param project Project to be fill.
   * @param saleOrder Sale order to be use for fill project.
   * @param localDateTime The date to use for create project's elements.
   * @return The project fill with elements from sale order.
   * @throws AxelorException If a error occur on filling.
   */
  ActionViewBuilder fill(Project project, SaleOrder saleOrder, LocalDateTime localDateTime)
      throws AxelorException;
}
