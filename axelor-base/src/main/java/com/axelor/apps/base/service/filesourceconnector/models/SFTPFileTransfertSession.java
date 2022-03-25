package com.axelor.apps.base.service.filesourceconnector.models;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.tool.SFTPUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SFTPFileTransfertSession implements FileTransfertSession {

  private Session session;
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int TIME_OUT = 60000;
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
          I18n.get(IExceptionMessage.FILE_TRANSFERT_SESSION_NOT_STARTED));
    }

    try {
      final ChannelSftp channel = SFTPUtils.openSftpChannel(this.session);
      channel.connect(TIME_OUT);
      channel.cd(parameter.getDestinationFolder());
      
      try {
        for (MetaFile file : files) {
          SFTPUtils.put(channel, new FileInputStream(MetaFiles.getPath(file).toFile()), file.getFileName());
        }
      } catch (Exception e) {
        throw e;
      } finally {
        channel.disconnect();
      }

    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.FILE_TRANSFERT_SESSION_UPLOAD_FAILED),
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
          I18n.get(IExceptionMessage.FILE_TRANSFERT_SESSION_NOT_STARTED));
    }
    try {
      final ChannelSftp channel = SFTPUtils.openSftpChannel(this.session);
      channel.connect(TIME_OUT);
      try {
        List<LsEntry> lsEntries = SFTPUtils.getFiles(channel, parameter.getSourceFolder());

        String fileNamingRule =
            parameter.getFileNamingRule() == null ? "" : parameter.getFileNamingRule();
        return lsEntries.stream()
            .filter(
                lsEntry ->
                    lsEntry.getFilename().matches(String.format(".*%s.*", fileNamingRule))
                        && SFTPUtils.isFile(lsEntry))
            .map(
                lsEntry -> {
                  try {
                    InputStream inputStream =
                        SFTPUtils.get(
                            channel,
                            String.format(
                                "%s/%s", parameter.getSourceFolder(), lsEntry.getFilename()));
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
          I18n.get(IExceptionMessage.FILE_TRANSFERT_SESSION_DOWNLOAD_FAILED),
          e.getMessage());
    }
  }

  @Override
  public FileTransfertSession configureSession(FileSourceConnector fileSourceConnector)
      throws AxelorException {
    Objects.requireNonNull(fileSourceConnector);
    log.debug("Starting FTP session using {}", fileSourceConnector);
    if (fileSourceConnector.getHost() == null || fileSourceConnector.getPort() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.FILE_SOURCE_CONNECTOR_CONNECTION_MISSING_FIELDS));
    }
    try {
      this.session =
          SFTPUtils.createSession(
              fileSourceConnector.getHost(),
              fileSourceConnector.getPort(),
              fileSourceConnector.getUsername(),
              fileSourceConnector.getPassword());
      this.session.setTimeout(60000);
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
          I18n.get(IExceptionMessage.FILE_TRANSFERT_SESSION_NOT_CONFIGURED));
    }
    try {
      this.session.connect();
    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }
}
