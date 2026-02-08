package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.db.Query;

public class TaskStatusBusinessProjectRepository extends TaskStatusRepository {

  public TaskStatus findByNameIgnoreCase(String name) {
    return Query.of(TaskStatus.class)
        .filter("UPPER(self.name) = UPPER(:name)")
        .bind("name", name)
        .fetchOne();
  }
}
