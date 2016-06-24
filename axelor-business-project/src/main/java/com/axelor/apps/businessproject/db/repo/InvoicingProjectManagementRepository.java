package com.axelor.apps.businessproject.db.repo;

import javax.persistence.PersistenceException;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.i18n.I18n;

public class InvoicingProjectManagementRepository extends InvoicingProjectRepository {

	
	@Override
	public void remove(InvoicingProject entity){
		
			if (entity.getInvoice() != null){
					throw new PersistenceException(I18n.get("Since the invoice has already been generated, it's impossible to delete this record"));
			}else{
				super.remove(entity);
			}

		
	}
}
