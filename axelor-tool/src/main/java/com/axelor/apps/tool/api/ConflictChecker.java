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
