package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.FilterOperator;

public class FilterOperatorRepository extends JpaRepository<FilterOperator> {

	public FilterOperatorRepository() {
		super(FilterOperator.class);
	}

	public FilterOperator findByName(String name) {
		return Query.of(FilterOperator.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
