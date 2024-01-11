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
package com.axelor.apps.bankpayment.ebics.xml;

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

import com.axelor.apps.account.ebics.schema.xmldsig.CanonicalizationMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.DigestMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.ReferenceType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureMethodType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignedInfoType;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformType;
import com.axelor.apps.account.ebics.schema.xmldsig.TransformsType;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.xml.XPathParse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.IgnoreAllErrorHandler;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the SignedInfo element performing signature for signed ebics requests
 *
 * @author hachani
 */
public class SignedInfo extends DefaultEbicsRootElement {

  /**
   * Constructs a new <code>SignedInfo</code> element
   *
   * @param digest the digest value
   */
  public SignedInfo(EbicsUser user, byte[] digest) {
    this.user = user;
    this.digest = digest;
  }

  @Override
  public void build() throws AxelorException {
    CanonicalizationMethodType canonicalizationMethod;
    SignatureMethodType signatureMethod;
    ReferenceType reference;
    TransformsType transforms;
    DigestMethodType digestMethod;
    TransformType transform;
    SignedInfoType signedInfo;

    if (digest == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("digest value cannot be null"));
    }

    transform = EbicsXmlFactory.createTransformType(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
    digestMethod =
        EbicsXmlFactory.createDigestMethodType("http://www.w3.org/2001/04/xmlenc#sha256");
    transforms = EbicsXmlFactory.createTransformsType(new TransformType[] {transform});
    reference =
        EbicsXmlFactory.createReferenceType(
            "#xpointer(//*[@authenticate='true'])", transforms, digestMethod, digest);
    signatureMethod =
        EbicsXmlFactory.createSignatureMethodType(
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
    canonicalizationMethod =
        EbicsXmlFactory.createCanonicalizationMethodType(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
    signedInfo =
        EbicsXmlFactory.createSignedInfoType(
            canonicalizationMethod, signatureMethod, new ReferenceType[] {reference});

    document = EbicsXmlFactory.createSignatureType(signedInfo);
  }

  /**
   * Returns the digest value.
   *
   * @return the digest value.
   */
  public byte[] getDigest() {
    return digest;
  }

  /**
   * Returns the signed info element as an <code>XmlObject</code>
   *
   * @return he signed info element
   * @throws EbicsException user Signature and Canonicalization errors
   */
  public SignatureType getSignatureType() {
    return ((SignatureType) document);
  }

  /**
   * Canonizes and signs a given input with the authentication private key. of the EBICS user.
   *
   * <p>The given input to be signed is first Canonized using the
   * http://www.w3.org/TR/2001/REC-xml-c14n-20010315 algorithm.
   *
   * <p>The element to be canonized is only the SignedInfo element that should be contained in the
   * request to be signed. Otherwise, a {@link TransformationException} is thrown.
   *
   * <p>The namespace of the SignedInfo element should be named <b>ds</b> as specified in the EBICS
   * specification for common namespaces nomination.
   *
   * <p>The signature is ensured using the user X002 private key. This step is done in {@link
   * EbicsUser#authenticate(byte[]) authenticate}.
   *
   * @param toSign the input to sign
   * @return the signed input
   * @throws EbicsException signature fails.
   */
  public byte[] sign(byte[] toSign) throws AxelorException {
    try {
      DocumentBuilderFactory factory;
      DocumentBuilder builder;
      Document document;
      Node node;
      Canonicalizer canonicalizer;
      ByteArrayOutputStream output;

      factory = Beans.get(XPathParse.class).getDocumentBuilderFactory();
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      builder = factory.newDocumentBuilder();
      builder.setErrorHandler(new IgnoreAllErrorHandler());
      document = builder.parse(new ByteArrayInputStream(toSign));
      node = XPathAPI.selectSingleNode(document, "//ds:SignedInfo");
      canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
      output = new ByteArrayOutputStream();

      canonicalizer.canonicalizeSubtree(node, output);
      return Beans.get(EbicsUserService.class).authenticate(user, output.toByteArray());
    } catch (Exception e) {
      e.printStackTrace();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public byte[] toByteArray() {
    addNamespaceDecl(XMLConstants.DEFAULT_NS_PREFIX, "http://www.ebics.org/H003");
    setSaveSuggestedPrefixes("http://www.w3.org/2000/09/xmldsig#", "ds");

    return super.toByteArray();
  }

  @Override
  public String getName() {
    return "SignedInfo.xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[] digest;
  private EbicsUser user;
}
