package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.ReportBuilder;

public class ReportBuilderRepository extends JpaRepository<ReportBuilder> {

	public ReportBuilderRepository() {
		super(ReportBuilder.class);
	}

	public ReportBuilder findByName(String name) {
		return Query.of(ReportBuilder.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
