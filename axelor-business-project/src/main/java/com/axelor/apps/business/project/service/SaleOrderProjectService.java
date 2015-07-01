package com.axelor.apps.business.project.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;

public class SaleOrderProjectService extends SaleOrderRepository{

	@Transactional
	public ProjectTask generateProject(SaleOrder saleOrder){
		ProjectTask project = new ProjectTask();
		project.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
		project.setName(saleOrder.getFullName());
		project.setCompany(saleOrder.getCompany());
		project.setCustomer(saleOrder.getClientPartner());
		project.setAssignedTo(saleOrder.getSalemanUser());
		project.setSaleOrder(saleOrder);
		project.setProgress(0);
		project.setImputable(true);
		project.setInvoicingTypeSelect(ProjectTaskRepository.INVOICING_TYPE_NONE);
		project.addMembersSetItem(saleOrder.getSalemanUser());
		project.setProduct(GeneralService.getGeneral().getProductInvoicingProjectTask());
		saleOrder.setProject(project);
		save(saleOrder);
		return project;
	}

	@Transactional
	public List<Long> generateTasks(SaleOrder saleOrder){
		List<Long> listId = new ArrayList<Long>();
		List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
		for (SaleOrderLine saleOrderLine : saleOrderLineList) {
			Product product = saleOrderLine.getProduct();
			if(product.getProductTypeSelect() == ProductRepository.PRODUCT_TYPE_SERVICE && product.getProcurementMethodSelect() == ProductRepository.PROCUREMENT_METHOD_PRODUCE){
				ProjectTask task = new ProjectTask();
				task.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
				task.setProject(saleOrder.getProject());
				task.setName(saleOrderLine.getFullName());
				task.setAssignedTo(saleOrder.getSalemanUser());
				task.setProgress(0);
				task.setImputable(true);
				task.setProduct(GeneralService.getGeneral().getProductInvoicingProjectTask());
				saleOrderLine.setProject(task);
				JPA.save(saleOrderLine);
				listId.add(task.getId());
			}
		}
		return listId;
	}

}
