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
import com.axelor.db.JPA;
import com.axelor.db.Model;
import javax.ws.rs.NotFoundException;

public class ObjectFinder {
  public static int NO_VERSION = -1;

  public static <T extends Model> T find(Class<T> objectClass, Long objectId, int versionProvided) {
    T object = JPA.find(objectClass, objectId);
    if (object == null) {
      throw new NotFoundException(
          "Object from " + objectClass + " with id " + objectId + " was not found.");
    } else {
      T objectFound = JPA.find(objectClass, objectId);
      if (versionProvided != NO_VERSION) {
        ConflictChecker.checkVersion((AuditableModel) objectFound, versionProvided);
      }
      return objectFound;
    }
  }
}
