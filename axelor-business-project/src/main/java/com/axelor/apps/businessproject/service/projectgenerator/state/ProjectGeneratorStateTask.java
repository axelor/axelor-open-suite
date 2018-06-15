package com.axelor.apps.businessproject.service.projectgenerator.state;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;

public class ProjectGeneratorStateTask implements ProjectGeneratorState {

  @Override
  public Project generate(SaleOrder saleOrder) {
    return null;
  }

  @Override
  public ActionViewBuilder fill(Project project, SaleOrder saleOrder) {
    return null;
  }
}
