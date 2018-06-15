package com.axelor.apps.businessproject.service.projectgenerator.state;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;

public interface ProjectGeneratorState {
  Project generate(SaleOrder saleOrder);

  ActionViewBuilder fill(Project project, SaleOrder saleOrder);
}
