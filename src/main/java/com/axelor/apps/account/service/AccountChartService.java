/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountChart;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.base.db.Company;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class AccountChartService {
	private static final Logger LOG = LoggerFactory.getLogger(AccountChartService.class);
	
	@Inject
	private Injector injector;
	
	public Boolean installAccountChart(AccountChart act, Company company, AccountConfig accountConfig){
		try {
			
			File tempDir = new File(System.getProperty("java.io.tmpdir")+"/chartImport");
			if(!tempDir.exists())
				tempDir.mkdir();
			String chartPath = "/l10n/l10n_"+act.getCountryCode()+"/"+act.getCode()+"/";
			String[] files = new String[]{"chart-config.xml",
										  "account_account.csv",
										  "account_accountEquiv.csv",
										  "account_accountType.csv",
										  "account_fiscalPosition.csv",
										  "account_tax.csv",
										  "account_taxAccount.csv",
										  "account_taxEquiv.csv",
										  "account_taxLine.csv"};
			
			for(String fileName : Arrays.asList(files)){
				File resourceFile = new File(tempDir,fileName);
				String resource = chartPath+fileName;
				if(fileName.equals("chart-config.xml"))
					resource = "/l10n/chart-config.xml";
				LOG.debug("Resource file path: {}",resource);
				InputStream inputStream = this.getClass().getResourceAsStream(resource);
				if(inputStream  == null)
					continue;
				FileOutputStream outputStream;
				outputStream = new FileOutputStream(resourceFile);
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = inputStream.read(bytes)) != -1) 
					outputStream.write(bytes, 0, read);
				outputStream.close();
			}
			HashMap<String,Object> context = new HashMap<String,Object>();
			context.put("_companyId", company.getId());
			importAccountChartData(tempDir.getAbsolutePath()+"/"+"chart-config.xml",tempDir.getAbsolutePath(), context);
			updateChartCompany(act,company,accountConfig);
			FileUtils.deleteDirectory(tempDir);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			TraceBackService.trace(e);
			return false;
		}
		
	}
	
	@Transactional
	public void updateChartCompany(AccountChart act, Company company, AccountConfig accountConfig){
		accountConfig.setHasChartImported(true);
		accountConfig.save();
		act = AccountChart.find(act.getId());
		company = Company.find(company.getId());
		Set<Company> companySet = act.getCompanySet();
		companySet.add(company);
		act.setCompanySet(companySet);
		act.save();
	}
	
	public void importAccountChartData(String configPath, String dataDir,HashMap<String,Object> context) throws IOException {
		CSVImporter importer = new CSVImporter(injector, configPath.toString(), dataDir);
		importer.addListener(new Listener() {
			@Override
			public void handle(Model bean, Exception e) {
			}

			@Override
			public void imported(Model arg0) {
			}

			@Override
			public void imported(Integer total, Integer count) {
				LOG.debug("Total records: {}",total);
				LOG.debug("Success records: {}",count);
			}

		});
		importer.setContext(context);
		importer.run(null);
	}


		
}