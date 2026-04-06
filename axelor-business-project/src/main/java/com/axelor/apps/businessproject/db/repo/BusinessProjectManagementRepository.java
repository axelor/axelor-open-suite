package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.taskreport.TaskReportService;
import com.axelor.apps.hr.db.repo.ProjectHRRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BusinessProjectManagementRepository extends ProjectHRRepository {
  private static final Log log = LogFactory.getLog(BusinessProjectManagementRepository.class);

  @Override
  public Project save(Project project) {
    boolean isNew = project.getVersion() == null || project.getVersion() == 0;

    // Ensure that single user projects don't have more than one member
    try {
      Beans.get(ProjectBusinessService.class).ensureSingleUserProjectConsistency(project);
    } catch (AxelorException e) {
      throw new PersistenceException(e);
    }

    Project savedProject = super.save(project);

    if (isNew) {
      Boolean createTaskReport =
          project.getProjectType() != null
              ? project.getProjectType().getRequiresTask()
              : Boolean.FALSE;
      if (Boolean.TRUE.equals(createTaskReport)) {
        log.debug("Creating Task Report for project: " + project.getName());
        Beans.get(TaskReportService.class).createTaskReport(project);
      }
    }
    return savedProject;
  }
}
