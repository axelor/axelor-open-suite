/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import java.util.Map;

/**
 * Protects the status of records that may only change through their workflow services (buttons,
 * dedicated API). Called from the validate() method of a repository, which only the generic save
 * (REST API, web client) calls.
 */
public interface WorkflowStatusGuardService {

  /**
   * Refuses the values of a generic save when they change the status of the stored record. A record
   * sent without its version, or with a stale one, is left to the generic save, which does not
   * apply it or rejects it.
   */
  void checkStatusUnchanged(JpaRepository<? extends Model> repository, Map<String, Object> values)
      throws AxelorException;
}
