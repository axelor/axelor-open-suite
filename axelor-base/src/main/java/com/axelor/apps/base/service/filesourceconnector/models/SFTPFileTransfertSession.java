/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.filesourceconnector.models;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.SFTPUtils;
import com.google.inject.Inject;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFTPFileTransfertSession implements FileTransfertSession {

  private Session session;
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int TIME_OUT = 10000;
  protected MetaFiles metaFiles;

  @Inject
  public SFTPFileTransfertSession(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  public void upload(FileSourceConnectorParameters parameter, List<MetaFile> files)
      throws AxelorException {
    Objects.requireNonNull(parameter);
    Objects.requireNonNull(files);
    log.debug("Uploading with FTP using {} with session {}", parameter, this.session);
    if (this.session == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.FILE_TRANSFERT_SESSION_NOT_STARTED));
    }

    try {
      final ChannelSftp channel = SFTPUtils.openSftpChannel(this.session);
      channel.connect(TIME_OUT);
      channel.cd(parameter.getDestinationFolder());
      SftpProgressMonitor monitor = createMonitor();

      try {
        for (MetaFile file : files) {
          try (FileInputStream inputStream =
              new FileInputStream(MetaFiles.getPath(file).toFile())) {
            SFTPUtils.put(channel, inputStream, file.getFileName(), monitor);
          }
        }
      } finally {
        if (channel != null) {
          channel.disconnect();
        }
      }

    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_TRANSFERT_SESSION_UPLOAD_FAILED),
          e.getMessage());
    }
  }

  @Override
  public List<MetaFile> download(FileSourceConnectorParameters parameter) throws AxelorException {
    Objects.requireNonNull(parameter);
    log.debug("Downloading with FTP using {} session {}", parameter, this.session);
    if (this.session == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.FILE_TRANSFERT_SESSION_NOT_STARTED));
    }
    try {
      final ChannelSftp channel = SFTPUtils.openSftpChannel(this.session);
      channel.connect(TIME_OUT);
      try {
        List<LsEntry> lsEntries = SFTPUtils.getFiles(channel, parameter.getSourceFolder());
        SftpProgressMonitor monitor = createMonitor();
        String fileNamingRule =
            parameter.getFileNamingRule() == null ? "" : parameter.getFileNamingRule();
        return lsEntries.stream()
            .filter(
                lsEntry ->
                    lsEntry.getFilename().matches(String.format(".*%s.*", fileNamingRule))
                        && SFTPUtils.isFile(lsEntry))
            .map(
                lsEntry -> {
                  String absoluthPath =
                      String.format("%s/%s", parameter.getSourceFolder(), lsEntry.getFilename());
                  try (InputStream inputStream = SFTPUtils.get(channel, absoluthPath, monitor)) {
                    return metaFiles.upload(inputStream, lsEntry.getFilename());
                  } catch (Exception e) {
                    TraceBackService.trace(e);
                    return null;
                  }
                })
            .collect(Collectors.toList());

      } finally {
        if (channel != null) {
          channel.disconnect();
        }
      }
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_TRANSFERT_SESSION_DOWNLOAD_FAILED),
          e.getMessage());
    }
  }

  protected SftpProgressMonitor createMonitor() {

    SftpProgressMonitor monitor =
        new SftpProgressMonitor() {

          private long fileSize;
          private String operation;
          private long bytes;
          private String fileName;

          @Override
          public void init(int op, String src, String dest, long max) {
            this.operation = op == SftpProgressMonitor.GET ? "download" : "upload";
            this.fileSize = max;
            this.bytes = 0;
            this.fileName = src;
            log.debug("Starting {} from {} to {}. (Size: {} B)", operation, src, dest, max);
          }

          @Override
          public boolean count(long count) {
            bytes += count;
            log.debug("{} {}/{} ({})", operation, bytes, fileSize, fileName);
            return true;
          }

          @Override
          public void end() {
            log.debug("{} ended", operation);
          }
        };
    return monitor;
  }

  @Override
  public FileTransfertSession configureSession(FileSourceConnector fileSourceConnector)
      throws AxelorException {
    Objects.requireNonNull(fileSourceConnector);
    log.debug("Starting FTP session using {}", fileSourceConnector);
    if (fileSourceConnector.getHost() == null || fileSourceConnector.getPort() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BaseExceptionMessage.FILE_SOURCE_CONNECTOR_CONNECTION_MISSING_FIELDS));
    }

    String privateKeyFileName = null;
    if (fileSourceConnector.getKeyFile() != null) {
      privateKeyFileName = fileSourceConnector.getKeyFile().getFileName();
    }
    try {
      this.session =
          SFTPUtils.createSession(
              fileSourceConnector.getHost(),
              fileSourceConnector.getPort(),
              fileSourceConnector.getUsername(),
              fileSourceConnector.getPassword(),
              privateKeyFileName,
              fileSourceConnector.getKeyFilePassphrase());
      this.session.setTimeout(TIME_OUT);
    } catch (JSchException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
    return this;
  }

  @Override
  public boolean isValid() throws AxelorException {
    log.debug("Checking connection of session {}", session);
    if (session != null) {

      try {
        return SFTPUtils.isValid(session);
      } catch (JSchException e) {
        throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
      }
    }

    return false;
  }

  @Override
  public void disconnect() throws AxelorException {

    if (this.session != null) {
      this.session.disconnect();
    }
  }

  @Override
  public void connect() throws AxelorException {

    if (this.session == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.FILE_TRANSFERT_SESSION_NOT_CONFIGURED));
    }
    try {
      this.session.connect();
    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }
}
