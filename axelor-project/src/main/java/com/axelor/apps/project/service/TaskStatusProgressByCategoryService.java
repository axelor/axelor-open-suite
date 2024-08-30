package com.axelor.apps.project.service;

import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import java.math.BigDecimal;
import java.util.List;

public interface TaskStatusProgressByCategoryService {

  void updateExistingProgressWithValue(
      List<TaskStatusProgressByCategory> taskStatusProgressByCategoryList, BigDecimal newProgress);
}
