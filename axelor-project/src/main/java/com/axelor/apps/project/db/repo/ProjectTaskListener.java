package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTask;
import javax.persistence.PostLoad;

public class ProjectTaskListener {

  @PostLoad
  public void postLoad(ProjectTask projectTask) {
    projectTask.setOldActiveSprint(projectTask.getActiveSprint());
  }
}
