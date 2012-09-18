package org.ow2.play.rest.proxy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.ow2.play.governance.api.EventGovernance;
import org.ow2.play.governance.api.GovernanceExeption;
import org.ow2.play.governance.api.bean.Topic;
import org.petalslink.dsb.notification.client.http.simple.HTTPConsumerClient;

import junit.framework.TestCase;

public class PubSubTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNotify() throws Exception {
		PubSubService service = new PubSubService() {
			@Override
			protected synchronized HTTPConsumerClient getClient() {
				return new HTTPConsumerClient("http://foo/bar") {
					public void notify(org.w3c.dom.Document payload, QName topic)
							throws org.petalslink.dsb.notification.commons.NotificationException {
						System.out.println("NOP");
					};
				};
			}
		};

		
		service.setEventGovernance(new EventGovernance() {

				@Override
				public void loadResources(InputStream arg0)
						throws GovernanceExeption {
					// TODO Auto-generated method stub

				}

				@Override
				@WebMethod
				public List<Topic> getTopics() throws GovernanceExeption {
					System.out.println("Get topics from mock");
					List<Topic> result = new ArrayList<Topic>();
					Topic t = new Topic();
					t.setName("foo");
					t.setNs("http://foo");
					t.setPrefix("pre");
					result.add(t);
					t = new Topic();
					t.setName("bar");
					t.setNs("http://bar");
					t.setPrefix("pre");
					result.add(t);

					return result;
				}

				@Override
				@WebMethod
				public List<QName> findTopicsByElement(QName arg0)
						throws GovernanceExeption {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				@WebMethod
				public List<W3CEndpointReference> findEventProducersByTopics(
						List<QName> arg0) throws GovernanceExeption {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				@WebMethod
				public List<W3CEndpointReference> findEventProducersByElements(
						List<QName> arg0) throws GovernanceExeption {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				@WebMethod
				public void createTopic(Topic arg0)
						throws GovernanceExeption {
					// TODO Auto-generated method stub

				}
			});
		
		service.notify("foo", "{a : b}");
		service.notify("foo", "{a : b}");
		
	}

}
