package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.DashletBuilder;

public class DashletBuilderRepository extends JpaRepository<DashletBuilder> {

	public DashletBuilderRepository() {
		super(DashletBuilder.class);
	}

	public DashletBuilder findByName(String name) {
		return Query.of(DashletBuilder.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
