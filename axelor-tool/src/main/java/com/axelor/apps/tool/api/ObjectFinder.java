package com.axelor.apps.tool.api;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import javax.ws.rs.NotFoundException;

public class ObjectFinder {

  public static <T extends Model> T find(Class<T> objectClass, Long objectId) {
    T object = JPA.find(objectClass, objectId);
    if (object == null) {
      throw new NotFoundException(
          "Object from " + objectClass + " with id " + objectId + " was not found.");
    } else {
      return JPA.find(objectClass, objectId);
    }
  }
}
