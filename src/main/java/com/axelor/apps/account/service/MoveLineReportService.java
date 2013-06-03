package com.axelor.apps.account.service;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class MoveLineReportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MoveLineReportService.class);
	
	@Inject
	private Injector injector;
	
	private DateTime dateTime;
	
	@Inject
	public MoveLineReportService() {
		
		dateTime = GeneralService.getTodayDateTime();
		
	}
	
	public List<MoveLine> getMoveLineList(MoveLineReport moveLineReport)  {
		
		LOG.debug("Begin getMoveLineList in service");
		
		String sqlRequest = "";
		String and = " AND ";
		
		if(moveLineReport.getCompany() != null)  {  sqlRequest += String.format("self.move.company = %s", moveLineReport.getCompany().getId());  } 
		
		if(moveLineReport.getDateFrom() != null)  {
			if(!sqlRequest.equals(""))  { sqlRequest += and;  }
			sqlRequest += String.format("self.date >='%s'", moveLineReport.getDateFrom().toString());  
		}
		
		if(moveLineReport.getDateTo() != null)  {
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		
		if(moveLineReport.getDate() != null)  {
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.date <= '%s'", moveLineReport.getDate().toString());  
		}
			
		if(moveLineReport.getJournal() != null)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.move.journal = %s", moveLineReport.getJournal().getId());  
		}
		
		if(moveLineReport.getPeriod() != null)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.move.period = %s", moveLineReport.getPeriod().getId());  
		}
			
		if(moveLineReport.getAccount() != null)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.account = %s", moveLineReport.getAccount().getId());  
		}
		
		if(moveLineReport.getFromPartner() != null)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.partner.name >= '%s'", moveLineReport.getFromPartner().getName().replace("'", " "));  
		}
		
		if(moveLineReport.getToPartner() != null)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.partner.name <= '%s'", moveLineReport.getToPartner().getName().replace("'", " "));  
		}
		
		if(moveLineReport.getPartner() != null)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.partner = %s", moveLineReport.getPartner().getId());  
		}
		
		if(moveLineReport.getYear() != null)  {
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.move.period.year = %s", moveLineReport.getYear().getId()); 
		}
		
		if(moveLineReport.getPaymentMode() != null)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.move.paymentMode = %s", moveLineReport.getPaymentMode().getId());  
		}
		
		if(moveLineReport.getTypeSelect() > 5 && moveLineReport.getTypeSelect() < 10)  {
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.move.journal.type = %s", this.getJournalType(moveLineReport).getId());  
		}
		
		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 5)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.move.paymentMode.code = 'CHQ'");  
		}
		
		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 10)	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.move.paymentMode.code = 'ESP'");  
		}
		
		if(moveLineReport.getTypeSelect() != null &&( moveLineReport.getTypeSelect() == 5 ))	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.amountPaid > 0 AND self.credit > 0");  
		}
		
		if(moveLineReport.getTypeSelect() != null &&( moveLineReport.getTypeSelect() == 10 ))	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.credit > 0");  
		}
		
		if(moveLineReport.getTypeSelect() != null && ( moveLineReport.getTypeSelect() <= 5 || moveLineReport.getTypeSelect() == 10 ))	{
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.account.reconcileOk = 'true'");  
		}
			
		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 1)  {
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += String.format("self.credit > 0");
		}
		
		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 12)  {
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += "self.account.code LIKE '7%'";
		}
		
		if(moveLineReport.getTypeSelect() != null && moveLineReport.getTypeSelect() == 4)  {
			if(!sqlRequest.equals("")) {  sqlRequest += and;  }
			sqlRequest += "self.amountRemaining > 0 AND self.debit > 0";
		}
		
		if(!sqlRequest.equals("")) {  sqlRequest += and;  }
		sqlRequest += String.format("self.ignoreInAccountingOk = 'false'");  
		
		LOG.debug("Requete : {}", sqlRequest);
		
		List<MoveLine> moveLineList = MoveLine.all().filter(sqlRequest).fetch();
		
		LOG.debug("End getMoveLineList in service");
		
		return moveLineList;
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setSequence(MoveLineReport moveLineReport, String sequence)  {
		moveLineReport.setRef(sequence);
		moveLineReport.save();
	}
	
	public String getSequence(MoveLineReport moveLineReport) throws AxelorException  {
		if(moveLineReport.getTypeSelect() > 0)  {
			 
			 SequenceService sgs = injector.getInstance(SequenceService.class);
			if(moveLineReport.getTypeSelect() <= 5 || moveLineReport.getTypeSelect() >= 10 )  {
				
				String seq = sgs.getSequence(IAdministration.MOVE_LINE_REPORT, moveLineReport.getCompany(), false);
				if(seq != null && !seq.isEmpty())  {  
					return seq;
				}
				else  {  
					throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Reporting comptable pour la société %s",
							GeneralService.getExceptionAccountingMsg(), moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
			else  {
				String seq = sgs.getSequence(IAdministration.MOVE_LINE_EXPORT, moveLineReport.getCompany(), false);
				if(seq != null && !seq.isEmpty())  {  
					return seq;
				}
				else  {  
					throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Export comptable pour la société %s",
							GeneralService.getExceptionAccountingMsg(), moveLineReport.getCompany().getName()), IException.CONFIGURATION_ERROR);
				}
			}
		}
		else  return "";
	}
	
	
	public JournalType getJournalType(MoveLineReport moveLineReport)  {
		if(moveLineReport.getTypeSelect() ==  6)  {
			return JournalType.all().filter("self.code = 'vte'").fetchOne();
		}
		else if(moveLineReport.getTypeSelect() ==  7)  {
			return JournalType.all().filter("self.code = 'avr'").fetchOne();
		}
		else if(moveLineReport.getTypeSelect() ==  8)  {
			return JournalType.all().filter("self.code = 'trs'").fetchOne();
		}
		else if(moveLineReport.getTypeSelect() ==  9)  {
			return JournalType.all().filter("self.code = 'ach'").fetchOne();
		}
		return null;
	}
	
	public Account getAccount(MoveLineReport moveLineReport)  {
		if(moveLineReport.getTypeSelect() ==  13 && moveLineReport.getCompany() != null)  {
			return Account.all().filter("self.company = ?1 AND self.code LIKE '58%'", moveLineReport.getCompany()).fetchOne();
		}
		return null;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setStatus(MoveLineReport moveLineReport)  {
		moveLineReport.setStatus(Status.all().filter("self.code = 'val'").fetchOne());
		moveLineReport.save();
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void setPublicationDateTime(MoveLineReport moveLineReport)  {
		moveLineReport.setPublicationDateTime(this.dateTime);
		moveLineReport.save();
	}
	
}
