package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.ViewBuilder;

public class ViewBuilderRepository extends JpaRepository<ViewBuilder> {

	public ViewBuilderRepository() {
		super(ViewBuilder.class);
	}

	public ViewBuilder findByName(String name) {
		return Query.of(ViewBuilder.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
