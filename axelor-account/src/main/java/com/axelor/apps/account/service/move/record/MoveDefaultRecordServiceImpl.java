package com.axelor.apps.account.service.move.record;

import java.util.Objects;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.record.MoveDefaultRecordService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.google.inject.Inject;

public class MoveDefaultRecordServiceImpl implements MoveDefaultRecordService{
	
	protected UserService userService;
	protected CompanyRepository companyRepository;
	protected AppBaseService appBaseService;
	
	@Inject
	public MoveDefaultRecordServiceImpl(UserService userService,
			CompanyRepository companyRepository,
			AppBaseService appBaseService) {
		this.userService = userService;
		this.companyRepository = companyRepository;
		this.appBaseService = appBaseService;
	}

	@Override
	public Move setDefaultMoveValues(Move move) {
		
		Objects.requireNonNull(move);
		
		Company activeCompany = userService.getUserActiveCompany();
		
		setCompany(move, activeCompany);
		move.setGetInfoFromFirstMoveLineOk(true);
		setDate(move);
		move.setTechnicalOriginSelect(MoveRepository.TECHNICAL_ORIGIN_ENTRY);
		move.setTradingName(userService.getTradingName());
		this.setDefaultCurrency(move);
		
		return move;
	}

	protected void setDate(Move move) {
		if (move.getDate() == null) {
			move.setDate(appBaseService.getTodayDate(move.getCompany()));
		}
	}
 
	protected void setCompany(Move move, Company activeCompany) {
		if (activeCompany != null) {
			move.setCompany(activeCompany);
		} else {
			if (onlyOneCompanyExist()) {
				move.setCompany(companyRepository.all().fetchOne());
			}
		}
	}

	protected boolean onlyOneCompanyExist() {
		return companyRepository.all().count() == 1;
	}

	@Override
	public Move setDefaultCurrency(Move move) {
		Objects.requireNonNull(move);
		
		Company company = move.getCompany();
		
		if (company != null && company.getCurrency() != null) {
			move.setCompanyCurrency(company.getCurrency());
			move.setCurrency(company.getCurrency());
			move.setCurrencyCode(company.getCurrency().getCodeISO());
		}
		
		return move;
	}

	
	
}
