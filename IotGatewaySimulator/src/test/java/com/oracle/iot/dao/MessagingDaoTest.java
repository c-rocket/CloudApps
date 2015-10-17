package com.oracle.iot.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessagingDaoTest {

	MessagingDao dao = new MessagingDao();

	@Before
	public void setUp() {
		dao.deleteAll();
	}

	@After
	public void tearDown() {
		dao.deleteAll();
	}

	@Test
	public void getPrivateKey() throws Exception {
		// setup
		String id = "Test-123";
		byte[] key = "privateByteSizedKey".getBytes();

		// execute
		dao.savePrivateKey(id, key);
		byte[] actualKey = dao.getPrivateKey(id);

		// assert
		assertEquals(key, actualKey);
	}

	@Test
	public void deletePrivateKey() throws Exception {
		// setup
		String id = "Test-123";
		byte[] key = "privateByteSizedKey".getBytes();

		// execute
		dao.savePrivateKey(id, key);
		dao.deletePrivateKey(id);
		byte[] actualKey = dao.getPrivateKey(id);

		// assert
		assertNull(actualKey);
	}

	@Test
	public void deleteAllPrivateKey() throws Exception {
		// setup
		String id = "Test-123";
		byte[] key = "privateByteSizedKey".getBytes();

		// execute
		dao.savePrivateKey(id, key);
		dao.deleteAll();
		byte[] actualKey = dao.getPrivateKey(id);

		// assert
		assertNull(actualKey);
	}
}
