package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.MenuBuilder;

public class MenuBuilderRepository extends JpaRepository<MenuBuilder> {

	public MenuBuilderRepository() {
		super(MenuBuilder.class);
	}

	public MenuBuilder findByName(String name) {
		return Query.of(MenuBuilder.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
