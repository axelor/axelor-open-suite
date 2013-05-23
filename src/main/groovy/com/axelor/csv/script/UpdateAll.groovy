package com.axelor.csv.script

import org.joda.time.LocalDate

import com.axelor.apps.account.db.Period
import com.axelor.apps.account.db.Year
import com.axelor.apps.base.db.General
import com.axelor.apps.base.db.Status
import com.axelor.apps.base.db.Company
import com.google.inject.persist.Transactional


class UpdateAll {
		
		@Transactional
		Object updatePeriod(Object bean, Map values) {
			try {
				assert bean instanceof Company
				Company company = (Company) bean
				def general = General.all().fetchOne()
				company.administration = general
				def periods = Period.all().filter("self.company.code = ?1",company.code)
				def opeStatus = Status.all().filter("self.code = 'ope'").fetchOne()
				if(periods.fetch().empty) {
					for(year in Year.all().filter("self.company.code = ?1",company.code).fetch()) {
						for(month in 1..12) {
							def period = new Period()
							LocalDate dt = new LocalDate(year.code.split('_')[0].toInteger(),month,1)
							period.toDate = dt.dayOfMonth().withMaximumValue()
							period.fromDate = dt.dayOfMonth().withMinimumValue()
							period.year = year
							period.status = opeStatus
							period.company = company
							period.code = dt.toString().split('-')[1]+'/'+year.code.split('_')[0]+'_'+company.name
							period.name = dt.toString().split('-')[1]+'/'+year.name
							period.save()
						}
					}
				}
				
				return company
			}catch(Exception e) {
				e.printStackTrace()
			}
		}
		
}



