package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.Filter;

public class FilterRepository extends JpaRepository<Filter> {

	public FilterRepository() {
		super(Filter.class);
	}

}
