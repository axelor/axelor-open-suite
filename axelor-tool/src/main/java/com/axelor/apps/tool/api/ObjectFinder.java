package com.axelor.apps.tool.api;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import java.util.Objects;
import javax.ws.rs.NotFoundException;

public class ObjectFinder {
  public static Integer NO_VERSION = -1;

  public static <T extends Model> T find(
      Class<T> objectClass, Long objectId, Integer versionProvided) {
    T object = JPA.find(objectClass, objectId);
    if (object == null) {
      throw new NotFoundException(
          "Object from " + objectClass + " with id " + objectId + " was not found.");
    } else {
      T objectFound = JPA.find(objectClass, objectId);
      if (!Objects.equals(versionProvided, NO_VERSION)) {
        ConflictChecker.checkVersion((AuditableModel) objectFound, versionProvided);
      }
      return objectFound;
    }
  }
}
