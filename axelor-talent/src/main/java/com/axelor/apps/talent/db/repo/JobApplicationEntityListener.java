/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.talent.db.repo;

import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.inject.Beans;
import javax.persistence.PreRemove;

public class JobApplicationEntityListener {

  @PreRemove
  protected void onJobApplicationPreRemove(JobApplication jobApplication) {
    Beans.get(EventRepository.class)
        .all()
        .filter(
            "relatedToSelect = ?1 AND self.relatedToSelectId = ?2",
            JobApplication.class.getName(),
            jobApplication.getId())
        .autoFlush(false)
        .remove();
  }
}
