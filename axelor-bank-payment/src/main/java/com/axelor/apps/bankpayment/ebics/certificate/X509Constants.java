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
package com.axelor.apps.bankpayment.ebics.certificate;

/**
 * X509 certificate constants
 *
 * @author hachani
 */
public interface X509Constants {

  /** Certificates key usage */
  int SIGNATURE_KEY_USAGE = 1;

  int AUTHENTICATION_KEY_USAGE = 2;
  int ENCRYPTION_KEY_USAGE = 3;

  /** Certificate signature algorithm */
  String SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";

  /** EBICS key size */
  int EBICS_KEY_SIZE = 2048;
}
