/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ProjectTaskBusinessService extends ProjectTaskService{

	@Inject
	protected GeneralService generalService;

	public ProjectTask generateProject(SaleOrder saleOrder){
		ProjectTask project = this.generateProject(null, saleOrder.getFullName()+"_project", saleOrder.getSalemanUser(), saleOrder.getCompany(), saleOrder.getClientPartner());
		project.setSaleOrder(saleOrder);
		saleOrder.setProject(project);
		return project;
	}

	@Override
	public ProjectTask generateProject(ProjectTask parentProject,String fullName, User assignedTo, Company company, Partner clientPartner){
		ProjectTask project = new ProjectTask();
		project.setTypeSelect(ProjectTaskRepository.TYPE_PROJECT);
		project.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
		project.setProject(parentProject);
		project.setName(fullName);
		if(Strings.isNullOrEmpty(fullName)){
			project.setName("project");
		}
		project.setFullName(project.getName());
		project.setCompany(company);
		project.setClientPartner(clientPartner);
		project.setAssignedTo(assignedTo);
		project.setProgress(BigDecimal.ZERO);
		project.addMembersUserSetItem(assignedTo);
		project.setImputable(true);
		project.setProjTaskInvTypeSelect(ProjectTaskRepository.INVOICING_TYPE_NONE);
		if(parentProject != null){
			project.setProjTaskInvTypeSelect(parentProject.getProjTaskInvTypeSelect());
		}
		Product product = generalService.getGeneral().getProductInvoicingProjectTask();
		if(product != null){
			project.setProduct(product);
			project.setQty(BigDecimal.ONE);
			project.setPrice(product.getPurchasePrice());
			project.setUnit(product.getUnit());
			project.setExTaxTotal(product.getPurchasePrice());
		}
		return project;
	}

	public ProjectTask generateTask(SaleOrderLine saleOrderLine, ProjectTask project){
		ProjectTask task = this.generateTask(project, saleOrderLine.getFullName(), saleOrderLine.getSaleOrder().getSalemanUser());
		task.setProduct(saleOrderLine.getProduct());
		task.setQty(saleOrderLine.getQty());
		task.setPrice(saleOrderLine.getPrice());
		task.setUnit(saleOrderLine.getUnit());
		task.setExTaxTotal(saleOrderLine.getCompanyExTaxTotal());
		saleOrderLine.setProject(task);
		return task;
	}

	@Override
	public ProjectTask generateTask(ProjectTask project,String fullName, User assignedTo){
		ProjectTask task = new ProjectTask();
		task.setTypeSelect(ProjectTaskRepository.TYPE_TASK);
		task.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
		task.setProject(project);
		task.setName(fullName);
		if(Strings.isNullOrEmpty(fullName)){
			task.setName(project.getFullName()+"_task");
		}
		task.setFullName(task.getName());
		task.setAssignedTo(assignedTo);
		task.setProgress(BigDecimal.ZERO);
		task.setImputable(true);
		Product product = generalService.getGeneral().getProductInvoicingProjectTask();
		task.setProduct(product);
		task.setQty(BigDecimal.ONE);
		task.setPrice(product.getPurchasePrice());
		task.setUnit(product.getUnit());
		task.setExTaxTotal(product.getPurchasePrice());
		task.setProjTaskInvTypeSelect(project.getProjTaskInvTypeSelect());
		return task;
	}
	
	public void getProjects(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		try{
			User user = AuthUtils.getUser();
			if(user != null){
				List<ProjectTask> projectTaskList = Beans.get(ProjectTaskRepository.class).all().filter("self.imputable = true").fetch();
				for (ProjectTask projectTask : projectTaskList) {
					if((projectTask.getMembersUserSet() != null && projectTask.getMembersUserSet().contains(user))
							|| user.equals(projectTask.getAssignedTo())){
						Map<String, String> map = new HashMap<String,String>();
						map.put("name", projectTask.getName());
						map.put("id", projectTask.getId().toString());
						dataList.add(map);
					}
				}
			}
			response.setData(dataList);
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
		
	}

}
