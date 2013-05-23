package com.axelor.apps.base.service;

import java.util.List;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.sale.service.SalesRuleService;

public abstract class AbstractSalesRuleBatch extends AbstractBatch {

	protected SalesRuleService salesRuleService;
	protected List<Partner> partners;
	
	protected AbstractSalesRuleBatch (SalesRuleService salesRuleService){
		this.salesRuleService = salesRuleService;
		this.partners = initPartners();
	}
	
	protected void updatePartner( Partner partner ){
					
		partner.addBatchSetItem( Batch.find( batch.getId() ) );
		incrementDone();
		
	}
		
	abstract protected List<Partner> initPartners();
	
}
