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

import com.axelor.apps.base.service.AnonymizeService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.mapper.Mapper;
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

public class GdprAnonymizeServiceImpl implements GdprAnonymizeService {

  protected MailMessageRepository mailMessageRepository;
  protected AnonymizeService anonymizeService;

  protected static final String MAIL_MESSAGE_TYPE_NOTIFICATION = "notification";

  @Inject
  public GdprAnonymizeServiceImpl(
      MailMessageRepository mailMessageRepository, AnonymizeService anonymizeService) {
    this.mailMessageRepository = mailMessageRepository;
    this.anonymizeService = anonymizeService;
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
  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void anonymizeTrackingDatas(AuditableModel reference) throws IOException {

    List<MailMessage> mailMessages = searchTrackingDatas(reference);

    Mapper mapper = Mapper.of(reference.getClass());
    ObjectMapper json = Beans.get(ObjectMapper.class);

    for (MailMessage mm : mailMessages) {
      anonymizeMailMessage(reference, mapper, json, mm);
      mailMessageRepository.save(mm);
    }
  }

  protected void anonymizeMailMessage(
      AuditableModel reference, Mapper mapper, ObjectMapper json, MailMessage mm)
      throws IOException {
    if (MAIL_MESSAGE_TYPE_NOTIFICATION.equals(mm.getType())) {
      anonymizeNotification(reference, mapper, json, mm);
    } else {
      anonymizeCommentAndMail(mm);
    }
  }

  protected void anonymizeCommentAndMail(MailMessage mm) {
    String subject = mm.getSubject();
    String body = mm.getBody();
    if (subject != null) {
      mm.setSubject(anonymizeService.hashValue(mm.getSubject()));
    }
    if (body != null) {
      mm.setBody(anonymizeService.hashValue(mm.getBody()));
    }
  }

  protected void anonymizeNotification(
      AuditableModel reference, Mapper mapper, ObjectMapper json, MailMessage mm)
      throws IOException {
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
