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
package com.axelor.apps.bpm.mapper;

import com.axelor.studio.service.mapper.MapperDeserializer;
import com.axelor.studio.service.mapper.MapperField;
import com.axelor.studio.service.mapper.MapperRecord;
import com.axelor.studio.service.mapper.MapperScriptGeneratorServiceImpl;
import com.axelor.studio.service.mapper.MapperValue;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BpmMapperScriptGeneratorServiceImpl extends MapperScriptGeneratorServiceImpl {

  @SuppressWarnings("unchecked")
  @Override
  public void registerDeserializer(SimpleModule module) {

    module.addDeserializer(MapperRecord.class, new MapperDeserializer(BpmMapperRecord.class));
    module.addDeserializer(MapperField.class, new MapperDeserializer(BpmMapperField.class));
    module.addDeserializer(MapperValue.class, new MapperDeserializer(BpmMapperValue.class));
  }
}
