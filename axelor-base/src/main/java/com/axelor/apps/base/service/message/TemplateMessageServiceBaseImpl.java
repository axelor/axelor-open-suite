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
package com.axelor.apps.base.service.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.BirtTemplateParameter;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
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
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;

public class TemplateMessageServiceBaseImpl extends TemplateMessageServiceImpl {

	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	public TemplateMessageServiceBaseImpl(MessageService messageService, EmailAddressRepository emailAddressRepo) {
		super(messageService, emailAddressRepo);
	}

	public Set<MetaFile> getMetaFiles(Template template, Message message) throws AxelorException, IOException {

		Set<MetaFile> metaFiles = super.getMetaFiles(template);
		if ( template.getBirtTemplate() == null ) { return metaFiles; }

		attachBirtTemplate( maker, template.getBirtTemplate() , message);
		
		logger.debug("Metafile to attach: {}", metaFiles);

		return metaFiles;

	}

	public void attachBirtTemplate(TemplateMaker maker, BirtTemplate birtTemplate , Message message) throws AxelorException, IOException {

		logger.debug("Generate birt metafile: {}", birtTemplate.getName());

		String fileName = birtTemplate.getName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		File file = generateBirtTemplate( maker,
				fileName,
				birtTemplate.getTemplateLink(),
				birtTemplate.getFormat(),
				birtTemplate.getBirtTemplateParameterList());

		try (InputStream is = new FileInputStream(file)) {
			Beans.get(MetaFiles.class).attach(is, fileName + "." + birtTemplate.getFormat(), message);
		}

	}

	public File generateBirtTemplate( TemplateMaker maker, String fileName, String modelPath, String format, List<BirtTemplateParameter> birtTemplateParameterList) throws AxelorException {

		if ( modelPath == null || modelPath.isEmpty() ) { return null; }

		ReportSettings reportSettings = ReportFactory.createReport(modelPath, fileName).addFormat(format);
		
		for(BirtTemplateParameter birtTemplateParameter : birtTemplateParameterList)  {
			maker.setTemplate(birtTemplateParameter.getValue());

			try {
				reportSettings.addParam(birtTemplateParameter.getName(), convertValue(birtTemplateParameter.getType(), maker.make()));
			} catch (BirtException e) {
				throw new AxelorException(e, IException.CONFIGURATION_ERROR);
			}
		}

		try {
			return reportSettings.generate().getFile();
		} catch (AxelorException e) {
			throw new AxelorException(e.getCause(), IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.TEMPLATE_MESSAGE_BASE_2));
		}

	}

	private Object convertValue(String type, String value) throws BirtException {

		if ( DesignChoiceConstants.PARAM_TYPE_BOOLEAN.equals( type ) )
		{
			return DataTypeUtil.toBoolean( value );
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_DATETIME.equals( type ) )
		{
			return DataTypeUtil.toDate( value );
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_DATE.equals( type ) )
		{
			return DataTypeUtil.toSqlDate( value );
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_TIME.equals( type ) )
		{
			return DataTypeUtil.toSqlTime( value );
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_DECIMAL.equals( type ) )
		{
			return DataTypeUtil.toBigDecimal( value );
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_FLOAT.equals( type ) )
		{
			return DataTypeUtil.toDouble( value );
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_STRING.equals( type ) )
		{
			return DataTypeUtil.toLocaleNeutralString( value );
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_INTEGER.equals( type ) )
		{
			return DataTypeUtil.toInteger( value );
		}
		return value;

	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Message generateMessage( long objectId, String model, String tag, Template template, MailAccount emailAccount ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, IOException  {
		
		if (!model.equals(template.getMetaModel().getFullName())) {
			throw new AxelorException(IException.INCONSISTENCY, I18n.get(com.axelor.apps.message.exception.IExceptionMessage.TEMPLATE_SERVICE_3), template.getMetaModel().getFullName());
		}
		
		logger.debug("model : {}", model);
		logger.debug("tag : {}", tag);
		logger.debug("object id : {}", objectId);
		logger.debug("template : {}", template);
		
		initMaker(objectId, model, tag);
		
		String content = "", subject = "", from= "", replyToRecipients = "", toRecipients = "", ccRecipients = "", bccRecipients = "", addressBlock= "";
		int mediaTypeSelect;
		
		if ( !Strings.isNullOrEmpty( template.getContent() ) )  {
			maker.setTemplate(template.getContent());
			content = maker.make();
		}
		
		if( !Strings.isNullOrEmpty( template.getAddressBlock() ) )  {
			maker.setTemplate(template.getAddressBlock());
			addressBlock = maker.make();
		}
		
		if ( !Strings.isNullOrEmpty( template.getSubject() ) )  {
			maker.setTemplate(template.getSubject());
			subject = maker.make();
			logger.debug( "Subject ::: {}", subject );
		}
		
		if( !Strings.isNullOrEmpty( template.getFromAdress() ) )  {
			maker.setTemplate(template.getFromAdress());
			from = maker.make();
			logger.debug( "From ::: {}", from );
		}
		
		if( !Strings.isNullOrEmpty( template.getReplyToRecipients() ) )  {
			maker.setTemplate(template.getReplyToRecipients());
			replyToRecipients = maker.make();
			logger.debug( "Reply to ::: {}", replyToRecipients );
		}
		
		if(template.getToRecipients() != null)  {
			maker.setTemplate(template.getToRecipients());
			toRecipients = maker.make();
			logger.debug( "To ::: {}", toRecipients );
		}
		
		if(template.getCcRecipients() != null)  {
			maker.setTemplate(template.getCcRecipients());
			ccRecipients = maker.make();
			logger.debug( "CC ::: {}", ccRecipients );
		}
		
		if(template.getBccRecipients() != null)  {
			maker.setTemplate(template.getBccRecipients());
			bccRecipients = maker.make();
			logger.debug( "BCC ::: {}", bccRecipients );
		}
		
		mediaTypeSelect = this.getMediaTypeSelect(template);
		logger.debug( "Media ::: {}", mediaTypeSelect );
		logger.debug( "Content ::: {}", content );
		
		Message message = messageService.createMessage( model, Long.valueOf(objectId).intValue(), subject,  content, getEmailAddress(from), getEmailAddresses(replyToRecipients),
				getEmailAddresses(toRecipients), getEmailAddresses(ccRecipients), getEmailAddresses(bccRecipients),
				null, addressBlock, mediaTypeSelect, emailAccount );	
		
		message = Beans.get(MessageRepository.class).save(message);
		
		messageService.attachMetaFiles(message, getMetaFiles(template, message));
		
		return message;
	}
}
