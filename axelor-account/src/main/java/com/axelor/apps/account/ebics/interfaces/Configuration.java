/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package com.axelor.apps.account.ebics.interfaces;

import java.util.Locale;

import com.axelor.apps.account.db.EbicsUser;


/**
 * EBICS client application configuration.
 *
 * @author hachani
 *
 */
public interface Configuration {

  /**
   * Returns the root directory of the client application.
   * @return the root directory of the client application.
   */
  public String getRootDirectory();

  /**
   * Returns the default log directory of the application.
   * @return the default log directory
   */
  public String getLogDirectory();

  /**
   * Returns the default log file name of the application
   * @return the default log file name
   */
  public String getLogFileName();

  /**
   * Returns the EBICS configuration file.
   * @return the EBICS configuration file.
   */
  public String getConfigurationFile();

  /**
   * Returns the property value of a given key from
   * the configuration file
   * @param key the given key
   * @return the property value
   */
  public String getProperty(String key);

  /**
   * Returns the directory path of the key store that contains
   * bank and user certificates.
   * @param the ebics user.
   * @return the key store directory of a given user.
   */
  public String getKeystoreDirectory(EbicsUser user);

  /**
   * Returns the directory path that contains the traces
   * XML transfer files.
   * @param user the ebics user
   * @return the transfer trace directory
   */
  public String getTransferTraceDirectory(EbicsUser user);

  /**
   * Returns the object serialization directory.
   * @param user the ebics user
   * @return the object serialization directory.
   */
  public String getSerializationDirectory();

  /**
   * Returns the SSL trusted store directory.
   * @return the SSL trusted store directory.
   */
  public String getSSLTrustedStoreDirectory();

  /**
   * Return the SSL key store directory
   * @return the SSL key store directory
   */
  public String getSSLKeyStoreDirectory();

  /**
   * Returns the SSL bank server certificates.
   * @return the SSL bank server certificates.
   */
  public String getSSLBankCertificates();

  /**
   * Returns the users directory.
   * @return the users directory.
   */
  public String getUsersDirectory();

  /**
   * Returns the Ebics client serialization manager.
   * @return the Ebics client serialization manager.
   */
  public SerializationManager getSerializationManager();

  /**
   * Returns the Ebics client trace manager.
   * @return the Ebics client trace manager.
   */
  public TraceManager getTraceManager();

  /**
   * Returns the letter manager.
   * @return the letter manager.
   */
  //public LetterManager getLetterManager();

  /**
   * Returns the initializations letters directory.
   * @return the initializations letters directory.
   */
  public String getLettersDirectory(EbicsUser user);

  /**
   * Returns the users directory.
   * @return the users directory.
   */
  public String getUserDirectory(EbicsUser user);

  /**
   * Configuration initialization.
   * Creates the necessary directories for the ebics configuration.
   */
  public void init();

  /**
   * Returns the application locale.
   * @return the application locale.
   */
  public Locale getLocale();

  /**
   * Returns the client application signature version
   * @return the signature version
   */
  public String getSignatureVersion();

  /**
   * Returns the client application authentication version
   * @return the authentication version
   */
  public String getAuthenticationVersion();

  /**
   * Returns the client application encryption version
   * @return the encryption version
   */
  public String getEncryptionVersion();

  /**
   * Tells if the client application should keep XML transfer
   * files in the transfer log directory
   * @return True if the client application should not delete
   *         the XML transfer files
   */
  public boolean isTraceEnabled();

  /**
   * Returns if the files to be transferred should be
   * compressed or sent without compression. This can
   * affect the time of data upload especially for big
   * files
   *
   * @return true if the file compression is enabled
   */
  public boolean isCompressionEnabled();

  /**
   * Returns the default revision of sent XML.
   * @return the default revision of sent XML.
   */
  public int getRevision();

  /**
   * Returns the version of the EBICS protocol used by the client.
   * @return the version of the EBICS protocol.
   */
  public String getVersion();
}
