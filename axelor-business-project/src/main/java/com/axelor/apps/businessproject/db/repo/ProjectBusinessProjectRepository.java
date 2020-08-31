package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.db.AppBusinessProject;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.repo.ProjectHRRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class ProjectBusinessProjectRepository extends ProjectHRRepository {

  @Override
  public Project save(Project project) {
    try {
      AppBusinessProject appBusinessProject =
          Beans.get(AppBusinessProjectService.class).getAppBusinessProject();

      if (StringUtils.isBlank(project.getCode())
          && appBusinessProject.getGenerateProjectSequence()) {
        Company company = project.getCompany();
        String seq =
            Beans.get(SequenceService.class)
                .getSequenceNumber(SequenceRepository.PROJECT_SEQUENCE, company);

        if (seq == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.PROJECT_SEQUENCE_ERROR),
              company.getName());
        }

        project.setCode(seq);
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
    return super.save(project);
  }
}
