package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;

public interface AnalyticDistributionLineService {
	public BigDecimal chooseComputeWay(Context context, AnalyticDistributionLine analyticDistributionLine);
	public BigDecimal computeAmount(AnalyticDistributionLine analyticDistributionLine);
	public List<AnalyticDistributionLine> generateLines(Partner partner, Product product, Company company, BigDecimal total) throws AxelorException;
	public List<AnalyticDistributionLine> generateLinesFromPartner(Partner partner, BigDecimal total);
	public List<AnalyticDistributionLine> generateLinesFromProduct(Product product, Company company, BigDecimal total) throws AxelorException;
	public List<AnalyticDistributionLine> generateLinesWithTemplate(AnalyticDistributionTemplate template, BigDecimal total);
	public boolean validateLines(List<AnalyticDistributionLine> analyticDistributionLineList);
}
