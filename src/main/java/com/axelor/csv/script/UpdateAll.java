/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.db.Company;
import com.google.inject.persist.Transactional;


public class UpdateAll {
		
		@Transactional
		public Object updatePeriod(Object bean, Map values) {
			try {
				assert bean instanceof Company;
				Company company = (Company) bean;
				General general = General.all().fetchOne();
				company.setAdministration(general);
				List<? extends Period> periods = Period.all().filter("self.company.id = ?1",company.getId()).fetch();
				Status opeStatus = Status.all().filter("self.code = 'ope'").fetchOne();
				if(periods == null || periods.isEmpty()) {
					for(Year year : Year.all().filter("self.company.id = ?1",company.getId()).fetch()) {
						for(Integer month : Arrays.asList(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12})) {
							Period period = new Period();
							LocalDate dt = new LocalDate(Integer.parseInt(year.getCode().split("_")[0]),month,1);
							period.setToDate(dt.dayOfMonth().withMaximumValue());
							period.setFromDate(dt.dayOfMonth().withMinimumValue());
							period.setYear(year);
							period.setStatus(opeStatus);
							period.setCompany(company);
							period.setCode(dt.toString().split("-")[1]+"/"+year.getCode().split("_")[0]+"_"+company.getName());
							period.setName(dt.toString().split("-")[1]+'/'+year.getName());
							period.save();
						}
					}
				}
				
				return company;
			}catch(Exception e) {
				e.printStackTrace();
			}
			return bean;
		}
		
}



