package com.axelor.meta.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaMenu;

public class MetaMenuRepository extends JpaRepository<MetaMenu> {

	public MetaMenuRepository() {
		super(MetaMenu.class);
	}

	public MetaMenu findByID(String xmlId) {
		return Query.of(MetaMenu.class)
				.filter("self.xmlId = :xmlId")
				.bind("xmlId", xmlId)
				.cacheable()
				.fetchOne();
	}

	public MetaMenu findByName(String name) {
		return Query.of(MetaMenu.class)
				.filter("self.name = :name")
				.bind("name", name)
				.order("-order")
				.cacheable()
				.fetchOne();
	}

	public Query<MetaMenu> findByParent(Long id) {
		return Query.of(MetaMenu.class)
				.filter("self.parent.id = :id")
				.bind("id", id)
				.cacheable();
	}

	public Query<MetaMenu> findByModule(String module) {
		return Query.of(MetaMenu.class)
				.filter("self.module = :module")
				.bind("module", module)
				.cacheable();
	}

}
