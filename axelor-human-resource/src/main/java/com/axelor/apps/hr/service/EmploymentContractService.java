package com.axelor.apps.hr.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportSettings;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.google.inject.persist.Transactional;

public class EmploymentContractService extends EmploymentContractRepository{

	@Transactional
    public int addAmendment( EmploymentContract EmploymentContract ) throws IOException{

    	String
    		url = new ReportSettings(IReport.EMPLYOMENT_CONTRACT, "pdf").addParam("Locale", "fr").addParam("__locale", "fr_FR").addParam("ContractId", EmploymentContract.getId().toString()).getUrl(),
    		filePath = AppSettings.get().get("file.upload.dir"),
    		fileName = EmploymentContract.getFullName() + "_" + EmploymentContract.getEmploymentContractVersion() + "." + ReportSettings.FORMAT_PDF;

    	URLService.fileDownload(url, filePath, fileName);

    	File file = new File(filePath, fileName);
    	MetaFile metaFile = new MetaFile();
    	metaFile.setFileName(fileName);
    	metaFile.setFilePath(fileName);
    	metaFile.setFileType( Files.probeContentType( file.toPath() ) );
    	metaFile.setFileSize( file.length() );

    	MetaAttachment metaAttachment = new MetaAttachment();
    	metaAttachment.setMetaFile(metaFile);
    	metaAttachment.setObjectId( EmploymentContract.getId() );
    	metaAttachment.setObjectName( EmploymentContract.class.getName() );

    	Beans.get(MetaAttachmentRepository.class).save(metaAttachment);

    	int version = EmploymentContract.getEmploymentContractVersion() + 1;
    	EmploymentContract.setEmploymentContractVersion( version );
    	save(EmploymentContract);

    	return version;
	}
}
