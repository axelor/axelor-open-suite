package com.axelor.meta.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaModel;

public class MetaModelRepository extends JpaRepository<MetaModel> {

	public MetaModelRepository() {
		super(MetaModel.class);
	}

	public MetaModel findByName(String name) {
		return Query.of(MetaModel.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

}
