package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.db.Query;

public class ProjectStatusBusinessProjectRepository extends ProjectStatusRepository {
  public ProjectStatus findByNameIgnoreCase(String name) {
    return Query.of(ProjectStatus.class)
        .filter("UPPER(self.name) = UPPER(:name)")
        .bind("name", name)
        .fetchOne();
  }
}
