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
package com.axelor.apps.base.syncContact;

public class SyncContactResponse {

  private String clientid;

  private String key;

  private String noImport;

  private String importSuccessful;

  private String authFailed;

  public String getNoImport() {
    return noImport;
  }

  public void setNoImport(String noImport) {
    this.noImport = noImport;
  }

  public String getImportSuccessful() {
    return importSuccessful;
  }

  public void setImportSuccessful(String importSuccessful) {
    this.importSuccessful = importSuccessful;
  }

  public String getAuthFailed() {
    return authFailed;
  }

  public void setAuthFailed(String authFailed) {
    this.authFailed = authFailed;
  }

  public String getClientid() {
    return clientid;
  }

  public String getKey() {
    return key;
  }

  public void setClientid(String clientid) {
    this.clientid = clientid;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
