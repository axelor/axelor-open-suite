package com.axelor.apps.business.project.web;

import java.util.List;

import com.axelor.apps.business.project.exception.IExceptionMessage;
import com.axelor.apps.business.project.service.SaleOrderProjectService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SaleOrderProjectController extends SaleOrderRepository{

	@Inject
	protected SaleOrderProjectService saleOrderProjectService;

	public void generateProject(ActionRequest request, ActionResponse response){
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		saleOrder = this.find(saleOrder.getId());
		ProjectTask project = saleOrderProjectService.generateProject(saleOrder);

		response.setReload(true);
		response.setView(ActionView
				.define("Project")
				.model(ProjectTask.class.getName())
				.add("form", "project-form")
				.param("forceEdit", "true")
				.context("_showRecord", String.valueOf(project.getId())).map());
	}

	public void generateTasks(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		saleOrder = this.find(saleOrder.getId());
		if(saleOrder.getProject() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SALE_ORDER_NO_PROJECT)), IException.CONFIGURATION_ERROR);
		}
		List<Long> listId = saleOrderProjectService.generateTasks(saleOrder);

		response.setReload(true);
		response.setView(ActionView
				.define("Tasks generated")
				.model(ProjectTask.class.getName())
				.add("grid","task-grid")
				.add("form", "task-form")
				.param("forceEdit", "true")
				.domain("self.id in ("+listId+")").map());
	}
}
