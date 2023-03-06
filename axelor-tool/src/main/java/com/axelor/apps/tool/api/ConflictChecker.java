/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.api;

import com.axelor.auth.db.AuditableModel;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

public class ConflictChecker {

  public static void checkVersion(AuditableModel currentObject, int versionProvided) {
    if (currentObject.getVersion() != versionProvided) {
      throw new ClientErrorException(
          "Object provided has been updated by another user", Response.Status.CONFLICT);
    }
  }
}
