/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.docusign.service;

import com.axelor.apps.docusign.db.DocuSignEnvelope;
import com.axelor.apps.docusign.db.DocuSignEnvelopeSetting;
import com.axelor.exception.AxelorException;
import java.util.Map;

public interface DocuSignEnvelopeService {

  public Map<String, Object> generateEnvelope(
      DocuSignEnvelopeSetting envelopeSetting, Long objectId) throws AxelorException;

  public DocuSignEnvelope createEnvelope(DocuSignEnvelopeSetting envelopeSetting, Long objectId)
      throws AxelorException;

  public DocuSignEnvelope sendEnvelope(DocuSignEnvelope docuSignEnvelope) throws AxelorException;

  public DocuSignEnvelope synchroniseEnvelopeStatus(DocuSignEnvelope docuSignEnvelope)
      throws AxelorException;
}
