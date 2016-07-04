package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.WkfTracking;

public class WkfTrackingRepository extends JpaRepository<WkfTracking> {

	public WkfTrackingRepository() {
		super(WkfTracking.class);
	}

}
