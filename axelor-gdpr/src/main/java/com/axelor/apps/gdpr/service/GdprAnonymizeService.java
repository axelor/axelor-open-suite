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
package com.axelor.apps.gdpr.service;

import com.axelor.auth.db.AuditableModel;
import com.axelor.mail.db.MailMessage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public interface GdprAnonymizeService {

  List<String> excludeFields =
      Arrays.asList("id", "archived", "version", "statusSelect", "partnerTypeSelect");

  /**
   * return tracking datas for given model
   *
   * @param model
   * @return
   */
  List<MailMessage> searchTrackingDatas(AuditableModel model);

  /**
   * Anonymize tracking datas (track -> oldValue and value)
   *
   * @param reference
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  void anonymizeTrackingDatas(AuditableModel reference) throws IOException;
}
