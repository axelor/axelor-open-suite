package com.axelor.apps.marketing.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillSender;
import com.microtripit.mandrillapp.lutung.view.MandrillUserInfo;

import edu.emory.mathcs.backport.java.util.Arrays;

public class TestMandrillApi {

	@Test
	public void testUsersCall() throws MandrillApiError, IOException {
		MandrillApi mandrillApi = new MandrillApi("ogM4Om9GhLWKEy_G1u8t-Q");
		MandrillUserInfo user = mandrillApi.users().info();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println( gson.toJson(user) );
		System.out.println( mandrillApi.users().ping());
		MandrillSender[] senders = mandrillApi.users().senders();
		System.out.println( "Senders :: " + Arrays.asList(senders) );
	}
	
	

}
