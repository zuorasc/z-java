package com.zuora.api.util;

import com.zuora.api.axis2.UnexpectedErrorFault;
import com.zuora.api.axis2.ZuoraServiceStub;
import org.apache.axis2.databinding.types.soapencoding.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import com.zuora.api.axis2.ZuoraServiceStub.SubscribeResult;
import com.zuora.api.axis2.ZuoraServiceStub.SubscribeRequest;
import com.zuora.api.axis2.ZuoraServiceStub.Contact;
import com.zuora.api.axis2.ZuoraServiceStub.PaymentMethod;
import com.zuora.api.axis2.ZuoraServiceStub.Subscription;
import com.zuora.api.axis2.ZuoraServiceStub.SubscriptionData;
import com.zuora.api.axis2.ZuoraServiceStub.RatePlanData;
import com.zuora.api.axis2.ZuoraServiceStub.RatePlan;
import com.zuora.api.axis2.ZuoraServiceStub.ProductRatePlanCharge;
import com.zuora.api.axis2.ZuoraServiceStub.RatePlanChargeData;
import com.zuora.api.axis2.ZuoraServiceStub.RatePlanCharge;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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
	public void testZSubscribe() throws UnexpectedErrorFault, RemoteException, ParseException {
		zapi.zLogin();

		SubscriptionData data = new SubscriptionData();
		data.setSubscription(makeSubscription());
		data.setRatePlanData(makeRatePlanData());

		SubscribeRequest request = new SubscribeRequest();
		request.setAccount(makeAccount());
		request.setBillToContact(makeContact()); //CONDITIONAL
		request.setSoldToContact(makeContact()); //NO
		request.setPaymentMethod(makePaymentMethod()); // NO
		request.setSubscriptionData(data);


		SubscribeResult[] result =  zapi.zSubscribe(new SubscribeRequest[]{  request });
		Assert.assertNotNull(result);
		Assert.assertEquals(result[0].getGatewayResponseCode(), "Approved");
		Assert.assertTrue(result[0].getSuccess());
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

	@Test
	public void zAdvancedQuery() {
		ZApi zapi = new ZApi();

		// This should be called before any other call
		zapi.zLogin();

		QueryResult resultado = zapi.zQuery("select id from invoiceitem");

		// Example on how to do a query
		List result = zapi.zAdvancedQuery("select id from invoiceitem");

		Assert.assertEquals(result.size(), resultado.getSize());

	}


	@Test
	public void filterProduct() {

		ZApi zapi = new ZApi();

		// This should be called before any other call
		zapi.zLogin();

		// Example on how to do a query
		QueryResult result = zapi.zQuery("Select id, titlekey__c from ProductRatePlan where id= '2c92c0f9555351c1015558ac83b36b31'");
	}

	/**
	 * Make account.
	 *
	 * @return the account
	 */
	private Account makeAccount() {
		long time = System.currentTimeMillis();
		Account acc = new Account();
		acc.setAccountNumber("T-" + time); // string
		acc.setBatch("Batch1"); // enum
		acc.setBillCycleDay(1); // int
		acc.setAllowInvoiceEdit(true); // boolean
		acc.setAutoPay(false);
		acc.setCrmId("SFDC-" + time);
		acc.setCurrency("USD"); // standard DB enum
		acc.setCustomerServiceRepName("CSR Dude");
		acc.setName("ACC-" + time);
		acc.setPurchaseOrderNumber("PO-" + time);
		acc.setSalesRepName("Sales Dude");
		acc.setPaymentTerm("Due Upon Receipt");
		acc.setStatus("Draft");
		return acc;
	}

	/**
	 * Make contact.
	 *
	 * @return the contact
	 */
	private Contact makeContact() {
		long time = System.currentTimeMillis();
		Contact con = new Contact();
		con.setFirstName("Firstly" + time);
		con.setLastName("Secondly" + time);
		con.setAddress1("52 Vexford Lane");
		con.setCity("Anaheim");
		con.setState("California");
		con.setCountry("United States");
		con.setPostalCode("92808");
		con.setWorkEmail("contact@test.com");
		con.setWorkPhone("4152225151");
		return con;
	}

	/**
	 * Make payment method.
	 *
	 * @return the payment method
	 */
	private PaymentMethod makePaymentMethod() {
		PaymentMethod pm = new PaymentMethod();
		pm.setType("CreditCard");
		pm.setCreditCardType("Visa");
		pm.setCreditCardAddress1("52 Vexford Lane");
		pm.setCreditCardCity("Anaheim");
		pm.setCreditCardState("California");
		pm.setCreditCardPostalCode("92808");
		pm.setCreditCardCountry("United States");
		pm.setCreditCardHolderName("Firstly Lastly");
		pm.setCreditCardExpirationYear(2017);
		pm.setCreditCardExpirationMonth(12);
		pm.setCreditCardNumber("4111111111111111");
		return pm;
	}

	/**
	 * Make payment method ach.
	 *
	 * @return the payment method
	 */
	private PaymentMethod makePaymentMethodACH() {
		PaymentMethod pm = new PaymentMethod();
		pm.setType("ACH");
		pm.setAchAbaCode("123123123");
		pm.setAchAccountName("testAccountName");
		pm.setAchAccountNumber("23232323232323");
		pm.setAchAccountType("Saving");
		pm.setAchBankName("Test Bank");
		pm.setCreatedDate(Calendar.getInstance());
		return pm;
	}

	/**
	 * Creates a Subscription object reading the values from the property.
	 *
	 * @return Subscription
	 */
	private Subscription makeSubscription() throws ParseException {

		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String dateInDatabaseFormat = sdf.format(date);

		System.out.println("dateInDatabaseFormat: " + dateInDatabaseFormat);


		Subscription sub = new Subscription();
		sub.setName("SUB-" + System.currentTimeMillis());
		sub.setTermStartDate(dateInDatabaseFormat);
		// set ContractEffectiveDate = current date to generate invoice
		// Generates invoice at the time of subscription creation. uncomment for
		// invoice generation
		sub.setContractEffectiveDate(dateInDatabaseFormat);
		sub.setServiceActivationDate(dateInDatabaseFormat);
		// set IsInvoiceSeparate=true //To generate invoice separate for every
		// subscription
		sub.setIsInvoiceSeparate(true);
		sub.setAutoRenew(true);
		sub.setInitialTerm(12);// sets value for next renewal date
		sub.setRenewalTerm(12);
		sub.setNotes("This is a test subscription");
		return sub;
	}

	/**
	 * Make rate plan data.
	 *
	 * @return the rate plan data[]
	 */
	public RatePlanData[] makeRatePlanData() {
		RatePlanData ratePlanData = new RatePlanData();
		RatePlan ratePlan = new RatePlan();

		ID id = new ID();
		id.setID("2c92c0f9552e602201552f62f5145984");
		ratePlan.setProductRatePlanId(id);
		ratePlanData.setRatePlan(ratePlan);

		return new RatePlanData[] { ratePlanData };
	}
}
