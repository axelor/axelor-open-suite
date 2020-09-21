package com.axelor.apps.docusign.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.docusign.service.DocuSignEnvelopeService;
import com.axelor.apps.docusign.service.DocuSignEnvelopeServiceImpl;
import com.axelor.apps.docusign.service.DocuSignEnvelopeSettingService;
import com.axelor.apps.docusign.service.DocuSignEnvelopeSettingServiceImpl;

public class DocuSignModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(DocuSignEnvelopeService.class).to(DocuSignEnvelopeServiceImpl.class);
    bind(DocuSignEnvelopeSettingService.class).to(DocuSignEnvelopeSettingServiceImpl.class);
  }
}
