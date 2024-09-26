package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.util.Optional;

public class ProjectTaskAttrsServiceImpl implements ProjectTaskAttrsService {

  protected MetaModelRepository metaModelRepository;

  @Inject
  public ProjectTaskAttrsServiceImpl(MetaModelRepository metaModelRepository) {
    this.metaModelRepository = metaModelRepository;
  }

  @Override
  public String getTagDomain(ProjectTask projectTask) {
    Company company =
        Optional.ofNullable(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getCompany)
            .orElse(null);
    String domain =
        String.format(
            "(self.concernedModelSet IS EMPTY OR %s member of self.concernedModelSet)",
            metaModelRepository.findByName("ProjectTask").getId());

    if (company != null) {
      domain =
          domain.concat(
              String.format(
                  " AND (self.companySet IS EMPTY OR %s member of self.companySet)",
                  company.getId()));
    }

    return domain;
  }
}
