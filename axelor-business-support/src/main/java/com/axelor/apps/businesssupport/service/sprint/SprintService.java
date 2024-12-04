package com.axelor.apps.businesssupport.service.sprint;

import com.axelor.apps.businesssupport.db.Sprint;
import com.axelor.apps.project.db.Project;
import java.util.List;

public interface SprintService {
  List<Sprint> getCurrentSprintsRelatedToTheProject(Project project);
}
