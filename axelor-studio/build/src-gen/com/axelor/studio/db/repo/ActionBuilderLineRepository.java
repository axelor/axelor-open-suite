package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.ActionBuilderLine;

public class ActionBuilderLineRepository extends JpaRepository<ActionBuilderLine> {

	public ActionBuilderLineRepository() {
		super(ActionBuilderLine.class);
	}

}
