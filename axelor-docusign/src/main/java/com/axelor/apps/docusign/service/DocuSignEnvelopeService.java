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
