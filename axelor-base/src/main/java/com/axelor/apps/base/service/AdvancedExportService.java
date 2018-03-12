/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.Context;
import com.axelor.rpc.filter.Filter;
import com.itextpdf.text.DocumentException;

public interface AdvancedExportService {

	public String getTargetField(Context context, MetaField metaField, String targetField, MetaModel parentMetaModel);

	@SuppressWarnings("rawtypes")
	public List<Map> showAdvancedExportData(List<Map<String, Object>> advancedExportLines, MetaModel metaModel, String criteria) throws ClassNotFoundException;

	@SuppressWarnings("rawtypes")
	public MetaFile advancedExportPDF(MetaFile exportFile, List<Map<String, Object>> advancedExportLines,
			List<Map> allFieldDataList, MetaModel metaModel) throws DocumentException, IOException;

	@SuppressWarnings("rawtypes")
	public MetaFile advancedExportExcel(MetaFile exportFile, MetaModel metaModel, List<Map> allFieldDataList,
			List<Map<String, Object>> advancedExportLines) throws IOException;
	
	@SuppressWarnings("rawtypes")
	public MetaFile advancedExportCSV(MetaFile exportFile, MetaModel metaModel, List<Map> allFieldDataList,
			List<Map<String, Object>> advancedExportLines) throws IOException;
	
	public Filter getJpaSecurityFilter(MetaModel metaModel);
}
