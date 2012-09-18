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
package org.ow2.play.rest.proxy.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author chamerling
 * 
 */
@Path(value = "pubsub")
public interface PubSub {

	/**
	 * Notify, gets the JSON body and put it in the WSN notify payload
	 * 
	 * @param topic
	 * @return
	 */
	@POST
	@Path("notify/{topic}")
	@Consumes("application/json")
	Response notify(@PathParam("topic") String topic, String payload);

	/**
	 * Get the currently available topics
	 * 
	 * @return
	 */
	@GET
	@Path("/topics/")
	@Produces("application/json")
	Response topics();

	/**
	 * Get a topic information
	 * 
	 * @param name
	 * @return
	 */
	@GET
	@Path("/topic/{name}")
	@Produces("application/json")
	Response topic(@PathParam("name") String name);

}
