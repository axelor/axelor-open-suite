package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.db.ProductTaskTemplate;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.team.db.TeamTask;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductTaskTemplateService {
  List<TeamTask> convert(
      List<? extends TaskTemplate> templates,
      Project project,
      TeamTask parent,
      LocalDateTime startDate,
      int qty);

  void remove(ProductTaskTemplate productTaskTemplate);
}
