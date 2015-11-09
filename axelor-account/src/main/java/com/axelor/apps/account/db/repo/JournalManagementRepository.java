package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Journal;

public class JournalManagementRepository extends JournalRepository {


	@Override
	public Journal copy(Journal entity, boolean deep) {

		Journal copy = super.copy(entity, deep);

		copy.setStatusSelect(JournalRepository.STATUS_INACTIVE);
		copy.setIsObsolete(false);
		copy.setSequence(null);

		return copy;
	}
	
	
}
