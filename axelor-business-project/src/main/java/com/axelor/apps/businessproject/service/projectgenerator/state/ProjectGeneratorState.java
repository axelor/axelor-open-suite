package com.axelor.apps.businessproject.service.projectgenerator.state;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;

public interface ProjectGeneratorState {
  default Project generate(SaleOrder saleOrder) throws AxelorException {
    throw new AxelorException(
        TraceBackRepository.TYPE_FUNCTIONNAL,
        I18n.get("You can't generate a project with this strategy."));
  }

  default ActionViewBuilder fill(Project project, SaleOrder saleOrder) throws AxelorException {
    throw new AxelorException(
        TraceBackRepository.TYPE_FUNCTIONNAL,
        I18n.get("You can't fill a project with this strategy."));
  }
}
