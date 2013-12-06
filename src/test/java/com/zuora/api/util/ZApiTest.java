package com.zuora.api.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.ZuoraServiceStub.Account;
import com.zuora.api.axis2.ZuoraServiceStub.DeleteResult;
import com.zuora.api.axis2.ZuoraServiceStub.ID;
import com.zuora.api.axis2.ZuoraServiceStub.LoginResult;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResult;
import com.zuora.api.axis2.ZuoraServiceStub.SaveResult;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;

public class ZApiTest {

	private ZApi zapi;
	private static Logger logger = LoggerFactory.getLogger(ZApiTest.class);

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

	@Test
	public void createWithError() {

		zapi.zLogin();

		Account account = new Account();
		account.setName("Dummy account");

		SaveResult[] result = zapi.zCreate(new ZObject[] { (ZObject) account });

		// Make sure the result throw an error
		Assert.assertFalse(result[0].getSuccess());

		logger.info("Save Result error message (expected) = " + result[0].getErrors()[0].getMessage());
	}

	@Test
	public void createAndDeleteAccount() {

		zapi.zLogin();

		logger.info("Starting account creation");

		Account account = new Account();

		account.setName("Test Account with API");
		account.setCurrency("USD");
		account.setBillCycleDay(1);
		account.setStatus("Draft");

		SaveResult[] result = zapi.zCreate(new ZObject[] { (ZObject) account });

		// Make sure we created the account
		Assert.assertTrue(result[0].getSuccess());

		String accountId = result[0].getId().getID();
		logger.info("Successfully account in draft status with ID = " + accountId);

		// Delete this account
		DeleteResult[] deleteResult = zapi.zDelete(new String[] { accountId }, "Account");

		// Make sure we deleted this test account
		Assert.assertTrue(deleteResult[0].getSuccess());

		logger.info("Successfully deleted the test account");
	}

	@Test
	public void createThenUpdateAndDeleteAccount() {

		zapi.zLogin();

		logger.info("Starting account creation");

		Account account = new Account();

		account.setName("Test Account with API");
		account.setCurrency("USD");
		account.setBillCycleDay(1);
		account.setStatus("Draft");

		SaveResult[] result = zapi.zCreate(new ZObject[] { (ZObject) account });

		// Make sure we created the account
		Assert.assertTrue(result[0].getSuccess());

		String accountId = result[0].getId().getID();
		logger.info("Successfully account in draft status with ID = " + accountId);
		
		// Update this account
		Account accountToUpdate = new Account();
		
		ID toUpdateID = new ID();
		toUpdateID.setID(accountId);
		
		String newName = "Updated Account with API";
		accountToUpdate.setName(newName);
		accountToUpdate.setId(toUpdateID);
		
		// Update in Zuora
		SaveResult[] updateResults = zapi.zUpdate(new ZObject[] { (ZObject) accountToUpdate });
		
		for (SaveResult updated : updateResults) {
			Assert.assertTrue(updated.getSuccess());
		}
		
		logger.info("Successfully updated the test account");
		
		// Now query Zuora and make sure the name matches
		QueryResult queryResult = zapi.zQuery("SELECT Name FROM Account WHERE Id = '" + accountId + "'");
		
		for (ZObject obj : queryResult.getRecords()) {
			Account acc = (Account) obj;
			Assert.assertEquals(newName, acc.getName());
		}
		
		// Delete this account
		DeleteResult[] deleteResult = zapi.zDelete(new String[] { accountId }, "Account");

		// Make sure we deleted this test account
		Assert.assertTrue(deleteResult[0].getSuccess());

		logger.info("Successfully deleted the test account");
		
	}
}
