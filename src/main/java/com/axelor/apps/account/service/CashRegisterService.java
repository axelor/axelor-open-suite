package com.axelor.apps.account.service;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.CashRegister;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.MailService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CashRegisterService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CashRegisterService.class);
	
	@Inject
	private MailService ms;
	
	private DateTime todayTime;
	private UserInfo user;

	@Inject
	public CashRegisterService(UserInfoService uis) {
		
		this.todayTime = GeneralService.getTodayDateTime();
		this.user = uis.getUserInfo();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Mail closeCashRegister(CashRegister cashRegister) throws AxelorException  {
		Company company = this.user.getActiveCompany();
		if(company == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une société active pour l'utilisateur %s",
					GeneralService.getExceptionAccountingMsg(), this.user.getFullName()), IException.CONFIGURATION_ERROR);
		}
		
		LOG.debug("In closeCashRegister");

		CashRegister cashRegisterFind = CashRegister
				.all().filter("self.agency = ?1 and self.cashRegisterDate = ?2 and self.stateSelect = '1'", 
						cashRegister.getAgency(), cashRegister.getCashRegisterDate()).fetchOne();
		
		if(cashRegisterFind != null)  {
			throw new AxelorException(String.format("%s :\n Une caisse existe déjà pour la même date de la caisse et la même agence",
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		else  {
			if(company.getCashRegisterAddressEmail() == null)  {
				throw new AxelorException(String.format("%s :\n Veuillez configurer une adresse email Caisses pour la société %s",
						GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			cashRegister.setCreateDateTime(this.todayTime);
			cashRegister.setUserInfo(this.user);
			cashRegister.setStateSelect(IAccount.CLOSED_CASHREGISTER);
			cashRegister.save();
			
			return ms.createCashRegisterMail(company.getCashRegisterAddressEmail(), company, cashRegister).save();
			
		}
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void openCashRegister(CashRegister cashRegister)  {
		 Mail.all().filter("cashRegister = ?1", cashRegister).remove();
		 cashRegister.setStateSelect(IAccount.DRAFT_CASHREGISTER);
		 cashRegister.save();
	}
	
	
}
