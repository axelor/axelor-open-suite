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
package com.axelor.apps.base;

import com.axelor.rpc.ActionResponse;
import java.util.function.BiConsumer;

public enum ResponseMessageType {
  INFORMATION(ActionResponse::setInfo),
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
