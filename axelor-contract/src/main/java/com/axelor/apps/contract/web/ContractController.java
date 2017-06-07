package com.axelor.apps.contract.web;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

import java.time.LocalDate;

public class ContractController {

	@Inject
	protected ContractService service;

	public void waiting(ActionRequest request, ActionResponse response) {
		try  {
			service.waitingCurrentVersion(JPA.find(Contract.class, request.getContext().asType(Contract.class).getId()), getToDay());
			response.setReload(true);
		} catch(Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	public void waitingNextVersion(ActionRequest request, ActionResponse response) {
		try  {
			service.waitingNextVersion(JPA.find(ContractVersion.class, request.getContext().asType(ContractVersion.class).getId()).getContractNext(), getToDay());
			response.setReload(true);
		} catch(Exception e) {
			String flash = e.toString();
			if (e.getMessage() != null) { flash = e.getMessage(); }
			response.setError(flash);
		}
	}

	public void ongoing(ActionRequest request, ActionResponse response) {
		try  {
			service.ongoingCurrentVersion(JPA.find(Contract.class, request.getContext().asType(Contract.class).getId()), getToDay());
			response.setReload(true);
		} catch(Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	public void invoicing(ActionRequest request, ActionResponse response) {
		try  {
			service.invoicingContract(JPA.find(Contract.class, request.getContext().asType(Contract.class).getId()));
			response.setReload(true);
		} catch(Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	public void terminated(ActionRequest request, ActionResponse response) {
		try  {
			service.terminateContract(JPA.find(Contract.class, request.getContext().asType(Contract.class).getId()), true, getToDay());
			response.setReload(true);
		} catch(Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	public void activeNextVersion(ActionRequest request, ActionResponse response) {
		try  {
			service.activeNextVersion(JPA.find(ContractVersion.class, request.getContext().asType(ContractVersion.class).getId()).getContractNext(), getToDay());
			response.setReload(true);
		} catch(Exception e) {
			String flash = e.toString();
			if (e.getMessage() != null) { flash = e.getMessage(); }
			response.setError(flash);
		}
	}

	public void deleteNextVersion(ActionRequest request, ActionResponse response) {
		final Contract contract = JPA.find(Contract.class, request.getContext().asType(Contract.class).getId());

		JPA.runInTransaction(new Runnable() {

			@Override
			public void run() {
				ContractVersion version = contract.getNextVersion();
				contract.setNextVersion(null);
				Beans.get(ContractVersionRepository.class).remove(version);
				Beans.get(ContractRepository.class).save(contract);
			}
		});

		response.setReload(true);
	}

	private LocalDate getToDay() {
		return Beans.get(AppBaseService.class).getTodayDate();
	}

	public void saveNextVersion(ActionRequest request, ActionResponse response) {
		final ContractVersion version = JPA.find(ContractVersion.class, request.getContext().asType(ContractVersion.class).getId());
		if(version.getContractNext() != null) { return; }

		final Long contractId = Long.valueOf(request.getContext().get("_xContractId").toString());
		JPA.runInTransaction(new Runnable() {
			@Override
			public void run() {
				Contract contract = JPA.find(Contract.class, contractId);
				contract.setNextVersion(version);
				Beans.get(ContractRepository.class).save(contract);
			}
		});

		response.setReload(true);
	}

}
