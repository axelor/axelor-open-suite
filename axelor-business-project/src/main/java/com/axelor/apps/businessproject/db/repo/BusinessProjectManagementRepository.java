package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.apps.hr.db.repo.ProjectHRRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BusinessProjectManagementRepository extends ProjectHRRepository {
  private static final Log log = LogFactory.getLog(BusinessProjectManagementRepository.class);

  @Override
  public Project save(Project project) {
    boolean isNew = project.getVersion() == null || project.getVersion() == 0;
    Project savedProject = super.save(project);

    if (isNew) {
      log.debug("Creating Task Report for project: " + project.getName());
      Beans.get(TaskReportService.class).createTaskReport(project);
    }
    return savedProject;
  }
}
