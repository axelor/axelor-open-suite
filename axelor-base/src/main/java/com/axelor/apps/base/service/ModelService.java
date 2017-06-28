package com.axelor.apps.base.service;

import java.util.List;

import com.axelor.db.Model;

public interface ModelService {

	void massArchive(List<? extends Model> modelList);

	void massUnarchive(List<? extends Model> modelList);

}
