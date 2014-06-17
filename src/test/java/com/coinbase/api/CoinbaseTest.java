package com.coinbase.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.client.Entity;

import mockit.Capturing;
import mockit.Cascading;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.glassfish.jersey.client.JerseyInvocation;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.coinbase.api.entity.Account;
import com.coinbase.api.entity.Address;
import com.coinbase.api.entity.AddressNode;
import com.coinbase.api.entity.Button;
import com.coinbase.api.entity.Contact;
import com.coinbase.api.entity.Order;
import com.coinbase.api.entity.Quote;
import com.coinbase.api.entity.Response;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.entity.TransactionNode;
import com.coinbase.api.entity.Transfer;
import com.coinbase.api.entity.TransferNode;
import com.coinbase.api.entity.User;
import com.coinbase.api.exception.CoinbaseException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CoinbaseTest {
    
    @Mocked(stubOutClassInitialization = true)
    @Capturing
    @Cascading
    JerseyInvocation.Builder invoker;
    
    private Coinbase cb;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
	cb = new CoinbaseBuilder().build();
	mapper = ObjectMapperProvider.createDefaultMapper();
    }
    
    @Test(expected = CoinbaseException.class)
    public void errorResponse() throws Exception {
	final InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/error.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.post((Entity) any, Response.class); times = 1; result = response;
	}};
	
	cb.createAccount(new Account());
    }
    

    @Test(expected = CoinbaseException.class)
    public void errorsResponse() throws Exception {
	final InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/errors.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.post((Entity) any, Response.class); times = 1; result = response;
	}};

	cb.createAccount(new Account());
    }

    @Test(expected = CoinbaseException.class)
    public void falseSuccessResponse() throws Exception {
	final InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/false_success.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.post((Entity) any, Response.class); times = 1; result = response;
	}};

	cb.createAccount(new Account());
    }
    
    @Test
    public void accounts() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/accounts.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.get(Response.class); times = 1; result = response;
	}};

	Response r = cb.getAccounts();
	List<Account> accounts = r.getAccounts();
	assertEquals(2, accounts.size());

	Account a1 = accounts.get(0);
	assertEquals("536a541fa9393bb3c7000023", a1.getId());
	assertEquals("My Wallet", a1.getName());
	assertEquals(Money.parse("BTC 50"), a1.getBalance());
	assertEquals(Money.parse("USD 500.12"), a1.getNativeBalance());
	assertEquals(DateTime.parse("2014-05-07T08:41:19-07:00"), a1.getCreatedAt());
	assertTrue(a1.isPrimary());
	assertTrue(a1.isActive());
	
	Account a2 = accounts.get(1);
	assertEquals("536a541fa9393bb3c7000034", a2.getId());
	assertEquals("Savings", a2.getName());
	assertEquals(Money.parse("BTC 0"), a2.getBalance());
	assertEquals(Money.parse("USD 0"), a2.getNativeBalance());
	assertEquals(DateTime.parse("2014-05-07T08:50:10-07:00"), a2.getCreatedAt());
	assertFalse(a2.isPrimary());
	assertTrue(a2.isActive());
    }
    
    @Test
    public void users() throws Exception {
	
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/users.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.get(Response.class); times = 1; result = response;
	}};
	
	User user = cb.getUser();

	assertEquals("512db383f8182bd24d000001", user.getId());
	assertEquals("User One", user.getName());
	assertEquals("user1@example.com", user.getEmail());
	assertEquals("Pacific Time (US & Canada)", user.getTimeZone());
	assertEquals("USD", user.getNativeCurrency());
	assertEquals(Money.parse("BTC 49.76"), user.getBalance());
	assertEquals(1, user.getBuyLevel().intValue());
	assertEquals(1, user.getSellLevel().intValue());
	assertEquals(Money.parse("BTC 10"), user.getBuyLimit());
	assertEquals(Money.parse("BTC 100"), user.getSellLimit());	
	
    }
    
    @Test
    public void transaction() throws Exception {
	
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/transaction.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.get(Response.class); times = 1; result = response;
	}};
	
	Transaction t = cb.getTransaction("5018f833f8182b129c00002f");

	assertEquals("5018f833f8182b129c00002f", t.getId());
	assertEquals(DateTime.parse("2012-08-01T02:34:43-07:00"), t.getCreatedAt());
	assertEquals(Money.parse("BTC -1.1"), t.getAmount());
	assertTrue(t.isRequest());
	assertEquals("pending", t.getStatus());
	
	User sender = t.getSender();
	
	assertEquals("5011f33df8182b142400000e", sender.getId());
	assertEquals("User Two", sender.getName());
	assertEquals("user2@example.com", sender.getEmail());
	
	User recipient = t.getRecipient();
	
	assertEquals("5011f33df8182b142400000a", recipient.getId());
	assertEquals("User One", recipient.getName());
	assertEquals("user1@example.com", recipient.getEmail());	
	
    }

    @Test
    public void transactions() throws Exception {

	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/transactions.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.get(Response.class); times = 1; result = response;
	}};
	
	Response r = cb.getTransactions();

	User current_user = r.getCurrentUser();
	assertEquals("5011f33df8182b142400000e", current_user.getId());
	assertEquals("User Two", current_user.getName());
	assertEquals("user2@example.com", current_user.getEmail());

	assertEquals(Money.parse("BTC 50"), r.getBalance());
	assertEquals(Money.parse("USD 500"), r.getNativeBalance());
	assertEquals(2, r.getTotalCount());
	assertEquals(1, r.getNumPages());
	assertEquals(1, r.getCurrentPage());

	List<TransactionNode> txs = r.getTransactions();
	assertEquals(2, txs.size());

	Transaction tx1 = txs.get(0).getTransaction();
	assertEquals("5018f833f8182b129c00002f", tx1.getId());

	Transaction tx2 = txs.get(1).getTransaction();
	assertEquals("5018f833f8182b129c00002e", tx2.getId());
    }

    @Test
    public void transfers() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/transfers.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.get(Response.class); times = 1; result = response;
	}};
	
	Response r = cb.getTransfers();
	
	assertEquals(1, r.getTotalCount());
	assertEquals(1, r.getNumPages());
	assertEquals(1, r.getCurrentPage());
	List<TransferNode> transfers = r.getTransfers();
	assertEquals(1, transfers.size());
	Transfer t = transfers.get(0).getTransfer();
	assertEquals(Transfer.Type.BUY, t.getType());
	assertEquals("QPCUCZHR", t.getCode());
	assertEquals(DateTime.parse("2013-02-27T23:28:18-08:00"), t.getCreatedAt());
	assertEquals(Money.parse("USD 0.14"), t.getFees().get("coinbase"));
	assertEquals(Money.parse("USD 0.15"), t.getFees().get("bank"));
	assertEquals(DateTime.parse("2013-03-05T18:00:00-08:00"), t.getPayoutDate());
	assertEquals("5011f33df8182b142400000e", t.getTransactionId());
	assertEquals(Transfer.Status.PENDING, t.getStatus());
	assertEquals(Money.parse("BTC 1"), t.getBtc());
	assertEquals(Money.parse("USD 13.55"), t.getSubtotal());
	assertEquals(Money.parse("USD 13.84"), t.getTotal());
	assertEquals("Paid for with $13.84 from Test xxxxx3111.", t.getDescription());
    }

    @Test
    public void quote() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/quote.json");
	final Quote quote = mapper.readValue(in, Quote.class);
	
	new NonStrictExpectations() {{
	    invoker.get(Quote.class); times = 1; result = quote;
	}};
	
	Quote q = cb.getBuyQuote(Money.parse("BTC 1"));
	
	assertEquals(Money.parse("USD 10.10"), q.getSubtotal());
	assertEquals(2, q.getFees().size());
	assertEquals(Money.parse("USD 0.10"), q.getFees().get("coinbase"));
	assertEquals(Money.parse("USD 0.15"), q.getFees().get("bank"));
	assertEquals(Money.parse("USD 10.35"), q.getTotal());
    }

    @Test
    public void addresses() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/addresses.json");
	final Response response = mapper.readValue(in, Response.class);
	
	new NonStrictExpectations() {{
	    invoker.get(Response.class); times = 1; result = response;
	}};
	
	Response r = cb.getAddresses();

	assertNull(r.isSuccess());

	List<AddressNode> addresses = r.getAddresses();
	assertEquals(2, addresses.size());
	
	Address a1 = addresses.get(0).getAddress();
	assertEquals("moLxGrqWNcnGq4A8Caq8EGP4n9GUGWanj4", a1.getAddress());
	assertNull(a1.getCallbackUrl());
	assertNull(a1.getLabel());
	assertEquals(DateTime.parse("2013-05-09T23:07:08-07:00"), a1.getCreatedAt());
	
	Address a2 = addresses.get(1).getAddress();
	assertEquals("mwigfecvyG4MZjb6R5jMbmNcs7TkzhUaCj", a2.getAddress());
	assertEquals("http://localhost/callback", a2.getCallbackUrl());
	assertEquals("My Label", a2.getLabel());
	assertEquals(DateTime.parse("2013-05-09T17:50:37-07:00"), a2.getCreatedAt());
    }

    @Test
    public void button() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/button.json");
	final Response response = mapper.readValue(in, Response.class);

	new NonStrictExpectations() {{
		invoker.post((Entity) any, Response.class);
		times = 1;
		result = response;
	}};

	Button buttonParams = new Button();
	buttonParams.setPrice(Money.parse("USD 2"));
	Button button = cb.createButton(buttonParams);

	assertEquals(Money.parse("USD 1.23"), button.getPrice());
	assertEquals("http://www.example.com/my_custom_button_callback", button.getCallbackUrl());
	assertEquals("Order123", button.getCustom());
	assertEquals("Sample description", button.getDescription());
	assertEquals("test", button.getName());
	assertEquals("Pay With Bitcoin", button.getText());
	assertEquals(Button.Style.CUSTOM_LARGE, button.getStyle());
	assertEquals(Button.Type.BUY_NOW, button.getType());
	assertEquals("93865b9cae83706ae59220c013bc0afd", button.getCode());
     }

    @Test
    public void contacts() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/contacts.json");
	final Response response = mapper.readValue(in, Response.class);

	new NonStrictExpectations() {{
		invoker.get(Response.class);
		times = 1;
		result = response;
	}};

	Response r = cb.getContacts();

	Contact c1 = r.getContacts().get(0).getContact();
	Contact c2 = r.getContacts().get(1).getContact();

	assertEquals("user1@example.com", c1.getEmail());
	assertEquals("user2@example.com", c2.getEmail());
    }

    @Test
    public void sell() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/new_sell.json");
	final Response response = mapper.readValue(in, Response.class);

	new NonStrictExpectations() {
	    {
		invoker.post((Entity) any, Response.class);
		times = 1;
		result = response;
	    }
	};

	Transfer t = cb.sell(Money.parse("BTC 1.00"));
	
	assertEquals(Money.parse("USD 13.21"), t.getTotal());
	assertEquals(Money.parse("USD 13.50"), t.getSubtotal());
	assertEquals(Money.parse("BTC 1"), t.getBtc());
	assertEquals(Transfer.Type.SELL, t.getType());
	assertEquals(Transfer.Status.CREATED, t.getStatus());
	assertEquals(Money.parse("USD 0.14"), t.getFees().get("coinbase"));
	assertEquals(Money.parse("USD 0.15"), t.getFees().get("bank"));
	assertEquals("RD2OC8AL", t.getCode());
    }

    @Test
    public void buy() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/new_buy.json");
	final Response response = mapper.readValue(in, Response.class);

	new NonStrictExpectations() {
	    {
		invoker.post((Entity) any, Response.class);
		times = 1;
		result = response;
	    }
	};

	Transfer t = cb.buy(Money.parse("BTC 1.00"));
	
	assertEquals(Money.parse("USD 13.84"), t.getTotal());
	assertEquals(Money.parse("USD 13.55"), t.getSubtotal());
	assertEquals(Money.parse("BTC 1"), t.getBtc());
	assertEquals(Transfer.Type.BUY, t.getType());
	assertEquals(Transfer.Status.CREATED, t.getStatus());
	assertEquals(Money.parse("USD 0.14"), t.getFees().get("coinbase"));
	assertEquals(Money.parse("USD 0.15"), t.getFees().get("bank"));
	assertEquals("6H7GYLXZ", t.getCode());
    }

    @Test
    public void orders() throws Exception {
	InputStream in = CoinbaseSSL.class.getResourceAsStream("/com/coinbase/api/entity/orders.json");
	final Response response = mapper.readValue(in, Response.class);

	new NonStrictExpectations() {
	    {
		invoker.get(Response.class);
		times = 1;
		result = response;
	    }
	};

	Response r = cb.getOrders();
	Order order = r.getOrders().get(0).getOrder();
	Transaction t = order.getTransaction();
	Button b = order.getButton();

	assertEquals("513eb768f12a9cf27400000b", t.getId());
	assertEquals("4cc5eec20cd692f3cdb7fc264a0e1d78b9a7e3d7b862dec1e39cf7e37ababc14", t.getHash());
	assertEquals(Integer.valueOf(1), t.getConfirmations());

	assertEquals(Button.Type.BUY_NOW, b.getType());
	assertEquals("Order #1234", b.getName());
	assertEquals("order description", b.getDescription());
	assertEquals("eec6d08e9e215195a471eae432a49fc7", b.getId());

	assertEquals("mgrmKftH5CeuFBU3THLWuTNKaZoCGJU5jQ", order.getReceiveAddress());
	assertEquals("custom_123", order.getCustom());
	assertEquals(Money.parse("USD 30"), order.getTotalNative());
	assertEquals(Money.parse("BTC 1"), order.getTotalBtc());
	assertEquals(Order.Status.COMPLETED, order.getStatus());
	assertEquals(DateTime.parse("2013-03-11T22:04:37-07:00"), order.getCreatedAt());
	assertEquals("A7C52JQT", order.getId());
    }

}
