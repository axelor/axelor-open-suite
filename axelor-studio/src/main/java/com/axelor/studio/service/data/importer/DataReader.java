package com.axelor.studio.service.data.importer;

import com.axelor.meta.db.MetaFile;

public interface DataReader {
	
	boolean initialize(MetaFile input);
	
	String[] read(String key, int index);
	
	int getTotalLines(String key);
	
	String[] getKeys();
}
