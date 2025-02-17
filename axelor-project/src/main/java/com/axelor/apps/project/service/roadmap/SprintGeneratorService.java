package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.Sprint;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SprintGeneratorService {
  Map<String, Object> initDefaultValues(Project project, ProjectVersion projectVersion);

  Set<Sprint> generateSprints(
      Project project,
      ProjectVersion projectVersion,
      LocalDate fromDate,
      LocalDate toDate,
      Integer numberDays);

  List<Sprint> getSprintDomain(Project project);
}
