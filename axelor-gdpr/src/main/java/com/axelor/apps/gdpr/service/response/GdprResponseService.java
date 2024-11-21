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
package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.auth.db.AuditableModel;
import com.axelor.meta.db.MetaField;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import wslite.json.JSONException;

public interface GdprResponseService {

  public List<MetaField> selectMetaFields(GDPRRequest gdprRequest) throws ClassNotFoundException;

  public Optional<String> getEmailFromPerson(AuditableModel reference);

  public Class<? extends AuditableModel> extractClassFromModel(String model)
      throws ClassNotFoundException;

  public AuditableModel extractReferenceFromModelAndId(String model, Long id)
      throws ClassNotFoundException;

  void generateResponse(GDPRRequest gdprRequest)
      throws AxelorException, IOException, ClassNotFoundException, JSONException;

  void sendResponse(GDPRRequest gdprRequest) throws AxelorException;
}
