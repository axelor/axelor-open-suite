/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import javax.ws.rs.core.Response;

public class ResponseBody {

  private final int codeStatus;
  private final String messageStatus;
  private final ResponseStructure object;

  public ResponseBody(Response.Status codeStatus, String messageStatus) {
    this.codeStatus = codeStatus.getStatusCode();
    this.messageStatus = messageStatus;
    this.object = null;
  }

  public ResponseBody(Response.Status codeStatus, String messageStatus, ResponseStructure object) {
    this.codeStatus = codeStatus.getStatusCode();
    this.messageStatus = messageStatus;
    this.object = object;
  }

  public int getCodeStatus() {
    return codeStatus;
  }

  public String getMessageStatus() {
    return messageStatus;
  }

  public ResponseStructure getObject() {
    return object;
  }
}
