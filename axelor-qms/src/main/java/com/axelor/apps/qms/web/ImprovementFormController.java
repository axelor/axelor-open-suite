package com.axelor.apps.qms.web;

import com.axelor.apps.qms.db.ImprovementForm;
import com.axelor.apps.qms.db.repo.ImprovementFormRepository;
import com.axelor.apps.qms.service.ImprovementFormService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ImprovementFormController {
	protected ImprovementFormService improvementFormService;
	protected ImprovementFormRepository improvementFormRepository;

	@Inject
	public ImprovementFormController(ImprovementFormService improvementFormService,
			ImprovementFormRepository improvementFormRepository) {
		this.improvementFormService = improvementFormService;
		this.improvementFormRepository = improvementFormRepository;
	}

	public void confirmImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.confirmImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}

	public void assignImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.assignImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}

	public void analyzeImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.analyzeImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}

	public void correctImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.correctImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}

	public void assessImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.assessImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}

	public void closeImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.closeImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}

	public void cancelImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.cancelImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}

	public void reopenImprovementForm(ActionRequest request, ActionResponse response) throws AxelorException {
		ImprovementForm form = request.getContext().asType(ImprovementForm.class);

		improvementFormService.reopenImprovementForm(improvementFormRepository.find(form.getId()));

		response.setReload(true);
	}
}
