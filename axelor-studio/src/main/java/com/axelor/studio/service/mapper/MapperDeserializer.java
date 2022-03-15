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
package com.axelor.studio.service.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

@SuppressWarnings("rawtypes")
public class MapperDeserializer extends StdDeserializer {

  private static final long serialVersionUID = 1L;
  private Class<?> subClass;

  @SuppressWarnings("unchecked")
  public MapperDeserializer(Class<?> subClass) {

    super(subClass);

    this.subClass = subClass;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object deserialize(JsonParser parser, DeserializationContext context)
      throws IOException, JsonProcessingException {

    ObjectCodec codec = parser.getCodec();

    return codec.readValue(parser, subClass);
  }
}
