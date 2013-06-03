package com.axelor.apps.account.service.invoice;

import java.util.List;

import javax.inject.Inject;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.AbstractSalesRuleBatch;
import com.axelor.apps.sale.service.SalesRuleService;
import com.axelor.db.JPA;

public class InvoicingBlockingBatch extends AbstractSalesRuleBatch {

	@Inject
	public InvoicingBlockingBatch(SalesRuleService salesRuleService) {
		super(salesRuleService);
	}

	@Override
	protected void process() {
		
		for (Partner partner : partners){

			partner = Partner.find( partner.getId() );
			salesRuleService.actionInvoicingBlocking( partner );
			updatePartner( partner );
			JPA.clear();
			
		}
		
	}

	@Override
	protected void stop() {

		String comment = "Compte rendu du blocacge en facturation :\n";
		comment += String.format("\t* %s contrats(s) bloqué(s) en facturation\n", partners.size());
		
		super.stop();
		addComment(comment);
		
		
	}

	@Override
	protected List<Partner> initPartners() {
		return Partner.all().fetch();
	}

}
