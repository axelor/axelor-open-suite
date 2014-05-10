/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
				List<Period> periods = Period.all().filter("self.company.id = ?1",company.getId()).fetch();
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



