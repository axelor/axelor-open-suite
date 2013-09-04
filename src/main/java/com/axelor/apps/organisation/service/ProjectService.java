package com.axelor.apps.organisation.service;

import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.google.inject.persist.Transactional;

public class ProjectService {

	@Transactional
	public void createDefaultTask(Project project) {

		if(project.getAffairName() != null && !project.getAffairName().isEmpty()) {
			Task findTask = Task.all().filter("name = ?", project.getAffairName()).fetchOne();
			if(findTask == null) {
				Task task = new Task();

				task.setName(project.getAffairName());
				task.save();
			}
		}
	}

	@Transactional
	public void createPreSalesTask(Project project) {

		if(project.getAffairName() != null && !project.getAffairName().isEmpty()) {
			Task findTask = Task.all().filter("name = ?", "Avant vente "+project.getAffairName()).fetchOne();
			if(findTask == null) {
				Task task = new Task();

				task.setName("Avant vente "+project.getAffairName());
				task.save();
			}
		}
	}
	
}
