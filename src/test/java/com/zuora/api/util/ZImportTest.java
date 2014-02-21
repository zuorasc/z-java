package com.zuora.api.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.ZuoraServiceStub.Account;
import com.zuora.api.axis2.ZuoraServiceStub.Contact;
import com.zuora.api.axis2.ZuoraServiceStub.DeleteResult;
import com.zuora.api.axis2.ZuoraServiceStub.ID;
import com.zuora.api.axis2.ZuoraServiceStub.SaveResult;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;

public class ZImportTest {

	private ZApi zapi;

	private static Logger logger = LoggerFactory.getLogger(ZImportTest.class);

	private static final String ACCOUNT_NUMBER = "A66666666";
	private static final String FILE_USAGE_NAME = "sample-usage-1.csv";

	@Before
	public void setUp() {
		zapi = new ZApi();
		zapi.zLogin();
	}

	@Test
	public void testImportUsage() {

		// Need to first create an account that will receive the usage record
		logger.debug("Creating account with number = " + ACCOUNT_NUMBER);

		Account account = new Account();
		account.setName("Test Account for usage through API");
		account.setAccountNumber(ACCOUNT_NUMBER);
		account.setCurrency("USD");
		account.setBillCycleDay(1);
		account.setStatus("Draft");
		account.setPaymentTerm("Due Upon Receipt");
		account.setBatch("Batch1");

		SaveResult[] result = zapi.zCreate(new ZObject[] { account });

		// Make sure we created the account
		Assert.assertTrue(result[0].getSuccess());

		ID accountId = result[0].getId();
		logger.info("Successfully account in draft status with ID = " + accountId.getID());
		
		// We need to add a bill to/sold to contact and then activate the account
		Contact contact = new Contact();
		contact.setAccountId(accountId);
		contact.setFirstName("Mickael");
		contact.setLastName("Pham");
		contact.setCountry("United States");
		contact.setState("California");
		
		result = zapi.zCreate(new ZObject[] { contact });
		Assert.assertTrue(result[0].getSuccess());
		
		ID contactId = result[0].getId();
		
		// Update the account to set it active
		account.setId(accountId);
		account.setBillToId(contactId);
		account.setSoldToId(contactId);
		account.setStatus("Active");
		
		result = zapi.zUpdate(new ZObject[] { account });
		Assert.assertTrue(result[0].getSuccess());

		// Now prepare the usage to import and make sure it's created
		ID usageId = ZImport
				.createImport(ZImportTest.class.getResourceAsStream("/" + FILE_USAGE_NAME), FILE_USAGE_NAME);
		Assert.assertNotNull(usageId);

		// Delete this account
		DeleteResult[] deleteResult = zapi.zDelete(new String[] { accountId.getID() }, "Account");

		// Make sure we deleted this test account
		Assert.assertTrue(deleteResult[0].getSuccess());

		logger.info("Successfully deleted the test account");
	}

}
