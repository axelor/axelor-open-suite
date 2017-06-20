package com.axelor.apps.base.service;

import java.util.List;

import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class ModelServiceImpl implements ModelService {

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void massArchive(List<? extends Model> modelList) {
		massSetArchived(modelList, true);
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void massUnarchive(List<? extends Model> modelList) {
		massSetArchived(modelList, false);
	}

	private void massSetArchived(List<? extends Model> modelList, boolean value) {
		for (Model model : modelList) {
			model.setArchived(value);
		}
	}

}
