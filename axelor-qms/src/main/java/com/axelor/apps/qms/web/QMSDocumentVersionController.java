package com.axelor.apps.qms.web;

import java.util.Collections;

import org.apache.commons.io.FilenameUtils;

import com.axelor.apps.qms.db.QMSDocument;
import com.axelor.apps.qms.db.QMSDocumentVersion;
import com.axelor.apps.qms.db.repo.QMSDocumentRepository;
import com.axelor.common.StringUtils;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class QMSDocumentVersionController {
	private QMSDocumentRepository documentRepository;

	@Inject
	public QMSDocumentVersionController(QMSDocumentRepository documentRepository) {
		this.documentRepository = documentRepository;
	}

	/**
	 * Adjusts the filename of a document version to integrate document reference,
	 * version index and document title, preserving extension.
	 */
	public void setVersionFileName(ActionRequest request, ActionResponse response) {
		QMSDocumentVersion version = request.getContext().asType(QMSDocumentVersion.class);
		QMSDocument document = documentRepository.find(version.getDocument().getId());
		MetaFile versionFile = version.getVersionMetaFile();
		if(versionFile != null && StringUtils.isBlank(versionFile.getFileName()) == false) {
			final String ext = FilenameUtils.getExtension(versionFile.getFileName());
			versionFile.setFileName(StringUtils.stripAccent(String.format("%s-%s %s", document.getReference(), version.getVersionIndex(), document.getTitle()))
					.replaceAll("[^A-Za-z0-9\\-_]+", "_") + "." + ext);
		}
		response.setValues(Collections.singletonMap("versionMetaFile", versionFile));
	}
}
