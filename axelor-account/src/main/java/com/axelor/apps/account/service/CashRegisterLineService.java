/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;


import java.io.IOException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.CashRegisterLine;
import com.axelor.apps.account.db.repo.CashRegisterLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CashRegisterLineService{

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected TemplateMessageService templateMessageService;
	protected CashRegisterLineRepository cashRegisterLineRepo;

	protected DateTime todayTime;
	protected User user;

	@Inject
	public CashRegisterLineService(TemplateMessageService templateMessageService, CashRegisterLineRepository cashRegisterLineRepo, UserService userService, GeneralService generalService) {

		this.templateMessageService = templateMessageService;
		this.cashRegisterLineRepo = cashRegisterLineRepo;
		
		this.todayTime = generalService.getTodayDateTime();
		this.user = userService.getUser();

	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Message closeCashRegister(CashRegisterLine cashRegisterLine) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {
		Company company = this.user.getActiveCompany();
		if(company == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CASH_REGISTER_1),
					GeneralServiceImpl.EXCEPTION, this.user.getFullName()), IException.CONFIGURATION_ERROR);
		}

		log.debug("In closeCashRegister");

		CashRegisterLine cashRegisterLineFound = cashRegisterLineRepo.all()
				.filter("self.cashRegister = ?1 and self.cashRegisterDate = ?2 and self.statusSelect = '1'",
						cashRegisterLine.getCashRegister(), cashRegisterLine.getCashRegisterDate()).fetchOne();

		if(cashRegisterLineFound != null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CASH_REGISTER_2),
					GeneralServiceImpl.EXCEPTION), IException.CONFIGURATION_ERROR);
		}
		else  {
			AccountConfig accountConfig = company.getAccountConfig();

			if(accountConfig.getCashRegisterAddressEmail() == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.CASH_REGISTER_3),
						GeneralServiceImpl.EXCEPTION, company.getName()), IException.CONFIGURATION_ERROR);
			}

			cashRegisterLine.setCreateDateTime(this.todayTime);
			cashRegisterLine.setUser(this.user);
			cashRegisterLine.setStatusSelect(CashRegisterLineRepository.CLOSED_CASHREGISTERLINE);
			cashRegisterLineRepo.save(cashRegisterLine);

			return Beans.get(MessageRepository.class).save(this.createCashRegisterLineMail(accountConfig.getCashRegisterAddressEmail(), company, cashRegisterLine));

		}
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void openCashRegister(CashRegisterLine cashRegisterLine)  {

		Beans.get(MessageRepository.class).all().filter("self.cashRegisterLine = ?1", cashRegisterLine).remove();

		cashRegisterLine.setStatusSelect(CashRegisterLineRepository.DRAFT_CASHREGISTERLINE);

		cashRegisterLineRepo.save(cashRegisterLine);
	}


	/**
	 * Procédure permettant de créer un email spécifique aux caisses
	 * @param contact
	 * 			Un contact
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Message createCashRegisterLineMail(String address, Company company, CashRegisterLine cashRegisterLine) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {

		AccountConfig accountConfig = company.getAccountConfig();

		if(accountConfig == null || accountConfig.getCashRegisterTemplate() == null)  {
			throw new AxelorException(String.format(IExceptionMessage.MAIL_1,
					GeneralServiceImpl.EXCEPTION, company.getName()), IException.CONFIGURATION_ERROR);
		}

		return templateMessageService.generateMessage(cashRegisterLine, accountConfig.getCashRegisterTemplate());

	}

}
