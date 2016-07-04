package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.StudioTemplate;

public class StudioTemplateRepository extends JpaRepository<StudioTemplate> {

	public StudioTemplateRepository() {
		super(StudioTemplate.class);
	}

	public StudioTemplate findByName(String name) {
		return Query.of(StudioTemplate.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
