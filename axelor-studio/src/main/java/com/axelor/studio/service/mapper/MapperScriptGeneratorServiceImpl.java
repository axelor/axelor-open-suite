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

import com.axelor.exception.service.TraceBackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class MapperScriptGeneratorServiceImpl implements MapperScriptGeneratorService {

  @Override
  public String generate(String mapperJson) {

    MapperRecord mapperRecord = getMapperRecord(mapperJson);
    try {
      if (mapperRecord != null) {
        return mapperRecord.toScript();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return null;
  }

  @Override
  public MapperRecord getMapperRecord(String mapperJson) {

    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    registerDeserializer(module);
    mapper.registerModule(module);

    try {
      MapperRecord mapperRecord = mapper.readValue(mapperJson.getBytes(), MapperRecord.class);
      return mapperRecord;

    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return null;
  }

  @Override
  public void registerDeserializer(SimpleModule module) {}
}
