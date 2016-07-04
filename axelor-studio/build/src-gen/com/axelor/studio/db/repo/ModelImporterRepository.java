package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.ModelImporter;

public class ModelImporterRepository extends JpaRepository<ModelImporter> {

	public ModelImporterRepository() {
		super(ModelImporter.class);
	}

}
