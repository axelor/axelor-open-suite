/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

public class UpdateAll {

  @Inject private YearRepository yearRepo;

  @Inject private PeriodRepository periodRepo;

  @Transactional
  public Object updatePeriod(Object bean, Map<String, Object> values) {
    try {
      assert bean instanceof Company;
      Company company = (Company) bean;
      for (Year year :
          yearRepo
              .all()
              .filter("self.company.id = ?1 AND self.typeSelect = 1", company.getId())
              .fetch()) {
        if (!year.getPeriodList().isEmpty()) {
          continue;
        }
        for (Integer month : Arrays.asList(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})) {
          Period period = new Period();
          LocalDate dt = LocalDate.of(year.getFromDate().getYear(), month, 1);
          period.setFromDate(dt.withDayOfMonth(1));
          period.setToDate(dt.withDayOfMonth(dt.lengthOfMonth()));
          period.setYear(year);
          period.setStatusSelect(PeriodRepository.STATUS_OPENED);
          period.setCode(
              (dt.toString().split("-")[1]
                      + "/"
                      + year.getCode().split("_")[0]
                      + "_"
                      + company.getCode())
                  .toUpperCase());
          period.setName(dt.toString().split("-")[1] + '/' + year.getName());
          periodRepo.save(period);
        }
      }

      return company;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bean;
  }
}
