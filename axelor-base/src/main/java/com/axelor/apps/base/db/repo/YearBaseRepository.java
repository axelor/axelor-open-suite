package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Year;

public class YearBaseRepository extends YearRepository {
	
	@Override
	public Year copy(Year year, boolean deep){
		
		year.setPeriodList(null);
		
		return super.copy(year, deep);
	}
	
}
