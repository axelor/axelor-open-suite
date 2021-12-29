/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.google.common.base.Throwables;
import java.sql.BatchUpdateException;

@JsonInclude(Include.NON_EMPTY)
public class PortalRestResponse {

  public static final int STATUS_SUCCESS = 0;
  public static final int STATUS_FAILURE = -1;

  @SuppressWarnings("serial")
  private static class OffsetSerializer extends NumberSerializer {

    public OffsetSerializer() {
      super(Integer.class);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Number value) {
      return value == null || value.intValue() == -1;
    }
  }

  @SuppressWarnings("serial")
  private static class TotalSerializer extends NumberSerializer {

    public TotalSerializer() {
      super(Long.class);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Number value) {
      return value == null || value.longValue() == -1;
    }
  }

  private Object data;

  private int status;

  private String message;

  @JsonSerialize(using = OffsetSerializer.class)
  private int offset = -1;

  @JsonSerialize(using = TotalSerializer.class)
  private long total = -1;

  public PortalRestResponse success() {
    this.status = STATUS_SUCCESS;
    return this;
  }

  public PortalRestResponse fail() {
    this.status = STATUS_FAILURE;
    return this;
  }

  public Object getData() {
    return data;
  }

  public PortalRestResponse setData(Object data) {
    this.data = data;
    return this;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public void setException(Throwable throwable) {

    Throwable cause = Throwables.getRootCause(throwable);
    if (cause instanceof BatchUpdateException) {
      cause = ((BatchUpdateException) cause).getNextException();
    }

    String message = throwable.getMessage();
    if (message == null || message.startsWith(cause.getClass().getName())) {
      message = cause.getMessage();
    }
    this.setMessage(message);
    this.setStatus(STATUS_FAILURE);
  }
}
