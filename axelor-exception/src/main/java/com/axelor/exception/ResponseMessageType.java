/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.exception;

import com.axelor.rpc.ActionResponse;
import java.util.function.BiConsumer;

public enum ResponseMessageType {
  INFORMATION(ActionResponse::setFlash),
  WARNING(ActionResponse::setAlert),
  ERROR(ActionResponse::setError),
  NOTIFICATION(ActionResponse::setNotify);

  private BiConsumer<ActionResponse, String> messageMethod;

  ResponseMessageType(BiConsumer<ActionResponse, String> messageMethod) {
    this.messageMethod = messageMethod;
  }

  public void setMessage(ActionResponse response, String message) {
    messageMethod.accept(response, message);
  }
}
