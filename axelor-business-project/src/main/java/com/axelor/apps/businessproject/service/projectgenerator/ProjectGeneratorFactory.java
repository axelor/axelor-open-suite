package com.axelor.apps.businessproject.service.projectgenerator;

import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryAlone;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryPhase;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryTask;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorFactoryTaskTemplate;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectGeneratorType;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import java.time.LocalDateTime;

public interface ProjectGeneratorFactory {

  default Project generate(SaleOrder saleOrder, LocalDateTime localDateTime)
      throws AxelorException {
    Project project = create(saleOrder);
    fill(project, saleOrder, localDateTime);
    return project;
  }

  static ProjectGeneratorFactory getFactory(ProjectGeneratorType type) throws AxelorException {
    switch (type) {
      case PROJECT_ALONE:
        return Beans.get(ProjectGeneratorFactoryAlone.class);
      case TASK_BY_LINE:
        return Beans.get(ProjectGeneratorFactoryTask.class);
      case PHASE_BY_LINE:
        return Beans.get(ProjectGeneratorFactoryPhase.class);
      case TASK_TEMPLATE:
        return Beans.get(ProjectGeneratorFactoryTaskTemplate.class);
      default:
        throw new AxelorException(
            TraceBackRepository.TYPE_FUNCTIONNAL,
            I18n.get("Factory not found this type of generator"));
    }
  }

  Project create(SaleOrder saleOrder) throws AxelorException;

  ActionViewBuilder fill(Project project, SaleOrder saleOrder, LocalDateTime localDateTime)
      throws AxelorException;
}
