package com.axelor.apps.docusign.service;

import com.axelor.apps.docusign.db.DocuSignEnvelope;
import com.axelor.apps.docusign.db.repo.DocuSignEnvelopeRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@Path("/public/docusign")
public class DocuSignWebService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @POST
  @Path("/update-envelope")
  // @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Response updateEnvelope(@QueryParam("op") String op, String data) {
    LOG.debug("Op : " + op);
    LOG.debug("Data received from DS Connect: " + data);
    if (StringUtils.notEmpty(op) && op.equals("webhook") && StringUtils.notEmpty(data)) {

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder;

      try {
        builder = factory.newDocumentBuilder();

        org.w3c.dom.Document xml = builder.parse(new InputSource(new StringReader(data)));
        xml.getDocumentElement().normalize();
        LOG.debug("Connect data parsed!");
        Element envelopeStatus = (Element) xml.getElementsByTagName("EnvelopeStatus").item(0);
        String envelopeId =
            envelopeStatus
                .getElementsByTagName("EnvelopeID")
                .item(0)
                .getChildNodes()
                .item(0)
                .getNodeValue();
        if (StringUtils.notEmpty(envelopeId)) {
          LOG.debug("envelopeId=" + envelopeId);
          DocuSignEnvelope envelope =
              Beans.get(DocuSignEnvelopeRepository.class)
                  .all()
                  .filter("self.envelopeId = :envelopeId")
                  .bind("envelopeId", envelopeId)
                  .fetchOne();
          if (ObjectUtils.notEmpty(envelope)) {
            envelope = Beans.get(DocuSignEnvelopeService.class).synchroniseEnvelopeStatus(envelope);
          }
        }

      } catch (Exception e) {
        LOG.error(
            "!!!!!! PROBLEM DocuSign Webhook: Couldn't parse the XML sent by DocuSign Connect: "
                + e.getMessage());
        TraceBackService.trace(e);
      }
    }
    return Response.ok().build();
  }
}
