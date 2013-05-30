package com.zuora.api.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.zuora.api.axis2.ZuoraServiceStub.LoginResult;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResult;

public class ZApiTest {

	private ZApi zapi;
	
	@Before
	public void setUp() {
		zapi = new ZApi();
	}
	
	@Test
	public void testCreatingZApi() {
		// Test the endpoint
		Assert.assertNotEquals(zapi.getEndpoint(), "");
	}
	
	@Test
	public void testZLogin() {
		LoginResult loginResult = zapi.zLogin();
		Assert.assertTrue(loginResult.getSession() != null);
	}
	
	@Test
	public void testZQuery() {
		zapi.zLogin();
		QueryResult queryResult = zapi.zQuery("SELECT AccountNumber FROM Account WHERE Id = 'dummyId'");
		Assert.assertEquals(queryResult.getSize(), 0);
	}
	
}
