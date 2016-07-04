package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.WkfTrackingLine;

public class WkfTrackingLineRepository extends JpaRepository<WkfTrackingLine> {

	public WkfTrackingLineRepository() {
		super(WkfTrackingLine.class);
	}

}
