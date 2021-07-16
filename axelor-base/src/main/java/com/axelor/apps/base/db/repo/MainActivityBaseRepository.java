package com.axelor.apps.base.db.repo;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.axelor.apps.base.db.MainActivity;

public class MainActivityBaseRepository extends MainActivityRepository {

	protected MainActivityRepository mainActivityRepository;

	@PrePersist
	@PreUpdate
	public void computeFullName(MainActivity mainActivity) {
		if (mainActivity != null) {
			mainActivity.setFullName(mainActivity.getCode() + " - " + mainActivity.getShortName());
		}
	}

}
