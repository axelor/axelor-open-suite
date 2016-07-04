package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.ViewPanel;

public class ViewPanelRepository extends JpaRepository<ViewPanel> {

	public ViewPanelRepository() {
		super(ViewPanel.class);
	}

	public ViewPanel findByName(String name) {
		return Query.of(ViewPanel.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
