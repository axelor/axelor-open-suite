package com.axelor.apps.prestashop.service;

import java.io.IOException;

/**
 *
 * @author www.zydor.pl
 */

public class PrestaShopWebserviceException extends Exception {
    
    public PrestaShopWebserviceException(String massage){
        super(massage);
    }
    
    public PrestaShopWebserviceException(String massage,PSWebServiceClient ws) {
        super(massage + '\n'+ws.getResponseContent());
    }
}