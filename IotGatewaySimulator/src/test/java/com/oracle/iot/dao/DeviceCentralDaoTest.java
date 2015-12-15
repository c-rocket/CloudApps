package com.oracle.iot.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
@Ignore("Developer CS Can't seem to connect out to run these tests")
public class DeviceCentralDaoTest {

	@Resource
	DeviceCentralDao dao;

	@Test
	public void getFileNames() throws Exception {
		// setup
		String indusrty = "Oil and Gas";
		String device = createString(
				this.getClass().getClassLoader().getResourceAsStream("deviceLoad/template.properties"));
		String image = createString(this.getClass().getClassLoader().getResourceAsStream("deviceLoad/widget.png"));

		// execute
		List<Map<String, Object>> names = dao.getDeviceNames();
		dao.saveDevice("Test", indusrty, device, image);
		List<Map<String, Object>> actualNames = dao.getDeviceNames();
		dao.deleteByName("Test");

		// assert
		assertNotNull(names);
		assertNotNull(actualNames);
		assertEquals(actualNames.size(), names.size() + 1);
		assertTrue(containsName(actualNames, "Test"));
	}

	private boolean containsName(List<Map<String, Object>> actualNames, String string) {
		for (Map<String, Object> item : actualNames) {
			if (((String) item.get("NAME")).equalsIgnoreCase(string)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void saveDevice() throws Exception {
		// setup
		String indusrty = "Oil and Gas";
		String device = createString(
				this.getClass().getClassLoader().getResourceAsStream("deviceLoad/template.properties"));
		String image = createString(this.getClass().getClassLoader().getResourceAsStream("deviceLoad/widget.png"));

		// execute
		Boolean saved = dao.saveDevice("Test", indusrty, device, image);
		Boolean deleted = dao.deleteByName("Test");

		// assert
		assertTrue(saved);
		assertTrue(deleted);
	}

	@Test
	public void downloadDevice() throws Exception {
		// setup
		String indusrty = "Oil and Gas";
		String device = createString(
				this.getClass().getClassLoader().getResourceAsStream("deviceLoad/template.properties"));
		String image = createString(this.getClass().getClassLoader().getResourceAsStream("deviceLoad/widget.png"));

		// execute
		Boolean saved = dao.saveDevice("Test", indusrty, device, image);
		Map<String, Object> actualDevice = dao.downloadDevice("Test");
		Boolean deleted = dao.deleteByName("Test");

		// assert
		assertTrue(saved);
		assertNotNull(actualDevice);
		assertEquals(actualDevice.get("NAME"), "Test");
		assertEquals(actualDevice.get("DEVICE"), device);
		assertNotNull(actualDevice.get("PICTURE"));
		assertNotNull(actualDevice);
		assertTrue(deleted);
	}

	@Test
	public void updateDevice() throws Exception {
		// setup
		String indusrty = "Oil and Gas";
		String device = createString(
				this.getClass().getClassLoader().getResourceAsStream("deviceLoad/template.properties"));
		String image = createString(this.getClass().getClassLoader().getResourceAsStream("deviceLoad/widget.png"));

		// execute
		Boolean saved = dao.saveDevice("Test", indusrty, device, image);
		Boolean updated = dao.saveDevice("Test", indusrty, device, image);
		Boolean deleted = dao.deleteByName("Test");

		// assert
		assertTrue(saved);
		assertTrue(updated);
		assertTrue(deleted);
	}

	private String createString(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		return writer.toString();
	}
}
