package com.axelor.meta.db.repo;

import com.axelor.db.JpaRepository;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaSequence;

public class MetaSequenceRepository extends JpaRepository<MetaSequence> {

	public MetaSequenceRepository() {
		super(MetaSequence.class);
	}

	public MetaSequence findByName(String name) {
		return Query.of(MetaSequence.class)
				.filter("self.name = :name")
				.bind("name", name)
				.autoFlush(false)
				.fetchOne();
	}

}
