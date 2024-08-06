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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ExpenseLineProjectServiceImpl implements ExpenseLineProjectService {

  @Inject private ExpenseLineRepository expenseLineRepo;

  @Transactional
  @Override
  public void setProject(List<Long> expenseLineIds, Project project) {

    if (expenseLineIds != null) {

      List<ExpenseLine> expenseLineList =
          expenseLineRepo.all().filter("self.id in ?1", expenseLineIds).fetch();

      for (ExpenseLine line : expenseLineList) {
        line.setProject(project);
        expenseLineRepo.save(line);
      }
    }
  }
}
