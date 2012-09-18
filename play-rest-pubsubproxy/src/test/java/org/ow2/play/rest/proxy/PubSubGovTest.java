package org.ow2.play.rest.proxy;


import junit.framework.TestCase;

public class PubSubGovTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testReal() throws Exception {
		PubSubService service = new PubSubService();
		service.setRegistry("http://46.105.181.221:8080/registry/RegistryService");
		
		service.notify("mytopic", "123");
		
	}

}
