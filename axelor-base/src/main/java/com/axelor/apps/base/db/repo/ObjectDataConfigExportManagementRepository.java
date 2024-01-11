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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.ObjectDataConfigExport;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;

public class ObjectDataConfigExportManagementRepository extends ObjectDataConfigExportRepository {

  @Override
  public ObjectDataConfigExport save(ObjectDataConfigExport entity) {

    try {
      Class<? extends Model> klass =
          (Class<? extends Model>) Class.forName(entity.getModelSelect());

      JpaRepository<? extends Model> repo = JpaRepository.of(klass);
      Object obj = repo.all().filter("self.id = ?", entity.getModelSelectId()).fetchOne();

      if (obj != null) {
        Mapper mapper = Mapper.of(obj.getClass());
        if (mapper.getNameField() != null && mapper.getNameField().get(obj) != null) {
          entity.setRecordName(mapper.getNameField().get(obj).toString());
        } else {
          entity.setRecordName(mapper.get(obj, "id").toString());
        }
      }
    } catch (ClassNotFoundException e) {
      TraceBackService.trace(e);
    }

    return super.save(entity);
  }
}
