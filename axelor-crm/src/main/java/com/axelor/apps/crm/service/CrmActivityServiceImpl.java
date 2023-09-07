package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
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

public class CrmActivityServiceImpl implements CrmActivityService {

  protected LeadRepository leadRepository;
  protected PartnerRepository partnerRepository;
  protected EventRepository eventRepository;
  protected OpportunityRepository opportunityRepository;
  protected MailMessageRepository mailMessageRepository;
  protected AppCrmService appCrmService;

  protected static final String LEAD_STATUS_FIELD = "leadStatus";
  protected static final String PARTNER_STATUS_FIELD = "partnerStatus";

  @Inject
  public CrmActivityServiceImpl(
      LeadRepository leadRepository,
      PartnerRepository partnerRepository,
      MailMessageRepository mailMessageRepository,
      OpportunityRepository opportunityRepository,
      EventRepository eventRepository,
      AppCrmService appCrmService) {
    this.leadRepository = leadRepository;
    this.partnerRepository = partnerRepository;
    this.mailMessageRepository = mailMessageRepository;
    this.opportunityRepository = opportunityRepository;
    this.eventRepository = eventRepository;
    this.appCrmService = appCrmService;
  }

  @Override
  public List<Map<String, Object>> getLeadActivityData(Long id) throws JsonProcessingException {
    List<Map<String, Object>> dataList = new ArrayList<>();
    Lead lead = leadRepository.find(id);

    Map<String, Object> createdOn =
        createActivityCardData(lead.getCreatedOn(), "creation", "Creation", "", "");

    dataList.add(createdOn);
    dataList.addAll(convertEventToMap(lead.getEventList()));
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

  @Override
  public List<Map<String, Object>> getPartnerActivityData(Long id)
      throws JsonProcessingException, AxelorException {

    List<Map<String, Object>> dataList = new ArrayList<>();
    Partner partner = partnerRepository.find(id);

    Map<String, Object> createdOn =
        createActivityCardData(partner.getCreatedOn(), "creation", "Creation", "", "");

    dataList.add(createdOn);

    List<Event> eventList = eventRepository.findByPartner(partner).fetch();
    List<Opportunity> opportunityList = opportunityRepository.findByPartner(partner).fetch();

    dataList.addAll(convertEventToMap(eventList));
    dataList.addAll(convertOpportunityToMap(opportunityList));
    dataList.addAll(convertTrackingDataToMap(partner));

    // sort list by date desc
    dataList.sort(
        (d1, d2) -> {
          LocalDateTime date1 = (LocalDateTime) d1.get("date");
          LocalDateTime date2 = (LocalDateTime) d2.get("date");
          return date2.compareTo(date1);
        });

    return dataList;
  }

  protected List<Map<String, Object>> convertEventToMap(List<Event> eventList) {
    List<Map<String, Object>> eventMapList = new ArrayList<>();

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

  protected List<Map<String, Object>> convertOpportunityToMap(List<Opportunity> opportunityList)
      throws AxelorException {
    List<Map<String, Object>> eventMapList = new ArrayList<>();

    String closedWinStatus = appCrmService.getClosedWinOpportunityStatus().getName();
    String closedLostStatus = appCrmService.getClosedLostOpportunityStatus().getName();

    for (Opportunity opportunity : opportunityList) {
      Map<String, Object> data =
          createActivityCardData(
              opportunity.getCreatedOn(),
              "opportunity",
              opportunity.getOpportunityRating().toString(),
              opportunity.getOpportunityStatus().getName(),
              opportunity.getOpportunityType());
      data.put("closedWonStatus", closedWinStatus);
      data.put("closedLostStatus", closedLostStatus);
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
  public List<Map<String, Object>> convertTrackingDataToMap(Model model)
      throws JsonProcessingException {
    List<MailMessage> mailMessages = mailMessageRepository.findAll(model, 0, 0);
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
        if (LEAD_STATUS_FIELD.equals(item.get("name"))
            || PARTNER_STATUS_FIELD.equals(item.get("name"))) {
          String value = item.get("value");
          String oldValue = item.get("oldValue");

          String name = "";
          if (!StringUtils.isBlank(oldValue)) {
            name = oldValue + " <i class='fa fa-long-arrow-right'></i> ";
          }
          name += value;

          Map<String, Object> data =
              createActivityCardData(mailMessage.getCreatedOn(), "statusChange", name, "", "");
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
