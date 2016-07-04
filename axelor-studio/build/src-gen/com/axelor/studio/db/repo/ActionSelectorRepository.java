package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.ActionSelector;

public class ActionSelectorRepository extends JpaRepository<ActionSelector> {

	public ActionSelectorRepository() {
		super(ActionSelector.class);
	}

	public ActionSelector findByName(String name) {
		return Query.of(ActionSelector.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
