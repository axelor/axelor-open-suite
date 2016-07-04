package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.WkfTrackingTotal;

public class WkfTrackingTotalRepository extends JpaRepository<WkfTrackingTotal> {

	public WkfTrackingTotalRepository() {
		super(WkfTrackingTotal.class);
	}

}
