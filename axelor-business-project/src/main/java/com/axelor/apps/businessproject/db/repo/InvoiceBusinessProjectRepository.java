package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.db.JPA;
import com.axelor.db.Query;

public class InvoiceBusinessProjectRepository extends InvoiceManagementRepository {

	
	@Override
	public void remove(Invoice entity){
		
		InvoicingProject invoicingProject = Query.of(InvoicingProject.class).filter("self.invoice = ?1", entity).fetchOne();
		
		invoicingProject.setInvoice(null);
		JPA.save(invoicingProject);
		
		
		super.remove(entity);
		
	}
}
