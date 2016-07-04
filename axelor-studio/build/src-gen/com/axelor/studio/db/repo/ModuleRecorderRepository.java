package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.studio.db.ModuleRecorder;

public class ModuleRecorderRepository extends JpaRepository<ModuleRecorder> {

	public ModuleRecorderRepository() {
		super(ModuleRecorder.class);
	}

}
