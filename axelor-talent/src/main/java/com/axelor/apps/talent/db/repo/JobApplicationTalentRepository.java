package com.axelor.apps.talent.db.repo;

import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.service.JobApplicationService;
import com.google.inject.Inject;

public class JobApplicationTalentRepository extends JobApplicationRepository {
	
	@Inject
	private JobApplicationService jobApplicationService;
	
	@Override
	public JobApplication save(JobApplication entity) {
		
		entity.setFullName(jobApplicationService.computeFullName(entity));
		
		return super.save(entity);
	}

}
