/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ProjectBusinessService extends ProjectService{

	@Inject
	protected AppBusinessProjectService appBusinessProjectService;

	public Project generateProject(SaleOrder saleOrder){
		Project project = this.generateProject(null, saleOrder.getFullName()+"_project", saleOrder.getSalemanUser(), saleOrder.getCompany(), saleOrder.getClientPartner());
		project.setSaleOrder(saleOrder);
		saleOrder.setProject(project);
		return project;
	}

	@Override
	public Project generateProject(Project parentProject,String fullName, User assignedTo, Company company, Partner clientPartner){
		Project project = new Project();
		project.setStatusSelect(ProjectRepository.STATE_PLANNED);
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
		project.setProjInvTypeSelect(ProjectRepository.INVOICING_TYPE_NONE);
		if(parentProject != null){
			project.setProjInvTypeSelect(parentProject.getProjInvTypeSelect());
		}
		Product product = appBusinessProjectService.getAppBusinessProject().getProductInvoicingProject();
		if(product != null){
			project.setProduct(product);
			project.setQty(BigDecimal.ONE);
			project.setPrice(product.getPurchasePrice());
			project.setUnit(product.getUnit());
			project.setExTaxTotal(product.getPurchasePrice());
		}
		return project;
	}

	public Project generate(SaleOrderLine saleOrderLine, Project project){
		Project project1 = this.generateProject(project, saleOrderLine.getFullName(), saleOrderLine.getSaleOrder().getSalemanUser());
		project1.setProduct(saleOrderLine.getProduct());
		project1.setQty(saleOrderLine.getQty());
		project1.setPrice(saleOrderLine.getPrice());
		project1.setUnit(saleOrderLine.getUnit());
		project1.setExTaxTotal(saleOrderLine.getCompanyExTaxTotal());
		saleOrderLine.setProject(project1);
		return project1;
	}

	public Project generateProject(Project project,String fullName, User assignedTo){
		Project project1 = new Project();
		project1.setStatusSelect(ProjectRepository.STATE_PLANNED);
		project1.setProject(project1);
		project1.setName(fullName);
		if(Strings.isNullOrEmpty(fullName)){
			project1.setName(project1.getFullName()+"_task");
		}
		project1.setFullName(project1.getName());
		project1.setAssignedTo(assignedTo);
		project1.setProgress(BigDecimal.ZERO);
		project1.setImputable(true);
		Product product = appBusinessProjectService.getAppBusinessProject().getProductInvoicingProject();
		project1.setProduct(product);
		project1.setQty(BigDecimal.ONE);
		project1.setPrice(product.getPurchasePrice());
		project1.setUnit(product.getUnit());
		project1.setExTaxTotal(product.getPurchasePrice());
		project1.setProjInvTypeSelect(project1.getProjInvTypeSelect());
		return project1;
	}
	
	public void getProjects(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		try{
			User user = AuthUtils.getUser();
			if(user != null){
				List<Project> projectList = Beans.get(ProjectRepository.class).all().filter("self.imputable = true").fetch();
				for (Project project : projectList) {
					if((project.getMembersUserSet() != null && project.getMembersUserSet().contains(user))
							|| user.equals(project.getAssignedTo())){
						Map<String, String> map = new HashMap<String,String>();
						map.put("name", project.getName());
						map.put("id", project.getId().toString());
						dataList.add(map);
					}
				}
			}
			response.setData(dataList);
			response.setTotal(dataList.size());
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
		
	}

}
