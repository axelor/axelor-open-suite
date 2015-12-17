package com.axelor.csv.script;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import com.axelor.apps.base.db.Company;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class ImportCompany {
	
	@Inject
	MetaFiles metaFiles;
	
	public Object importCompany(Object bean, Map<String,Object> values) {
		
		assert bean instanceof Company;
		
		Company company = (Company) bean;
		
		final Path path = (Path) values.get("__path__");
	    final File image = path.resolve((String) values.get("logo_fileName")).toFile(); 

		try {
			final MetaFile metaFile = metaFiles.upload(image);
			company.setLogo(metaFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bean;
	}

}
