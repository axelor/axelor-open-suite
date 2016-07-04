package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.WkfTransition;

public class WkfTransitionRepository extends JpaRepository<WkfTransition> {

	public WkfTransitionRepository() {
		super(WkfTransition.class);
	}

	public WkfTransition findByName(String name) {
		return Query.of(WkfTransition.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
