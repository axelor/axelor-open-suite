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
