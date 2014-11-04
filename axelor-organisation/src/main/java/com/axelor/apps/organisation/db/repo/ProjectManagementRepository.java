package com.axelor.apps.organisation.db.repo;

import com.axelor.apps.organisation.db.Project;

public class ProjectManagementRepository extends ProjectRepository {
	@Override
	public Project copy(Project entity, boolean deep) {
		entity.setProjectStatusSelect(1);
		entity.setBusinessStatusSelect(1);
		entity.setProgress(null);
		return super.copy(entity, deep);
	}
}
