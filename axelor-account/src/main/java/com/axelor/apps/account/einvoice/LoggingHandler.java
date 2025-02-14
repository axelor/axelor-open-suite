package com.axelor.apps.account.einvoice;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import javax.xml.namespace.QName;

import java.util.Set;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    private void logMessage(SOAPMessageContext context) {
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty) {
            System.out.println("Outgoing SOAP message:");
        } else {
            System.out.println("Incoming SOAP message:");
        }

        try {
            SOAPMessage message = context.getMessage();
            message.writeTo(System.out);
            System.out.println("\n");
        } catch (Exception e) {
            System.err.println("Error while logging SOAP message: " + e.getMessage());
        }
    }

    @Override
    public void close(MessageContext context) {}

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
