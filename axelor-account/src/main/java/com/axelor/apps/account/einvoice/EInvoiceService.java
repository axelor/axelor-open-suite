package com.axelor.apps.account.einvoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve.*;
import com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp.*;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.i18n.I18n;
import org.apache.commons.lang3.StringUtils;

import javax.xml.ws.BindingProvider;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EInvoiceService {

    private static EInvoiceService instance;
    private static ErpDataExchange port;
    private static String apiKey;

    private final static String TEST_ENDPOINT = "https://testfinance.post.ee:443/finance/erp/";
    private final static String PROD_ENDPOINT = "https://finance.omniva.eu:443/finance/erp/";

    private EInvoiceService() {
    }

    public static synchronized EInvoiceService getInstance() {
        if (instance == null) {
            instance = new EInvoiceService();

            apiKey = System.getProperties().getProperty("einvoice.authkey");
            String environment = System.getProperties().getProperty("einvoice.environment", "test");
            String endpoint = environment.equalsIgnoreCase("prod") ? PROD_ENDPOINT : TEST_ENDPOINT;

            if (StringUtils.stripToNull(apiKey) == null) {
                throw new RuntimeException(I18n.get("einvoice.authkey.not.set"));
            }

            port = new ErpDataExchangeService().getErpDataExchangeSoap11();

            BindingProvider bp = (BindingProvider) port;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        }

        return instance;
    }

    public boolean isPartnerAcceptEinvoice(String reg) {
        CompanyStatusRequestType.RegNumber regNumber = new CompanyStatusRequestType.RegNumber();
        regNumber.setCountryCode("EE");
        regNumber.setValue(reg);

        CompanyStatusRequestType requestType = new CompanyStatusRequestType();
        requestType.setAuthPhrase(apiKey);
        requestType.getRegNumber().add(regNumber);

        CompanyStatusResponseType responseType = port.companyStatus(requestType);
        List<CompanyActiveType> companyActive = responseType.getCompanyActive();

        return companyActive.size() > 0 && companyActive.get(0).getValue().equals("YES");
    }

    public void sendInvoice(com.axelor.apps.account.db.Invoice axelorInvoice) {
        String invoiceId = axelorInvoice.getInvoiceId();
        Integer daysToPay = axelorInvoice.getPaymentCondition().getPaymentTime();
        BigDecimal inTaxTotal = axelorInvoice.getCompanyInTaxTotal();
        BigDecimal exTaxTotal = axelorInvoice.getCompanyExTaxTotal();
        BigDecimal taxTotal = axelorInvoice.getCompanyTaxTotal();
        String currency = axelorInvoice.getCurrency().getCode();

        Partner seller = axelorInvoice.getCompany().getPartner();
        BankDetails bankDetails = axelorInvoice.getCompany().getParent().getBankDetailsList().get(0);

        Partner buyer = axelorInvoice.getPartner();
        String payerName = buyer.getSimpleFullName();
        String payerRegNumber = buyer.getRegistrationCode();
        Partner contactPerson = axelorInvoice.getContactPartner();

        InvoiceParties invoiceParties = invoiceParties(seller, buyer, bankDetails, contactPerson);
        InvoiceInformation invoiceInformation = invoiceInformation(invoiceId, daysToPay);
        InvoiceSumGroup invoiceSumGroup = invoiceSumGroup(exTaxTotal, taxTotal, inTaxTotal, currency);
        InvoiceItem invoiceItem = invoiceItem(axelorInvoice.getInvoiceLineList());
        PaymentInfo paymentInfo = paymentInfo(inTaxTotal, payerName, invoiceId, daysToPay, currency, bankDetails.getIban(), seller.getSimpleFullName());


        Invoice invoice = new Invoice();
        invoice.setInvoiceId(invoiceId);
        invoice.setRegNumber(payerRegNumber);
        invoice.setSellerRegnumber(seller.getRegistrationCode());
        invoice.setInvoiceParties(invoiceParties);
        invoice.setInvoiceInformation(invoiceInformation);
        invoice.getInvoiceSumGroup().add(invoiceSumGroup);
        invoice.setInvoiceItem(invoiceItem);
        invoice.setPaymentInfo(paymentInfo);

        EInvoice eInvoice = new EInvoice();
        eInvoice.setHeader(header(invoiceId));
        eInvoice.getInvoice().add(invoice);
        eInvoice.setFooter(footer(inTaxTotal));

        EInvoiceRequest eInvoiceRequest = new EInvoiceRequest();
        eInvoiceRequest.setAuthPhrase(apiKey);
        eInvoiceRequest.setEInvoice(eInvoice);

        SimpleResponseType simpleResponseType = port.eInvoice(eInvoiceRequest);
    }

    private Header header(String invoiceId) {
        Header header = new Header();
        header.setTest("YES");
        header.setDate(LocalDate.now());
        header.setFileId(invoiceId);
        header.setAppId("EARVE");
        header.setVersion("1.2");

        return header;
    }

    private InvoiceParties invoiceParties(Partner seller, Partner buyer, BankDetails bankDetails, Partner contactPerson) {
        AddressRecord sellerAddressRecord = new AddressRecord();
        sellerAddressRecord.setCity(seller.getMainAddress().getAddressL6());
        sellerAddressRecord.setCountry(seller.getMainAddress().getAddressL7Country().getName());
        sellerAddressRecord.setPostalAddress1(seller.getMainAddress().getAddressL4());

        ContactDataRecord sellerContactDataRecord = new ContactDataRecord();
//        contactDataRecord.setContactName("Kirill Krabu");
        sellerContactDataRecord.setPhoneNumber(seller.getFixedPhone());
        sellerContactDataRecord.setEMailAddress(seller.getEmailAddress().getAddress());
        sellerContactDataRecord.setURL(seller.getWebSite());
        sellerContactDataRecord.setLegalAddress(sellerAddressRecord);

        String iban = bankDetails.getIban();
        String bankName = bankDetails.getBank().getBankName();
        String bic = bankDetails.getBank().getCode();

        AccountDataRecord accountDataRecord = new AccountDataRecord();
        accountDataRecord.setAccountNumber(iban.substring(8));
        accountDataRecord.setBankName(bankName);
        accountDataRecord.setIBAN(iban);
        accountDataRecord.setBIC(bic);

        SellerPartyRecord sellerParty = new SellerPartyRecord();
        sellerParty.setName(seller.getSimpleFullName());
        sellerParty.setRegNumber(seller.getRegistrationCode());
        sellerParty.setVATRegNumber(seller.getTaxNbr());
        sellerParty.setContactData(sellerContactDataRecord);
        sellerParty.getAccountInfo().add(accountDataRecord);

        AddressRecord buyerAddressRecord = new AddressRecord();
        buyerAddressRecord.setCity(buyer.getMainAddress().getAddressL6());
        buyerAddressRecord.setCountry(buyer.getMainAddress().getAddressL7Country().getName());
        buyerAddressRecord.setPostalAddress1(buyer.getMainAddress().getAddressL4());
        ContactDataRecord buyerContactDataRecord = new ContactDataRecord();
        buyerContactDataRecord.setLegalAddress(buyerAddressRecord);

        BillPartyRecord buyerParty = new BillPartyRecord();
        buyerParty.setName(buyer.getSimpleFullName());
        buyerParty.setRegNumber(buyer.getRegistrationCode());
        buyerParty.setContactData(buyerContactDataRecord);
        if(contactPerson != null && contactPerson.getSimpleFullName() != null) {
            buyerContactDataRecord.setContactName(contactPerson.getSimpleFullName());
        }
        if(contactPerson != null && contactPerson.getEmailAddress() != null && contactPerson.getEmailAddress().getAddress() != null) {
            buyerContactDataRecord.setEMailAddress(contactPerson.getEmailAddress().getAddress());
        }

        InvoiceParties invoiceParties = new InvoiceParties();
        invoiceParties.setBuyerParty(buyerParty);
        invoiceParties.setSellerParty(sellerParty);

        return invoiceParties;
    }

    private InvoiceInformation invoiceInformation(String invoiceId, int daysToPay) {
        InvoiceInformation.Type invoiceType = new InvoiceInformation.Type();
        invoiceType.setType("DEB");

        ExtensionRecord extensionRecord = new ExtensionRecord();
        extensionRecord.setExtensionId("eakStatusAfterImport");
        extensionRecord.setInformationContent("SENT");

        InvoiceInformation invoiceInformation = new InvoiceInformation();
        invoiceInformation.setType(invoiceType);
        invoiceInformation.setDocumentName("Arve");
        invoiceInformation.setInvoiceNumber(invoiceId);
        invoiceInformation.setInvoiceDate(LocalDate.now());
        invoiceInformation.setDueDate(LocalDate.now().plus(daysToPay, ChronoUnit.DAYS));
        invoiceInformation.setFineRatePerDay(new BigDecimal("0.05"));
        invoiceInformation.getExtension().add(extensionRecord);

        return invoiceInformation;
    }

    private InvoiceSumGroup invoiceSumGroup(BigDecimal exTax, BigDecimal tax, BigDecimal inTax, String currency) {
        InvoiceSumGroup invoiceSumGroup = new InvoiceSumGroup();
        invoiceSumGroup.setInvoiceSum(exTax);
        invoiceSumGroup.setTotalVATSum(tax);
        invoiceSumGroup.setTotalSum(inTax);
        invoiceSumGroup.setCurrency(currency);

        return invoiceSumGroup;
    }

    private InvoiceItem invoiceItem(List<InvoiceLine> invoiceLineList) {
        InvoiceItemGroup invoiceItemGroup = new InvoiceItemGroup();

        invoiceLineList.stream()
                .map(this::newItem)
                .forEach(e -> invoiceItemGroup.getItemEntry().add(e));

        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.getInvoiceItemGroup().add(invoiceItemGroup);

        return invoiceItem;
    }

    private ItemEntry newItem(InvoiceLine line) {
        String productName = line.getProductName();
        if (line.getSaleOrderLine() != null && line.getSaleOrderLine().getContactPartner() != null) {
            String contactPerson = line.getSaleOrderLine().getContactPartner().getSimpleFullName();
            productName = productName + " (" + contactPerson + ")";
        }
        BigDecimal price = line.getPriceDiscounted();
        BigDecimal qty = line.getQty();
        String units = line.getUnit().getLabelToPrinting();
        BigDecimal taxRate = line.getTaxRate().multiply(BigDecimal.valueOf(100)); // 0.2 -> 20%

        ItemEntry.ItemDetailInfo detailInfo = new ItemEntry.ItemDetailInfo();
        detailInfo.setItemPrice(price);
        detailInfo.setItemUnit(units);
        detailInfo.setItemAmount(qty);

        BigDecimal exTaxSum = line.getExTaxTotal();
        BigDecimal tax = line.getInTaxTotal().subtract(exTaxSum);

        VATRecord vatRecord = new VATRecord();
        vatRecord.setVATRate(taxRate);
        vatRecord.setVATSum(tax);

        ItemEntry itemEntry = new ItemEntry();
        itemEntry.setDescription(productName);
        itemEntry.setItemSum(exTaxSum);
        itemEntry.setVAT(vatRecord);
        itemEntry.getItemDetailInfo().add(detailInfo);

        return itemEntry;
    }

    private PaymentInfo paymentInfo(BigDecimal totalSum, String PayerName, String invoiceId, int daysToPay, String currency, String payToAccount, String payToName) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCurrency(currency);
        paymentInfo.setPaymentDescription("Arve " + invoiceId);
        paymentInfo.setPayable("YES");
        paymentInfo.setPayDueDate(LocalDate.now().plus(daysToPay, ChronoUnit.DAYS));
        paymentInfo.setPaymentTotalSum(totalSum);
        paymentInfo.setPayerName(PayerName);
        paymentInfo.setPaymentId(invoiceId);
        paymentInfo.setPayToAccount(payToAccount);
        paymentInfo.setPayToName(payToName);

        return paymentInfo;
    }

    private Footer footer(BigDecimal totalSum) {
        Footer footer = new Footer();
        footer.setTotalNumberInvoices(BigInteger.ONE);
        footer.setTotalAmount(totalSum);

        return footer;
    }

}
