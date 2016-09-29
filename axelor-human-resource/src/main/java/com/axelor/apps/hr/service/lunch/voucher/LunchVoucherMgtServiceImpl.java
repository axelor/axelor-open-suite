package com.axelor.apps.hr.service.lunch.voucher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LunchVoucherMgtServiceImpl implements LunchVoucherMgtService{
	
	protected UserRepository userRepository;
	
	protected LunchVoucherMgtRepository lunchVoucherMgtRepository;
	
	protected LunchVoucherMgtLineService lunchVoucherMgtLineService;
	
	protected HRConfigService hrConfigService;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	public LunchVoucherMgtServiceImpl(UserRepository userRepository, LunchVoucherMgtLineService lunchVoucherMgtLineService, 
								      LunchVoucherMgtRepository lunchVoucherMgtRepository, HRConfigService hrConfigService){
		
		this.userRepository = userRepository;
		this.lunchVoucherMgtLineService = lunchVoucherMgtLineService;
		this.lunchVoucherMgtRepository = lunchVoucherMgtRepository;
		this.hrConfigService = hrConfigService;
	}
	
	@Override
	@Transactional
	public void calculate(LunchVoucherMgt lunchVoucherMgt) throws AxelorException {
		Company company = lunchVoucherMgt.getCompany();
		List<User> UsersList = userRepository.all().filter("self.activeCompany = ?1", company).fetch();
		for (User user : UsersList) {
			Employee employee = user.getEmployee();
			if (employee != null){
				LunchVoucherMgtLine LunchVoucherMgtLine = lunchVoucherMgtLineService.create(employee);
				lunchVoucherMgt.addLunchVoucherMgtLineListItem(LunchVoucherMgtLine);
			}	
		}
		lunchVoucherMgt.setStatusSelect(LunchVoucherMgtRepository.STATUS_CALCULATED);
		calculateTotal(lunchVoucherMgt);
		lunchVoucherMgtRepository.save(lunchVoucherMgt);
	}
	
	@Transactional
	public void calculateTotal(LunchVoucherMgt lunchVoucherMgt) {
		int total = 0;
		List<LunchVoucherMgtLine> lunchVoucherMgtLineList = lunchVoucherMgt.getLunchVoucherMgtLineList();
		for (LunchVoucherMgtLine lunchVoucherMgtLine : lunchVoucherMgtLineList) {
			total += lunchVoucherMgtLine.getLunchVoucherNumber();
		}
		lunchVoucherMgt.setTotalLunchVouchers(total);
	}
	
	@Override
	public int checkStock(LunchVoucherMgt lunchVoucherMgt) throws AxelorException{
		
		HRConfig hrConfig = hrConfigService.getHRConfig(lunchVoucherMgt.getCompany());
		int minStoclLV = hrConfig.getMinStockLunchVoucher();
		int totalLV = lunchVoucherMgt.getTotalLunchVouchers();
		int availableStoclLV = hrConfig.getAvailableStockLunchVoucher();
		int stockLine = lunchVoucherMgt.getStockLineQuantity();
		
		return availableStoclLV - totalLV - stockLine - minStoclLV;
	}

}
