package com.axelor.apps.hr.service.lunch.voucher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
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
		this.getStockQuantityStatus(lunchVoucherMgt);
		calculateTotal(lunchVoucherMgt);
		lunchVoucherMgtRepository.save(lunchVoucherMgt);
	}
	
	@Override
	@Transactional
	public void calculateTotal(LunchVoucherMgt lunchVoucherMgt) {
		int total = 0;
		List<LunchVoucherMgtLine> lunchVoucherMgtLineList = lunchVoucherMgt.getLunchVoucherMgtLineList();
		for (LunchVoucherMgtLine lunchVoucherMgtLine : lunchVoucherMgtLineList) {
			total += lunchVoucherMgtLine.getLunchVoucherNumber();
		}
		lunchVoucherMgt.setTotalLunchVouchers(total+lunchVoucherMgt.getStockLineQuantity());
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

	@Override
	@Transactional
	public void getStockQuantityStatus(LunchVoucherMgt lunchVoucherMgt) throws AxelorException {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(lunchVoucherMgt.getCompany());
		int stockQuantityStatus = hrConfig.getAvailableStockLunchVoucher();
		lunchVoucherMgt.setStockQuantityStatus(stockQuantityStatus);
	}
	
	@Transactional
	public String exportLunchVoucherMgt(LunchVoucherMgt lunchVoucherMgt) throws IOException{
		
		String headers[] = new String[4];
		headers[0] = I18n.get("Company code");
		headers[1] = I18n.get("Lunch Voucher's number");
		headers[2] = I18n.get("Employee");
		headers[3] = I18n.get("Lunch Voucher format");
		
		
		List<String[]> list = new ArrayList<String[]>();
		
		for(LunchVoucherMgtLine lunchVoucherMgtLine : lunchVoucherMgt.getLunchVoucherMgtLineList()){

			String item[] = new String[4];
			item[0] = lunchVoucherMgt.getCompany().getCode();
			item[1] = lunchVoucherMgtLine.getLunchVoucherNumber().toString();
			item[2] = lunchVoucherMgtLine.getEmployee().getName();
			item[3] = lunchVoucherMgtLine.getEmployee().getLunchVoucherFormatSelect().toString();
			
			list.add(item);
		}
		
		String fileName = I18n.get("Lunch Voucher Mgt") + " - " + Beans.get(GeneralService.class).getTodayDateTime().toString() + ".csv";
		String filePath = AppSettings.get().get("file.upload.dir");
		
		new File(filePath).mkdirs();
		CsvTool.csvWriter(filePath, fileName, ';', headers, list);
		
		lunchVoucherMgt.setExported(true);
		lunchVoucherMgt.setExportDate(Beans.get(GeneralService.class).getTodayDate());
		
		lunchVoucherMgtRepository.save(lunchVoucherMgt);
		
		Path path = Paths.get(filePath + System.getProperty("file.separator") +fileName);
		
		try (InputStream is = new FileInputStream(path.toFile())) {
			Beans.get(MetaFiles.class).attach(is, fileName, lunchVoucherMgt);
		}
		
		return filePath + System.getProperty("file.separator") +fileName;
		
		
		
	}

}
