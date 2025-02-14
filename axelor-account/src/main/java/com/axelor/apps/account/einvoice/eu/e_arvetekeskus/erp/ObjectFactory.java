
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.e_arvetekeskus.erp package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _BuyInvoiceExportRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoiceExportRequest");
    private final static QName _BuyInvoiceExportResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoiceExportResponse");
    private final static QName _SaleInvoiceBuyStatusRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "SaleInvoiceBuyStatusRequest");
    private final static QName _SaleInvoiceBuyStatusResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "SaleInvoiceBuyStatusResponse");
    private final static QName _BuyInvoiceRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoiceRequest");
    private final static QName _BuyInvoicesResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoicesResponse");
    private final static QName _BuyInvoiceRegisteredRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoiceRegisteredRequest");
    private final static QName _BuyInvoiceRegisteredResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoiceRegisteredResponse");
    private final static QName _BuyInvoicePaidRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoicePaidRequest");
    private final static QName _BuyInvoicePaidResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "BuyInvoicePaidResponse");
    private final static QName _SaleInvoiceExportRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "SaleInvoiceExportRequest");
    private final static QName _SaleInvoiceExportResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "SaleInvoiceExportResponse");
    private final static QName _CompanyStatusRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "CompanyStatusRequest");
    private final static QName _CompanyStatusResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "CompanyStatusResponse");
    private final static QName _InvoiceAttachmentRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "InvoiceAttachmentRequest");
    private final static QName _InvoiceAttachmentResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "InvoiceAttachmentResponse");
    private final static QName _CostReportExportRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "CostReportExportRequest");
    private final static QName _CostReportExportResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "CostReportExportResponse");
    private final static QName _CostReportAttachmentRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "CostReportAttachmentRequest");
    private final static QName _CostReportAttachmentResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "CostReportAttachmentResponse");
    private final static QName _HasBuyInvoiceRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "HasBuyInvoiceRequest");
    private final static QName _HasBuyInvoiceResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "HasBuyInvoiceResponse");
    private final static QName _PdfAttachment_QNAME = new QName("http://e-arvetekeskus.eu/erp", "PdfAttachment");
    private final static QName _EInvoiceResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "EInvoiceResponse");
    private final static QName _AccountPlanResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "AccountPlanResponse");
    private final static QName _AccountPlanRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "AccountPlanRequest");
    private final static QName _ClientRegistryResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "ClientRegistryResponse");
    private final static QName _ClientRegistryRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "ClientRegistryRequest");
    private final static QName _DimensionRegistryRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "DimensionRegistryRequest");
    private final static QName _RejectConfirmationResponse_QNAME = new QName("http://e-arvetekeskus.eu/erp", "RejectConfirmationResponse");
    private final static QName _RejectConfirmationRequest_QNAME = new QName("http://e-arvetekeskus.eu/erp", "RejectConfirmationRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.e_arvetekeskus.erp
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DimensionRegistryConnectionErrorsType }
     * 
     */
    public DimensionRegistryConnectionErrorsType createDimensionRegistryConnectionErrorsType() {
        return new DimensionRegistryConnectionErrorsType();
    }

    /**
     * Create an instance of {@link CompanyStatusRequestType }
     * 
     */
    public CompanyStatusRequestType createCompanyStatusRequestType() {
        return new CompanyStatusRequestType();
    }

    /**
     * Create an instance of {@link BuyInvoiceReqType }
     * 
     */
    public BuyInvoiceReqType createBuyInvoiceReqType() {
        return new BuyInvoiceReqType();
    }

    /**
     * Create an instance of {@link EInvoiceResponseType }
     * 
     */
    public EInvoiceResponseType createEInvoiceResponseType() {
        return new EInvoiceResponseType();
    }

    /**
     * Create an instance of {@link SaleInvoiceBuyStatusRequestType }
     * 
     */
    public SaleInvoiceBuyStatusRequestType createSaleInvoiceBuyStatusRequestType() {
        return new SaleInvoiceBuyStatusRequestType();
    }

    /**
     * Create an instance of {@link SaleInvoiceBuyStatusResponseType }
     * 
     */
    public SaleInvoiceBuyStatusResponseType createSaleInvoiceBuyStatusResponseType() {
        return new SaleInvoiceBuyStatusResponseType();
    }

    /**
     * Create an instance of {@link EInvoiceRespoonseType }
     * 
     */
    public EInvoiceRespoonseType createEInvoiceRespoonseType() {
        return new EInvoiceRespoonseType();
    }

    /**
     * Create an instance of {@link BuyInvoiceRegisteredReqType }
     * 
     */
    public BuyInvoiceRegisteredReqType createBuyInvoiceRegisteredReqType() {
        return new BuyInvoiceRegisteredReqType();
    }

    /**
     * Create an instance of {@link SimpleResponseType }
     * 
     */
    public SimpleResponseType createSimpleResponseType() {
        return new SimpleResponseType();
    }

    /**
     * Create an instance of {@link BuyInvoicePaidReqType }
     * 
     */
    public BuyInvoicePaidReqType createBuyInvoicePaidReqType() {
        return new BuyInvoicePaidReqType();
    }

    /**
     * Create an instance of {@link SaleInvoiceExportReqType }
     * 
     */
    public SaleInvoiceExportReqType createSaleInvoiceExportReqType() {
        return new SaleInvoiceExportReqType();
    }

    /**
     * Create an instance of {@link CompanyStatusResponseType }
     * 
     */
    public CompanyStatusResponseType createCompanyStatusResponseType() {
        return new CompanyStatusResponseType();
    }

    /**
     * Create an instance of {@link InvoiceAttachmentRequestType }
     * 
     */
    public InvoiceAttachmentRequestType createInvoiceAttachmentRequestType() {
        return new InvoiceAttachmentRequestType();
    }

    /**
     * Create an instance of {@link InvoiceAttachmentResponseType }
     * 
     */
    public InvoiceAttachmentResponseType createInvoiceAttachmentResponseType() {
        return new InvoiceAttachmentResponseType();
    }

    /**
     * Create an instance of {@link CostReportExportRequestType }
     * 
     */
    public CostReportExportRequestType createCostReportExportRequestType() {
        return new CostReportExportRequestType();
    }

    /**
     * Create an instance of {@link CostReportExportResponseType }
     * 
     */
    public CostReportExportResponseType createCostReportExportResponseType() {
        return new CostReportExportResponseType();
    }

    /**
     * Create an instance of {@link CostReportAttachmentRequestType }
     * 
     */
    public CostReportAttachmentRequestType createCostReportAttachmentRequestType() {
        return new CostReportAttachmentRequestType();
    }

    /**
     * Create an instance of {@link CostReportAttachmentResponseType }
     * 
     */
    public CostReportAttachmentResponseType createCostReportAttachmentResponseType() {
        return new CostReportAttachmentResponseType();
    }

    /**
     * Create an instance of {@link BuyInvoicePdfRequest }
     * 
     */
    public BuyInvoicePdfRequest createBuyInvoicePdfRequest() {
        return new BuyInvoicePdfRequest();
    }

    /**
     * Create an instance of {@link BuyInvoicePdfResponse }
     * 
     */
    public BuyInvoicePdfResponse createBuyInvoicePdfResponse() {
        return new BuyInvoicePdfResponse();
    }

    /**
     * Create an instance of {@link Base64FileType }
     * 
     */
    public Base64FileType createBase64FileType() {
        return new Base64FileType();
    }

    /**
     * Create an instance of {@link EInvoiceRequest }
     * 
     */
    public EInvoiceRequest createEInvoiceRequest() {
        return new EInvoiceRequest();
    }

    /**
     * Create an instance of {@link AccountPlanRequestType }
     * 
     */
    public AccountPlanRequestType createAccountPlanRequestType() {
        return new AccountPlanRequestType();
    }

    /**
     * Create an instance of {@link ClientRegistryRequestType }
     * 
     */
    public ClientRegistryRequestType createClientRegistryRequestType() {
        return new ClientRegistryRequestType();
    }

    /**
     * Create an instance of {@link DimensionRegistryResponse }
     * 
     */
    public DimensionRegistryResponse createDimensionRegistryResponse() {
        return new DimensionRegistryResponse();
    }

    /**
     * Create an instance of {@link DimensionRegistryRequestType }
     * 
     */
    public DimensionRegistryRequestType createDimensionRegistryRequestType() {
        return new DimensionRegistryRequestType();
    }

    /**
     * Create an instance of {@link RejectConfirmationRequestType }
     * 
     */
    public RejectConfirmationRequestType createRejectConfirmationRequestType() {
        return new RejectConfirmationRequestType();
    }

    /**
     * Create an instance of {@link RegistryImportRequest }
     * 
     */
    public RegistryImportRequest createRegistryImportRequest() {
        return new RegistryImportRequest();
    }

    /**
     * Create an instance of {@link RegistryImportResponse }
     * 
     */
    public RegistryImportResponse createRegistryImportResponse() {
        return new RegistryImportResponse();
    }

    /**
     * Create an instance of {@link CostReportAttachmentType }
     * 
     */
    public CostReportAttachmentType createCostReportAttachmentType() {
        return new CostReportAttachmentType();
    }

    /**
     * Create an instance of {@link SaleInvoiceBuyStatusType }
     * 
     */
    public SaleInvoiceBuyStatusType createSaleInvoiceBuyStatusType() {
        return new SaleInvoiceBuyStatusType();
    }

    /**
     * Create an instance of {@link RegisteredInvoiceType }
     * 
     */
    public RegisteredInvoiceType createRegisteredInvoiceType() {
        return new RegisteredInvoiceType();
    }

    /**
     * Create an instance of {@link PaidInvoiceType }
     * 
     */
    public PaidInvoiceType createPaidInvoiceType() {
        return new PaidInvoiceType();
    }

    /**
     * Create an instance of {@link InvoiceAttachmentType }
     * 
     */
    public InvoiceAttachmentType createInvoiceAttachmentType() {
        return new InvoiceAttachmentType();
    }

    /**
     * Create an instance of {@link DimensionRegistryConnectionPartType }
     * 
     */
    public DimensionRegistryConnectionPartType createDimensionRegistryConnectionPartType() {
        return new DimensionRegistryConnectionPartType();
    }

    /**
     * Create an instance of {@link CompanyActiveType }
     * 
     */
    public CompanyActiveType createCompanyActiveType() {
        return new CompanyActiveType();
    }

    /**
     * Create an instance of {@link RejectContentType }
     * 
     */
    public RejectContentType createRejectContentType() {
        return new RejectContentType();
    }

    /**
     * Create an instance of {@link DimensionRegistryConnectionErrorsType.ImportError }
     * 
     */
    public DimensionRegistryConnectionErrorsType.ImportError createDimensionRegistryConnectionErrorsTypeImportError() {
        return new DimensionRegistryConnectionErrorsType.ImportError();
    }

    /**
     * Create an instance of {@link CompanyStatusRequestType.RegNumber }
     * 
     */
    public CompanyStatusRequestType.RegNumber createCompanyStatusRequestTypeRegNumber() {
        return new CompanyStatusRequestType.RegNumber();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyInvoiceReqType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoiceExportRequest")
    public JAXBElement<BuyInvoiceReqType> createBuyInvoiceExportRequest(BuyInvoiceReqType value) {
        return new JAXBElement<BuyInvoiceReqType>(_BuyInvoiceExportRequest_QNAME, BuyInvoiceReqType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EInvoiceResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoiceExportResponse")
    public JAXBElement<EInvoiceResponseType> createBuyInvoiceExportResponse(EInvoiceResponseType value) {
        return new JAXBElement<EInvoiceResponseType>(_BuyInvoiceExportResponse_QNAME, EInvoiceResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaleInvoiceBuyStatusRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "SaleInvoiceBuyStatusRequest")
    public JAXBElement<SaleInvoiceBuyStatusRequestType> createSaleInvoiceBuyStatusRequest(SaleInvoiceBuyStatusRequestType value) {
        return new JAXBElement<SaleInvoiceBuyStatusRequestType>(_SaleInvoiceBuyStatusRequest_QNAME, SaleInvoiceBuyStatusRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaleInvoiceBuyStatusResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "SaleInvoiceBuyStatusResponse")
    public JAXBElement<SaleInvoiceBuyStatusResponseType> createSaleInvoiceBuyStatusResponse(SaleInvoiceBuyStatusResponseType value) {
        return new JAXBElement<SaleInvoiceBuyStatusResponseType>(_SaleInvoiceBuyStatusResponse_QNAME, SaleInvoiceBuyStatusResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyInvoiceReqType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoiceRequest")
    public JAXBElement<BuyInvoiceReqType> createBuyInvoiceRequest(BuyInvoiceReqType value) {
        return new JAXBElement<BuyInvoiceReqType>(_BuyInvoiceRequest_QNAME, BuyInvoiceReqType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EInvoiceRespoonseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoicesResponse")
    public JAXBElement<EInvoiceRespoonseType> createBuyInvoicesResponse(EInvoiceRespoonseType value) {
        return new JAXBElement<EInvoiceRespoonseType>(_BuyInvoicesResponse_QNAME, EInvoiceRespoonseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyInvoiceRegisteredReqType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoiceRegisteredRequest")
    public JAXBElement<BuyInvoiceRegisteredReqType> createBuyInvoiceRegisteredRequest(BuyInvoiceRegisteredReqType value) {
        return new JAXBElement<BuyInvoiceRegisteredReqType>(_BuyInvoiceRegisteredRequest_QNAME, BuyInvoiceRegisteredReqType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoiceRegisteredResponse")
    public JAXBElement<SimpleResponseType> createBuyInvoiceRegisteredResponse(SimpleResponseType value) {
        return new JAXBElement<SimpleResponseType>(_BuyInvoiceRegisteredResponse_QNAME, SimpleResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyInvoicePaidReqType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoicePaidRequest")
    public JAXBElement<BuyInvoicePaidReqType> createBuyInvoicePaidRequest(BuyInvoicePaidReqType value) {
        return new JAXBElement<BuyInvoicePaidReqType>(_BuyInvoicePaidRequest_QNAME, BuyInvoicePaidReqType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "BuyInvoicePaidResponse")
    public JAXBElement<SimpleResponseType> createBuyInvoicePaidResponse(SimpleResponseType value) {
        return new JAXBElement<SimpleResponseType>(_BuyInvoicePaidResponse_QNAME, SimpleResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaleInvoiceExportReqType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "SaleInvoiceExportRequest")
    public JAXBElement<SaleInvoiceExportReqType> createSaleInvoiceExportRequest(SaleInvoiceExportReqType value) {
        return new JAXBElement<SaleInvoiceExportReqType>(_SaleInvoiceExportRequest_QNAME, SaleInvoiceExportReqType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EInvoiceRespoonseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "SaleInvoiceExportResponse")
    public JAXBElement<EInvoiceRespoonseType> createSaleInvoiceExportResponse(EInvoiceRespoonseType value) {
        return new JAXBElement<EInvoiceRespoonseType>(_SaleInvoiceExportResponse_QNAME, EInvoiceRespoonseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompanyStatusRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "CompanyStatusRequest")
    public JAXBElement<CompanyStatusRequestType> createCompanyStatusRequest(CompanyStatusRequestType value) {
        return new JAXBElement<CompanyStatusRequestType>(_CompanyStatusRequest_QNAME, CompanyStatusRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompanyStatusResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "CompanyStatusResponse")
    public JAXBElement<CompanyStatusResponseType> createCompanyStatusResponse(CompanyStatusResponseType value) {
        return new JAXBElement<CompanyStatusResponseType>(_CompanyStatusResponse_QNAME, CompanyStatusResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InvoiceAttachmentRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "InvoiceAttachmentRequest")
    public JAXBElement<InvoiceAttachmentRequestType> createInvoiceAttachmentRequest(InvoiceAttachmentRequestType value) {
        return new JAXBElement<InvoiceAttachmentRequestType>(_InvoiceAttachmentRequest_QNAME, InvoiceAttachmentRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InvoiceAttachmentResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "InvoiceAttachmentResponse")
    public JAXBElement<InvoiceAttachmentResponseType> createInvoiceAttachmentResponse(InvoiceAttachmentResponseType value) {
        return new JAXBElement<InvoiceAttachmentResponseType>(_InvoiceAttachmentResponse_QNAME, InvoiceAttachmentResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CostReportExportRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "CostReportExportRequest")
    public JAXBElement<CostReportExportRequestType> createCostReportExportRequest(CostReportExportRequestType value) {
        return new JAXBElement<CostReportExportRequestType>(_CostReportExportRequest_QNAME, CostReportExportRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CostReportExportResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "CostReportExportResponse")
    public JAXBElement<CostReportExportResponseType> createCostReportExportResponse(CostReportExportResponseType value) {
        return new JAXBElement<CostReportExportResponseType>(_CostReportExportResponse_QNAME, CostReportExportResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CostReportAttachmentRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "CostReportAttachmentRequest")
    public JAXBElement<CostReportAttachmentRequestType> createCostReportAttachmentRequest(CostReportAttachmentRequestType value) {
        return new JAXBElement<CostReportAttachmentRequestType>(_CostReportAttachmentRequest_QNAME, CostReportAttachmentRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CostReportAttachmentResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "CostReportAttachmentResponse")
    public JAXBElement<CostReportAttachmentResponseType> createCostReportAttachmentResponse(CostReportAttachmentResponseType value) {
        return new JAXBElement<CostReportAttachmentResponseType>(_CostReportAttachmentResponse_QNAME, CostReportAttachmentResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyInvoiceReqType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "HasBuyInvoiceRequest")
    public JAXBElement<BuyInvoiceReqType> createHasBuyInvoiceRequest(BuyInvoiceReqType value) {
        return new JAXBElement<BuyInvoiceReqType>(_HasBuyInvoiceRequest_QNAME, BuyInvoiceReqType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "HasBuyInvoiceResponse")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createHasBuyInvoiceResponse(String value) {
        return new JAXBElement<String>(_HasBuyInvoiceResponse_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Base64FileType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "PdfAttachment")
    public JAXBElement<Base64FileType> createPdfAttachment(Base64FileType value) {
        return new JAXBElement<Base64FileType>(_PdfAttachment_QNAME, Base64FileType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "EInvoiceResponse")
    public JAXBElement<SimpleResponseType> createEInvoiceResponse(SimpleResponseType value) {
        return new JAXBElement<SimpleResponseType>(_EInvoiceResponse_QNAME, SimpleResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "AccountPlanResponse")
    public JAXBElement<SimpleResponseType> createAccountPlanResponse(SimpleResponseType value) {
        return new JAXBElement<SimpleResponseType>(_AccountPlanResponse_QNAME, SimpleResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AccountPlanRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "AccountPlanRequest")
    public JAXBElement<AccountPlanRequestType> createAccountPlanRequest(AccountPlanRequestType value) {
        return new JAXBElement<AccountPlanRequestType>(_AccountPlanRequest_QNAME, AccountPlanRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "ClientRegistryResponse")
    public JAXBElement<SimpleResponseType> createClientRegistryResponse(SimpleResponseType value) {
        return new JAXBElement<SimpleResponseType>(_ClientRegistryResponse_QNAME, SimpleResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientRegistryRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "ClientRegistryRequest")
    public JAXBElement<ClientRegistryRequestType> createClientRegistryRequest(ClientRegistryRequestType value) {
        return new JAXBElement<ClientRegistryRequestType>(_ClientRegistryRequest_QNAME, ClientRegistryRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DimensionRegistryRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "DimensionRegistryRequest")
    public JAXBElement<DimensionRegistryRequestType> createDimensionRegistryRequest(DimensionRegistryRequestType value) {
        return new JAXBElement<DimensionRegistryRequestType>(_DimensionRegistryRequest_QNAME, DimensionRegistryRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "RejectConfirmationResponse")
    public JAXBElement<SimpleResponseType> createRejectConfirmationResponse(SimpleResponseType value) {
        return new JAXBElement<SimpleResponseType>(_RejectConfirmationResponse_QNAME, SimpleResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RejectConfirmationRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://e-arvetekeskus.eu/erp", name = "RejectConfirmationRequest")
    public JAXBElement<RejectConfirmationRequestType> createRejectConfirmationRequest(RejectConfirmationRequestType value) {
        return new JAXBElement<RejectConfirmationRequestType>(_RejectConfirmationRequest_QNAME, RejectConfirmationRequestType.class, null, value);
    }

}
