package com.axelor.exception.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.exception.db.TraceBack;

public class TraceBackRepository extends JpaRepository<TraceBack> {

	public TraceBackRepository() {
		super(TraceBack.class);
	}

	public TraceBack findByName(String name) {
		return Query.of(TraceBack.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
