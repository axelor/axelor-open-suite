package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.StudioConfiguration;

public class StudioConfigurationRepository extends JpaRepository<StudioConfiguration> {

	public StudioConfigurationRepository() {
		super(StudioConfiguration.class);
	}

	public StudioConfiguration findByName(String name) {
		return Query.of(StudioConfiguration.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
