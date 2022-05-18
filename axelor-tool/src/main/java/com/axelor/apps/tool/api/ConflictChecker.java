package com.axelor.apps.tool.api;

import com.axelor.auth.db.AuditableModel;
import java.util.Objects;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

public class ConflictChecker {

  public static void checkVersion(AuditableModel currentObject, Integer versionProvided) {
    if (!Objects.equals(currentObject.getVersion(), versionProvided)) {
      throw new ClientErrorException(
          "Object provided has been updated by another user", Response.Status.CONFLICT);
    }
  }
}
