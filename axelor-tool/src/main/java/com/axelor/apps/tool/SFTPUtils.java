/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class SFTPUtils {

  private SFTPUtils() {}

  /**
   * Returns a new session created with given {@code host}, {@code port}, {@code username}, {@code
   * password}, {@code privateKey} and optional {@code passphrase}.
   *
   * @throws JSchException if {@code username} or {@code host} are invalid, or if {@code passphrase}
   *     is not right.
   */
  public static Session createSession(
      String host, int port, String username, String password, String privateKey, String passphrase)
      throws JSchException {
    JSch jsch = new JSch();

    if (privateKey != null) {
      jsch.addIdentity(privateKey, passphrase);
    }

    Session session = jsch.getSession(username, host, port);
    session.setPassword(password);
    session.setConfig("StrictHostKeyChecking", "no");

    return session;
  }

  /**
   * Returns a new session created with given {@code host}, {@code port}, {@code username} and
   * {@code password}.
   *
   * @throws JSchException if {@code username} or {@code host} are invalid.
   */
  public static Session createSession(String host, int port, String username, String password)
      throws JSchException {
    return createSession(host, port, username, password, null, null);
  }

  /**
   * Returns true if session is valid.
   *
   * @throws JSchException if session is already connected.
   */
  public static boolean isValid(Session session) throws JSchException {
    boolean valid;

    session.connect();
    valid = session.isConnected();
    session.disconnect();

    return valid;
  }

  /**
   * Opens a SFTP channel with given {@code session}.
   *
   * @throws JSchException if session is not connected.
   */
  public static ChannelSftp openSftpChannel(Session session) throws JSchException {
    return (ChannelSftp) session.openChannel("sftp");
  }

  /** Returns a list of all files in given directory {@code dir}. */
  @SuppressWarnings("unchecked")
  public static List<LsEntry> getFiles(ChannelSftp channel, String dir) throws SftpException {
    List<LsEntry> files = new ArrayList<>((Vector<LsEntry>) channel.ls(dir));

    files.sort(
        Comparator.comparing((LsEntry file) -> file.getAttrs().getMTime())
            .thenComparing(LsEntry::getFilename));

    return files;
  }

  /**
   * Returns an {@link InputStream} corresponding to given {@code absoluteFilePath} in remote
   * server.
   */
  public static InputStream get(ChannelSftp channel, String absoluteFilePath) throws SftpException {
    return channel.get(absoluteFilePath);
  }

  /** Sends given {@code file} to given {@code absoluteFilePath} in remote server. */
  public static void put(ChannelSftp channel, InputStream file, String absoluteFilePath)
      throws SftpException {
    channel.put(file, absoluteFilePath);
  }

  /** Moves given {@code src} file to given {@code dst} file, in remote server. */
  public static void move(ChannelSftp channel, String src, String dst) throws SftpException {
    channel.rename(src, dst);
  }

  /**
   * Returns true if given {@code file} is a file.
   *
   * <p>Returns false if it is a directory or a link.
   */
  public static boolean isFile(LsEntry file) {
    return !file.getAttrs().isDir() && !file.getAttrs().isLink();
  }
}
