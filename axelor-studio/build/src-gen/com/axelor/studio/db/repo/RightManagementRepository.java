package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.RightManagement;

public class RightManagementRepository extends JpaRepository<RightManagement> {

	public RightManagementRepository() {
		super(RightManagement.class);
	}

	public RightManagement findByName(String name) {
		return Query.of(RightManagement.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
