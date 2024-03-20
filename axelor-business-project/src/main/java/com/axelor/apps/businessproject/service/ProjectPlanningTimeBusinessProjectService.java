package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.ProjectPlanningTime;
import java.time.LocalDateTime;
import java.util.List;

public interface ProjectPlanningTimeBusinessProjectService {
  void updateProjectPlanningTime(
      ProjectPlanningTime projectPlanningTime,
      LocalDateTime startDateTime,
      LocalDateTime endDateTime,
      String description);

  void updateLinkedEvent(ProjectPlanningTime projectPlanningTime);

  void deleteLinkedProjectPlanningTime(List<Long> ids);
}
