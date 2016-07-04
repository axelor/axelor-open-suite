package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.WkfTrackingTime;

public class WkfTrackingTimeRepository extends JpaRepository<WkfTrackingTime> {

	public WkfTrackingTimeRepository() {
		super(WkfTrackingTime.class);
	}

}
