package com.axelor.apps.gdpr.service;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import wslite.json.JSONException;

public class GDPRAnonymizeServiceImpl implements GDPRAnonymizeService {

  protected MailMessageRepository mailMessageRepository;

  @Inject
  public GDPRAnonymizeServiceImpl(MailMessageRepository mailMessageRepository) {
    this.mailMessageRepository = mailMessageRepository;
  }

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
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  @Override
  public void anonymizeTrackingDatas(AuditableModel reference)
      throws JSONException, ClassNotFoundException, IOException {

    List<MailMessage> mailMessages = searchTrackingDatas(reference);

    Mapper mapper = Mapper.of(reference.getClass());
    ObjectMapper json = Beans.get(ObjectMapper.class);

    for (MailMessage mm : mailMessages) {
      mm.setRelatedName(reference.getId().toString());
      String body = mm.getBody();
      final Map<String, Object> bodyData = json.readValue(body, Map.class);
      final List<Map<String, String>> values = new ArrayList<>();

      for (Map<String, String> item : (List<Map>) bodyData.get("tracks")) {
        values.add(item);
        Object value = mapper.get(reference, item.get("name"));

        if (Objects.nonNull(value)) {
          item.put("value", value.toString());
          item.put("oldValue", value.toString());
        }
      }

      bodyData.put("tracks", values);
      mm.setBody(json.writeValueAsString(bodyData));
      mailMessageRepository.save(mm);
    }
  }

  /**
   * return tracking datas for given model
   *
   * @param model
   * @return
   */
  @Override
  public List<MailMessage> searchTrackingDatas(AuditableModel model) {
    return Beans.get(MailMessageRepository.class).findAll(model, 0, 0);
  }
}
