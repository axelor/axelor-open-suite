package com.axelor.apps.gdpr.service;

import com.axelor.auth.db.AuditableModel;
import com.axelor.mail.db.MailMessage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import wslite.json.JSONException;

public interface GDPRAnonymizeService {

  public static final List<String> excludeFields =
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
   * @throws JSONException
   * @throws ClassNotFoundException
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  void anonymizeTrackingDatas(AuditableModel reference)
      throws JSONException, ClassNotFoundException, IOException;
}
