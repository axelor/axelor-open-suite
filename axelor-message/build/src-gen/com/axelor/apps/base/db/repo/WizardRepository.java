package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Wizard;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class WizardRepository extends JpaRepository<Wizard> {

	public WizardRepository() {
		super(Wizard.class);
	}

	public Wizard findByCode(String code) {
		return Query.of(Wizard.class)
				.filter("self.code = :code")
				.bind("code", code)
				.fetchOne();
	}

	public Wizard findByName(String name) {
		return Query.of(Wizard.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
