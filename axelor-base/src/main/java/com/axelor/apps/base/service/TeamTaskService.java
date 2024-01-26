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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Frequency;
import com.axelor.team.db.TeamTask;

public interface TeamTaskService {

  /** Generates reccurent tasks from given {@link TeamTask} and {@link Frequency} */
  void generateTasks(TeamTask teamTask, Frequency frequency);

  /**
   * Updates fields of next task of given {@link TeamTask}, recursively.
   *
   * <p>This method DOES NOT update potential parent.
   */
  void updateNextTask(TeamTask teamTask);

  /** Removes all next tasks of given {@link TeamTask}. */
  void removeNextTasks(TeamTask teamTask);
}
