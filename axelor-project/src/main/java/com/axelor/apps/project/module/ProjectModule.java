package com.axelor.apps.project.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.project.db.repo.ProjectTaskManagementRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;

@AxelorModuleInfo(name = "axelor-project")
public class ProjectModule extends AxelorModule {
	@Override
    protected void configure() {
        bind(ProjectTaskRepository.class).to(ProjectTaskManagementRepository.class);
    }
}
