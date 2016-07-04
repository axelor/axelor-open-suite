package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.ViewItem;

public class ViewItemRepository extends JpaRepository<ViewItem> {

	public ViewItemRepository() {
		super(ViewItem.class);
	}

	public ViewItem findByName(String name) {
		return Query.of(ViewItem.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
