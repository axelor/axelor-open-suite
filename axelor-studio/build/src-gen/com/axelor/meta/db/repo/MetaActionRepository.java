package com.axelor.meta.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaAction;

public class MetaActionRepository extends JpaRepository<MetaAction> {

	public MetaActionRepository() {
		super(MetaAction.class);
	}

	public MetaAction findByID(String xmlId) {
		return Query.of(MetaAction.class)
				.filter("self.xmlId = :xmlId")
				.bind("xmlId", xmlId)
				.cacheable()
				.fetchOne();
	}

	public MetaAction findByName(String name) {
		return Query.of(MetaAction.class)
				.filter("self.name = :name")
				.bind("name", name)
				.order("-priority")
				.cacheable()
				.fetchOne();
	}

	public Query<MetaAction> findByModule(String module) {
		return Query.of(MetaAction.class)
				.filter("self.module = :module")
				.bind("module", module)
				.cacheable();
	}

}
