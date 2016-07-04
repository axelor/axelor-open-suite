package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.WkfNode;

public class WkfNodeRepository extends JpaRepository<WkfNode> {

	public WkfNodeRepository() {
		super(WkfNode.class);
	}

	public WkfNode findByName(String name) {
		return Query.of(WkfNode.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
