package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.Wkf;

public class WkfRepository extends JpaRepository<Wkf> {

	public WkfRepository() {
		super(Wkf.class);
	}

	public Wkf findByName(String name) {
		return Query.of(Wkf.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
