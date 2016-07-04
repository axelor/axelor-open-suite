package com.axelor.studio.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.studio.db.FieldType;

public class FieldTypeRepository extends JpaRepository<FieldType> {

	public FieldTypeRepository() {
		super(FieldType.class);
	}

	public FieldType findByName(String name) {
		return Query.of(FieldType.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
