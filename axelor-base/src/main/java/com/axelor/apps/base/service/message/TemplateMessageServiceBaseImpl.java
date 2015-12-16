/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.message;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.BirtTemplateParameter;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageServiceImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.tool.template.TemplateMaker;

public class TemplateMessageServiceBaseImpl extends TemplateMessageServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	public TemplateMessageServiceBaseImpl(MessageService messageService, EmailAddressRepository emailAddressRepo) {
		super(messageService, emailAddressRepo);
	}

	@Override
	public Set<MetaFile> getMetaFiles(Template template) throws AxelorException, IOException {

		Set<MetaFile> metaFiles = super.getMetaFiles(template);
		if ( template.getBirtTemplate() == null ) { return metaFiles; }

		MetaFile birtMetaFile = generateMetaFile( maker, template.getBirtTemplate() );
		if ( birtMetaFile == null ) { return metaFiles; }

		metaFiles.add(birtMetaFile);
		logger.debug("Metafile to attach: {}", metaFiles);

		return metaFiles;

	}

	public MetaFile generateMetaFile( TemplateMaker maker, BirtTemplate birtTemplate ) throws AxelorException, IOException {

		logger.debug("Generate birt metafile: {}", birtTemplate.getName());

		File file =  generateFile( maker,
				birtTemplate.getName(),
				birtTemplate.getTemplateLink(),
				birtTemplate.getFormat(),
				birtTemplate.getBirtTemplateParameterList());

		if (file == null) { return null; }

		return Beans.get(MetaFiles.class).upload(file);

	}

	public File generateFile( TemplateMaker maker, String name, String modelPath, String format, List<BirtTemplateParameter> birtTemplateParameterList ) throws AxelorException {

		if ( modelPath == null || modelPath.isEmpty() ) { return null; }

		ReportSettings reportSettings = ReportFactory.createReport(modelPath, name+"-${date}${time}").addFormat(format);
		
		for(BirtTemplateParameter birtTemplateParameter : birtTemplateParameterList)  {
			maker.setTemplate(birtTemplateParameter.getValue());
			reportSettings.addParam(birtTemplateParameter.getName(), maker.make());
		}

		try {
			return reportSettings.generate().getFile();
		} catch (AxelorException e) {
			throw new AxelorException(I18n.get(IExceptionMessage.TEMPLATE_MESSAGE_BASE_2), e, IException.CONFIGURATION_ERROR);
		}

	}

}
