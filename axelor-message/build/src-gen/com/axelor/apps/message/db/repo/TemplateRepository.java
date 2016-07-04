package com.axelor.apps.message.db.repo;

import com.axelor.apps.message.db.Template;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class TemplateRepository extends JpaRepository<Template> {

	public TemplateRepository() {
		super(Template.class);
	}

	public Template findByName(String name) {
		return Query.of(Template.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
