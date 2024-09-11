package com.axelor.apps.project.service.taskStatus;

import com.axelor.apps.project.db.TaskStatusProgressByCategory;
import com.axelor.apps.project.db.repo.TaskStatusProgressByCategoryRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class TaskStatusProgressByCategoryServiceImpl
    implements TaskStatusProgressByCategoryService {

  protected TaskStatusProgressByCategoryRepository taskStatusProgressByCategoryRepository;

  @Inject
  public TaskStatusProgressByCategoryServiceImpl(
      TaskStatusProgressByCategoryRepository taskStatusProgressByCategoryRepository) {
    this.taskStatusProgressByCategoryRepository = taskStatusProgressByCategoryRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateExistingProgressWithValue(
      List<TaskStatusProgressByCategory> taskStatusProgressByCategoryList, BigDecimal newProgress) {
    if (ObjectUtils.isEmpty(taskStatusProgressByCategoryList)) {
      return;
    }

    for (TaskStatusProgressByCategory taskStatusProgressByCategory :
        taskStatusProgressByCategoryList) {
      taskStatusProgressByCategory.setProgress(newProgress);
      taskStatusProgressByCategoryRepository.save(taskStatusProgressByCategory);
    }
  }
}
