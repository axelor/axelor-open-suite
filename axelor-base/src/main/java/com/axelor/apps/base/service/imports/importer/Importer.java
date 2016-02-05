/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.imports.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public abstract class Importer {

	private static final File DEFAULT_WORKSPACE = createDefaultWorkspace();
	
	protected Logger log = LoggerFactory.getLogger(getClass());

	private ImportConfiguration configuration;
	private File workspace;

	public void setConfiguration( ImportConfiguration configuration ) { this.configuration = configuration; }

	public void setWorkspace(File workspace) {
		Preconditions.checkArgument(workspace.exists() && workspace.isDirectory());
		this.workspace = workspace;
	}

	public ImportConfiguration getConfiguration() { return this.configuration; }
	public File getWorkspace() { return this.workspace; }

	public Importer init( ImportConfiguration configuration ) {
		return init(configuration, DEFAULT_WORKSPACE);
	}

	public Importer init( ImportConfiguration configuration, File workspace ) {
		setConfiguration(configuration);
		setWorkspace(workspace);
		log.debug("Initialisation de l'import pour la configuration {}", configuration.getName());
		return this;
	}

	public ImportHistory run() throws AxelorException, IOException {

		File 
			bind = MetaFiles.getPath( configuration.getBindMetaFile() ).toFile(), 
			data = MetaFiles.getPath( configuration.getDataMetaFile() ).toFile();

		if (!bind.exists() || !data.exists()) {
			throw new AxelorException(I18n.get(IExceptionMessage.IMPORTER_1), IException.CONFIGURATION_ERROR);
		}

		File workspace = createFinalWorkspace(data);
		ImportHistory importHistory = process( bind.getAbsolutePath(), workspace.getAbsolutePath() );
		deleteFinalWorkspace(workspace);

		log.debug("Import terminé : {}", importHistory.getLog());
		
		return importHistory;
		
	}
	
	abstract protected ImportHistory process( String bind, String data ) throws IOException;
	
	protected void deleteFinalWorkspace( File workspace ) throws IOException {

		if ( workspace.isDirectory() ){ FileUtils.deleteDirectory(workspace); }
		else { workspace.delete(); }
		
	}
	
	protected File createFinalWorkspace( File data ) throws IOException {

		File finalWorkspace =  new File( workspace, computeFinalWorkspaceName(data) );
		finalWorkspace.mkdir();
		
		if ( isZip( data ) ) { unZip( data, finalWorkspace ); }
		else { FileUtils.copyFileToDirectory( data, finalWorkspace ); }
		
		return finalWorkspace;
		
	}
	
	protected String computeFinalWorkspaceName( File data ){ return String.format("%s-%s", Files.getNameWithoutExtension( data.getName() ) , LocalDateTime.now().toString("yyyyMMdd")); }

	protected boolean isZip(File file) { return Files.getFileExtension(file.getName()).equals("zip"); }
	
	protected void unZip(File file, File directory) throws ZipException, IOException {

		File extractFile = null;
		FileOutputStream fileOutputStream = null;
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			try {
				ZipEntry entry = entries.nextElement();
				InputStream entryInputStream = zipFile.getInputStream(entry);
				byte[] buffer = new byte[1024];
				int bytesRead = 0;

				extractFile = new File(directory, entry.getName());
				if (entry.isDirectory()) {
					extractFile.mkdirs();
					continue;
				} else {
					extractFile.getParentFile().mkdirs();
					extractFile.createNewFile();
				}

				fileOutputStream = new FileOutputStream(extractFile);
				while ((bytesRead = entryInputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);
				}

			} catch (IOException ioException) {
				log.error( ioException.getMessage() );
				continue;
			} finally {
				if (fileOutputStream == null) { continue; }
				try { fileOutputStream.close(); } catch (IOException e) { }
			}
		}
		
		zipFile.close();

	}
	
	/**
	 * Ajout d'un nouveau log dans la table des historiques pour la configuration données.
	 * 
	 * @param listener
	 * @return
	 */
	protected ImportHistory addHistory( ImporterListener listener ) {
		
		ImportHistory importHistory = new ImportHistory( AuthUtils.getUser(), configuration.getDataMetaFile() );
		importHistory.setLog( listener.getImportLog() );
		importHistory.setImportConfiguration( configuration );
		

		return importHistory;
		
	}
	
	private static File createDefaultWorkspace(){
		
		File file = Files.createTempDir();
		file.deleteOnExit();
		return file;
		
	}

}
