package com.axelor.apps.business.project.service;

import java.util.List;

import com.axelor.apps.businessproject.db.BusinessFolder;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BusinessFolderService {

	@Inject
	protected SaleOrderServiceSupplychainImpl saleOrderService;

	private int countRecursion = 0;

	public SaleOrder createSaleOrder(BusinessFolder businessFolder) throws AxelorException{

		User user = AuthUtils.getUser();
		SaleOrder saleOrder = saleOrderService.createSaleOrder(user.getActiveCompany());
		saleOrder.setClientPartner(businessFolder.getCustomer());
		saleOrder = saleOrderService.getClientInformations(saleOrder);
		return saleOrder;
	}

	public SaleOrder createSaleOrderFromTemplate(BusinessFolder businessFolder,SaleOrder template) throws AxelorException{
		SaleOrder saleOrder = saleOrderService.createSaleOrder(template);
		if(businessFolder.getCustomer() != null){
			saleOrder.setClientPartner(businessFolder.getCustomer());
			saleOrder = saleOrderService.getClientInformations(saleOrder);
		}
		return saleOrder;
	}

	@Transactional
	public void changeFolders (ProjectTask project){
		List<ProjectTask> projectTaskList = Beans.get(ProjectTaskRepository.class).all().filter("self.taskTypeSelect = 'project' AND self.project.id = ?1", project.getId()).fetch();
		if(projectTaskList != null && !projectTaskList.isEmpty()){
			for (ProjectTask projectTask : projectTaskList) {
				if(countRecursion>50){
					return;
				}
				countRecursion++;
				projectTask.setFolder(project.getFolder());
				Beans.get(ProjectTaskRepository.class).save(projectTask);
				changeFolders(projectTask);
			}
		}
		else{
			Beans.get(ProjectTaskRepository.class).save(project);
		}
	}
}
