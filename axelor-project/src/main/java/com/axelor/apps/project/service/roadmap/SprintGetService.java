package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import java.util.List;

public interface SprintGetService {
  List<Sprint> getSprintToDisplay(Project project);

  List<Sprint> getSprintToDisplayIncludingBacklog(Project project);

  List<Sprint> getSprintList(Project project);

  String getSprintIdsToExclude(List<Sprint> sprintList);
}
