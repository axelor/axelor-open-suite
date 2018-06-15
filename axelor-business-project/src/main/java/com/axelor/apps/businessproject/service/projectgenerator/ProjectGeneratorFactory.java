package com.axelor.apps.businessproject.service.projectgenerator;

import com.axelor.apps.businessproject.service.projectgenerator.state.ProjectGeneratorState;
import com.axelor.apps.businessproject.service.projectgenerator.state.ProjectGeneratorStateAlone;
import com.axelor.apps.businessproject.service.projectgenerator.state.ProjectGeneratorStatePhase;
import com.axelor.apps.businessproject.service.projectgenerator.state.ProjectGeneratorStateTask;
import com.axelor.apps.businessproject.service.projectgenerator.state.ProjectGeneratorStateTaskTemplate;
import com.axelor.apps.project.db.ProjectGeneratorType;
import com.axelor.inject.Beans;
import java.util.EnumMap;
import java.util.Map;

public class ProjectGeneratorFactory {

  private static final Map<ProjectGeneratorType, ProjectGeneratorState> STATES =
      new EnumMap<>(ProjectGeneratorType.class);

  static {
    STATES.put(ProjectGeneratorType.PROJECT_ALONE, Beans.get(ProjectGeneratorStateAlone.class));
    STATES.put(ProjectGeneratorType.PHASE_BY_LINE, Beans.get(ProjectGeneratorStatePhase.class));
    STATES.put(ProjectGeneratorType.TASK_BY_LINE, Beans.get(ProjectGeneratorStateTask.class));
    STATES.put(
        ProjectGeneratorType.TASK_TEMPLATE, Beans.get(ProjectGeneratorStateTaskTemplate.class));
  }

  public ProjectGeneratorState getGenerator(ProjectGeneratorType type) {
    return STATES.get(type);
  }
}
