package com.axelor.apps.business.project.service;

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
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
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
	
	public List<Map<String,String>> getProjects(User user){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
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
		return dataList;
	}

}
