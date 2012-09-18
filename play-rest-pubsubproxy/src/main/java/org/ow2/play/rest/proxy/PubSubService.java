/**
 *
 * Copyright (c) 2012, PetalsLink
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
 *
 */
package org.ow2.play.rest.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;

import org.ow2.play.governance.api.EventGovernance;
import org.ow2.play.governance.api.GovernanceExeption;
import org.ow2.play.governance.api.bean.Topic;
import org.ow2.play.rest.proxy.api.PubSub;
import org.ow2.play.rest.proxy.api.Status;
import org.ow2.play.rest.proxy.api.Topics;
import org.ow2.play.service.registry.api.Constants;
import org.ow2.play.service.registry.api.Registry;
import org.ow2.play.service.registry.api.RegistryException;
import org.petalslink.dsb.notification.client.http.simple.HTTPConsumerClient;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;

/**
 * @author chamerling
 * 
 */
public class PubSubService implements PubSub {

	private static Logger logger = Logger.getLogger(PubSubService.class
			.getName());

	static {
		// WTF?
		Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
				new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
				new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
				new WsnbModelFactoryImpl());
	}

	private String registry;

	private EventGovernance eventGovernance;

	private HTTPConsumerClient client;

	Cache<String, QName> cache;

	public PubSubService() {
		cache = CacheBuilder.newBuilder().maximumSize(1000).build();
	}

	public Response notify(String topic, String payload) {
		logger.fine("Got a notify for topic '" + topic + "'");

		if (payload == null) {
			Status status = new Status();
			status.message = "Empty payload";
			status.type = "Error";
			return Response.ok(status).build();
		}

		Document document = null;
		try {
			document = XMLHelper.createDocumentFromString("<json>" + payload
					+ "</json>");
		} catch (Exception e1) {
			logger.warning("Can not create the XML document from the payload '"
					+ payload + "'");
			return Response.serverError().build();
		}

		QName qname = getTopicName(topic);
		if (qname == null) {
			logger.fine("Topic has not been found in the system '" + topic
					+ "'");
			Status status = new Status();
			status.message = "No such topic";
			status.type = "error";
			return Response.ok(status).build();
		}

		// TODO do it async
		Status result = new Status();
		try {
			logger.fine("Sending the notification to the platform");
			getClient().notify(document, qname);
		} catch (Exception e) {
			result.message = e.getMessage();
			result.type = "error";
			logger.warning("Error while sending the message to the platform : "
					+ e.getMessage());
		}

		return Response.ok(result).build();
	}

	public Response topics() {
		List<Topic> topics = null;
		try {
			topics = getEventGovernance().getTopics();
		} catch (GovernanceExeption e) {
			e.printStackTrace();
		}
		Topics result = new Topics();
		result.topics = topics;
		return Response.ok(result).build();
	}

	public Response topic(final String name) {
		// do it the bad way...

		List<Topic> topics = null;
		try {
			topics = new ArrayList<Topic>(Collections2.filter(
					getEventGovernance().getTopics(), new Predicate<Topic>() {
						@Override
						public boolean apply(Topic t) {
							return t.getName().equalsIgnoreCase(name);
						}
					}));
		} catch (GovernanceExeption e) {
			Status status = new Status();
			status.message = "Internal problem occured";
			status.type = "error";
			return Response.ok(status).build();
		}

		if (topics != null && topics.size() > 0) {
			return Response.ok(topics.get(0)).build();
		} else {
			Status status = new Status();
			status.message = "No such topic";
			status.type = "error";
			return Response.ok(status).build();
		}
	}

	protected synchronized HTTPConsumerClient getClient() throws Exception {
		if (client == null) {
			// get the endpoint at the first call...
			// FIXME : Need to be able to replace it
			client = new HTTPConsumerClient(getDSBConsumerEndpoint());
		}
		return client;
	}

	protected QName getTopicName(final String topic) {
		QName result = null;

		try {
			result = cache.get(topic, new Callable<QName>() {
				@Override
				public QName call() throws Exception {
					logger.fine("Getting topic from governance...");
					QName result = null;

					List<Topic> topics = new ArrayList<Topic>(Collections2
							.filter(getEventGovernance().getTopics(),
									new Predicate<Topic>() {
										@Override
										public boolean apply(Topic t) {
											return t.getName()
													.equalsIgnoreCase(topic);
										}
									}));

					if (topics != null && topics.size() > 0) {
						result = new QName(topics.get(0).getNs(), topics.get(0)
								.getName(), topics.get(0).getPrefix());
					}

					if (result == null) {
						throw new Exception("Null topic");
					}
					return result;
				}
			});
		} catch (ExecutionException e) {
			System.out.println(e.getMessage());
		}

		return result;
	}

	protected synchronized EventGovernance getEventGovernance() {
		if (eventGovernance == null) {
			String url = null;
			try {
				url = org.petalslink.dsb.cxf.CXFHelper.getClientFromFinalURL(
						registry, Registry.class).get(Constants.GOVERNANCE);
			} catch (RegistryException e) {
				e.printStackTrace();
			}
			eventGovernance = org.petalslink.dsb.cxf.CXFHelper
					.getClientFromFinalURL(url, EventGovernance.class);
		}
		return eventGovernance;
	}

	/**
	 * For testing overiding
	 * 
	 * @return
	 * @throws Exception
	 */
	protected final String getDSBConsumerEndpoint() throws Exception {
		String endpoint = null;
		try {
			endpoint = org.petalslink.dsb.cxf.CXFHelper.getClientFromFinalURL(
					registry, Registry.class).get(Constants.DSB_CONSUMER);
			logger.info("Get the endpoint to push notification to " + endpoint);
		} catch (Exception e) {
			logger.warning("Can not get the DSB endpoint address from the registry");
			throw e;
		}
		return endpoint;
	}

	public void setEventGovernance(EventGovernance eventGovernance) {
		this.eventGovernance = eventGovernance;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}
}
