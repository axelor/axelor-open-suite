package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.ActionBuilder;

public class ActionBuilderRepository extends JpaRepository<ActionBuilder> {

	public ActionBuilderRepository() {
		super(ActionBuilder.class);
	}

	public ActionBuilder findByName(String name) {
		return Query.of(ActionBuilder.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
