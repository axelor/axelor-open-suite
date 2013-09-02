package com.axelor.apps.crm.web;

import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.EventCategory;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.MeetingType;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Adapter;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class ConvertLeadWizardController {

	@Inject
	private LeadService leadService;
	
	private static final Logger LOG = LoggerFactory.getLogger(ConvertLeadWizardController.class);
	
	public void convertLead(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Context context = request.getContext();
		
		Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");
		
		Lead lead = Lead.find(((Integer)leadContext.get("id")).longValue());
		
		Partner partner = null;
		Partner contactPartner = null;
		Opportunity opportunity = null;
		Event callEvent = null;
		Event meetingEvent = null;
		Event taskEvent = null;
		
		if(context.get("hasConvertIntoContact") != null && (Boolean) context.get("hasConvertIntoContact")) {
			contactPartner = this.createPartner((Map<String, Object>) context.get("contactPartner"));
		}
		else  if(context.get("selectContact") != null) {
			contactPartner = Partner.find((Long) context.get("selectContactPartner"));
		}
		
		if(context.get("hasConvertIntoPartner") != null && (Boolean) context.get("hasConvertIntoPartner")) {
			partner = this.createPartner((Map<String, Object>) context.get("partner"));
		}
		else  if(context.get("selectPartner") != null) {
			partner = Partner.find((Long) context.get("selectPartner"));
		}
		
		if(context.get("hasConvertIntoOpportunity") != null && (Boolean) context.get("hasConvertIntoOpportunity")) {
			opportunity = this.createOpportunity((Map<String, Object>) context.get("opportunity"));
		}
		if(context.get("hasConvertIntoCall") != null && (Boolean) context.get("hasConvertIntoCall")) {
			callEvent = this.createEvent((Map<String, Object>) context.get("callEvent"), 1);
		}
		if(context.get("hasConvertIntoMeeting") != null && (Boolean) context.get("hasConvertIntoMeeting")) {
			meetingEvent = this.createEvent((Map<String, Object>) context.get("meetingEvent"), 2);
		}
		if(context.get("hasConvertIntoTask") != null && (Boolean) context.get("hasConvertIntoTask")) {
			taskEvent = this.createEvent((Map<String, Object>)context.get("taskEvent"), 3);
		}
		
		leadService.convertLead(lead, partner, contactPartner, opportunity, callEvent, meetingEvent, taskEvent);
	}
	
	/**
	 * Create a partner from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Partner createPartnerOld(Partner context) throws AxelorException  {
		
		Partner partner = new Partner();
		
		if(context != null)  {
			LOG.debug("ContextPartner"+context);
			LOG.debug("partnerTypeSelect"+context.getPartnerTypeSelect());
			partner.setFirstName(context.getFirstName());
			partner.setName(context.getName());
			partner.setTitleSelect(context.getTitleSelect());
			partner.setCustomerTypeSelect(context.getCustomerTypeSelect());
			partner.setPartnerTypeSelect(context.getPartnerTypeSelect());
			partner.setIsContact(context.getIsContact());
			partner.setEmail(context.getEmail());
			partner.setFax(context.getFax());
			partner.setWebSite(context.getWebSite());
			partner.setMobilePhonePro(context.getMobilePhonePro());
			partner.setSource(context.getSource());
			partner.setDepartment(context.getDepartment());
			partner.setPicture(context.getPicture());
			partner.setBankDetails(context.getBankDetails());
			partner.setPartnerSeq(leadService.getSequence());
		}	
		// add others
		return partner;
	}
	
	
	public Partner createPartner(Map<String, Object> context) throws AxelorException  {
		
		Mapper mapper = Mapper.of(Partner.class);
		Partner partner = Mapper.toBean(Partner.class, null);
		
		partner = (Partner) this.createObject(context, partner, mapper);
		
		partner.setPartnerSeq(leadService.getSequence());
		
		return partner;
	}
	
	
	
	/**
	 * Create an opportunity from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Opportunity createOpportunityOld(Opportunity context) throws AxelorException  {

		if(context != null)  {
			Opportunity opportunity = new Opportunity();
			
			opportunity.setAmount(context.getAmount()); 
			opportunity.setCampaign(context.getCampaign());
			opportunity.setCompany(context.getCompany());
			opportunity.setBestCase(context.getBestCase());
			opportunity.setCurrency(context.getCurrency());
			opportunity.setDescription(context.getDescription());
			opportunity.setExpectedCloseDate(context.getExpectedCloseDate());
			opportunity.setName(context.getName());
			opportunity.setNextStep(context.getNextStep());
			opportunity.setOpportunityType(context.getOpportunityType());
			opportunity.setPartner(context.getPartner());
			opportunity.setProbability(context.getProbability());
			opportunity.setSalesStageSelect(context.getSalesStageSelect());
			opportunity.setSource(context.getSource());
			opportunity.setTeam(context.getTeam());
			opportunity.setUserInfo(context.getUserInfo());
			opportunity.setWorstCase(context.getWorstCase());
			return opportunity;
		}
		// add others
		return null;
	}
	
	
	public Opportunity createOpportunity(Map<String, Object> context) throws AxelorException  {
		
		Mapper mapper = Mapper.of(Opportunity.class);
		Opportunity opportunity = Mapper.toBean(Opportunity.class, null);
		
		opportunity = (Opportunity) this.createObject(context, opportunity, mapper);
		
		return opportunity;
	}
	
	
	/**
	 * Create an event from a lead (Call or Meeting)
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Event createEventOld(Map<String, Object> context, int type) throws AxelorException  {
		
		if(context != null)  {
			Event event = new Event();
			
			event.setDescription((String) context.get("description"));
			event.setDurationHours((Integer) context.get("durationHours"));
			event.setDurationMinutesSelect((Integer) context.get("durationMinutesSelect"));
			event.setEndDateTime(new DateTime(context.get("endDateTime")).toLocalDateTime());
			event.setIsTimesheetAffected((Boolean) context.get("isTimesheetAffected"));
			event.setLocation((String) context.get("location"));
			event.setPrioritySelect((Integer) context.get("prioritySelect"));
			event.setProgressSelect((Integer) context.get("progressSelect"));
			event.setRelatedToSelect((String) context.get("relatedToSelect"));
			event.setRelatedToSelectId((Integer) context.get("relatedToSelectId"));
			event.setReminder((Boolean) context.get("reminder"));
//			event.setStartDateTime(new DateTime(context.get("startDateTime")).toLocalDateTime());
			
			event.setStartDateTime((LocalDateTime) Adapter.adapt(context.get("startDateTime"), LocalDateTime.class, null, null));
			
			event.setSubject((String) context.get("subject"));
			event.setTicketNumberSeq((String) context.get("ticketNumberSeq"));
			event.setTypeSelect((Integer) context.get("typeSelect"));

			Map<String, Object> eventCategory = (Map<String, Object>) context.get("eventCategory");
			Map<String, Object> meetingType = (Map<String, Object>) context.get("meetingType");
			Map<String, Object> project = (Map<String, Object>) context.get("project");
			Map<String, Object> responsibleUserInfo = (Map<String, Object>) context.get("responsibleUserInfo");
			Map<String, Object> task = (Map<String, Object>) context.get("task");
			Map<String, Object> taskPartner = (Map<String, Object>) context.get("taskPartner");
			Map<String, Object> team = (Map<String, Object>) context.get("team");
			Map<String, Object> userInfo = (Map<String, Object>) context.get("userInfo");
			
//			if(eventCategory != null)  {
//				event.setEventCategory(EventCategory.find((Long) eventCategory.get("id")));
				event.setEventCategory((EventCategory) Adapter.adapt(context.get("eventCategory"), EventCategory.class, null, null));
//			}
			if(meetingType != null)  {
				event.setMeetingType(MeetingType.find((Long) meetingType.get("id")));
			}
			if(project != null)  {
				event.setProject(Project.find((Long) project.get("id")));
			}
			if(responsibleUserInfo != null)  {
				event.setResponsibleUserInfo(UserInfo.find((Long) responsibleUserInfo.get("id")));
			}
			if(task != null)  {
				event.setTask(Task.find((Long) task.get("id")));
			}
			if(taskPartner != null)  {
				event.setTaskPartner(Partner.find((Long) taskPartner.get("id"))); 
			}
			if(team != null)  {
				event.setTeam(Team.find((Long) team.get("id")));
			}
			if(userInfo != null)  {
				event.setUserInfo(UserInfo.find((Long) userInfo.get("id")));
			}
			
			return event;
		}
		// add others
		return null;
	}
	
	
	
	public Event createEvent(Map<String, Object> context, int type) throws AxelorException  {
		
		Mapper mapper = Mapper.of(Event.class);
		Event event = Mapper.toBean(Event.class, null);
		
		event = (Event) this.createObject(context, event, mapper);
		
		event.setTypeSelect(type);
		
		return event;
	}
	
	
	
	public Object createObject(Map<String, Object> context, Object obj, Mapper mapper) throws AxelorException  {
		
		if(context != null)  {
			
			final int random = new Random().nextInt();
			for(final Property p : mapper.getProperties()) {
				
				if (p.isVirtual() || p.isPrimary() || p.isVersion()) {
					continue;
				}

				LOG.debug("Property name / Context value  : {} / {}", p.getName());	
				
				Object value = context.get(p.getName());
				
				LOG.debug("Context value : {}", value);	
				
				if(value != null)  {
				
					if (value instanceof String && p.isUnique()) {
						value = ((String) value) + " (" +  random + ")";
					}	
	
					if(value instanceof Map)  {
						Map map = (Map) value;
						Object id = map.get("id");
						value = JPA.find((Class) p.getTarget(), Long.parseLong(id.toString()));
					} 
					p.set(obj, value);
				}
			}
			
			return obj;
		}
		// add others
		return null;
	}
	
	
}
