package com.axelor.apps.crm.service;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeadActivityServiceImpl implements LeadActivityService {

  protected LeadRepository leadRepository;
  protected MailMessageRepository mailMessageRepository;

  protected static final String LEAD_STATUS_FIELD = "leadStatus";

  @Inject
  public LeadActivityServiceImpl(
      LeadRepository leadRepository, MailMessageRepository mailMessageRepository) {
    this.leadRepository = leadRepository;
    this.mailMessageRepository = mailMessageRepository;
  }

  @Override
  public List<Map<String, Object>> getLeadActivityData(Long id) throws JsonProcessingException {

    List<Map<String, Object>> dataList = new ArrayList<>();
    Lead lead = leadRepository.find(id);

    Map<String, Object> createdOn =
        createActivityCardData(lead.getCreatedOn(), "creation", "Creation", "", "");

    dataList.add(createdOn);
    dataList.addAll(convertEventToMap(lead));
    dataList.addAll(convertTrackingDataToMap(lead));

    // sort list by date desc
    dataList.sort(
        (d1, d2) -> {
          LocalDateTime date1 = (LocalDateTime) d1.get("date");
          LocalDateTime date2 = (LocalDateTime) d2.get("date");
          return date2.compareTo(date1);
        });

    return dataList;
  }

  private List<Map<String, Object>> convertEventToMap(Lead lead) {
    List<Map<String, Object>> eventMapList = new ArrayList<>();
    List<Event> eventList = lead.getEventList();

    for (Event event : eventList) {
      Map<String, Object> data =
          createActivityCardData(
              event.getStartDateTime(),
              "event",
              event.getTypeSelect().toString(),
              event.getStatusSelect().toString(),
              event.getSubject());
      eventMapList.add(data);
    }
    return eventMapList;
  }

  /**
   * Filter tracking on leadStatus field
   *
   * @param lead
   * @return list of tracking data converted to a map
   * @throws JsonProcessingException
   */
  public List<Map<String, Object>> convertTrackingDataToMap(Lead lead)
      throws JsonProcessingException {
    List<MailMessage> mailMessages = mailMessageRepository.findAll(lead, 0, 0);
    List<Map<String, Object>> statusTrackingData = new ArrayList<>();
    mailMessages =
        mailMessages.stream()
            .filter(mailMessage -> "notification".equals(mailMessage.getType()))
            .collect(Collectors.toList());
    ObjectMapper json = Beans.get(ObjectMapper.class);
    for (MailMessage mailMessage : mailMessages) {
      String body = mailMessage.getBody();
      Map<String, Object> bodyData = json.readValue(body, Map.class);

      for (Map<String, String> item : (List<Map>) bodyData.get("tracks")) {
        if (LEAD_STATUS_FIELD.equals(item.get("name"))) {
          String value = item.get("value");
          String oldValue = item.get("oldValue");

          Map<String, Object> data =
              createActivityCardData(
                  mailMessage.getCreatedOn(), LEAD_STATUS_FIELD, oldValue + " => " + value, "", "");
          statusTrackingData.add(data);
        }
      }
    }
    return statusTrackingData;
  }

  /**
   * Convert lead data to activity card map
   *
   * @param date
   * @param type
   * @param activityName
   * @param status
   * @param description
   * @return
   */
  public Map<String, Object> createActivityCardData(
      LocalDateTime date, String type, String activityName, String status, String description) {
    Map<String, Object> data = new HashMap<>();
    data.put("date", date);
    data.put("type", type);
    data.put("activityName", activityName);
    data.put("status", status);
    data.put("description", description);
    return data;
  }
}
