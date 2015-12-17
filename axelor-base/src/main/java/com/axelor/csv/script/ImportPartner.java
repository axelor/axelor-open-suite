package com.axelor.csv.script;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import com.axelor.apps.base.db.Partner;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ImportPartner {
	
	
	@Inject
	MetaFiles metaFiles;
	
	public Object importPartner(Object bean, Map<String,Object> values) {
		
		assert bean instanceof Partner;
		
		Partner partner = (Partner) bean;
		
		final Path path = (Path) values.get("__path__");
	    String fileName = (String) values.get("picture_fileName");
		if(Strings.isNullOrEmpty((fileName)))  {  return bean;  }
		
	    final File image = path.resolve(fileName).toFile(); 

		try {
			final MetaFile metaFile = metaFiles.upload(image);
			partner.setPicture(metaFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bean;
	}

}
