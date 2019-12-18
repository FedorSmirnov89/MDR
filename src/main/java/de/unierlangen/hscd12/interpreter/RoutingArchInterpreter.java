package de.unierlangen.hscd12.interpreter;

import java.util.HashSet;
import java.util.Set;

import de.unierlangen.hscd12.model.TaskParamPropertyService;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Routings;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.Models.DirectedLink;
import net.sf.opendse.model.properties.TaskPropertyService;
import properties.LinkInvasicPropertyService;
import properties.ResourceInvasicPropertyService;
import properties.RouterPropertyService;

import static net.sf.opendse.encoding.interpreter.InterpreterVariable.copy;

/**
 * The routing arch interpreter interprets the routing and the architecture
 * based on the chosen mappings.
 * 
 * @author Fedor Smirnov
 *
 */
public class RoutingArchInterpreter {

	/**
	 * Fills the provided architecture and routing graph according to the mapping
	 * choices made for the tasks of the application.
	 * 
	 * @param implArch
	 *            the implementation architecture (empty)
	 * @param implRoutings
	 *            the implementation routings (empty)
	 * @param implAppl
	 *            the implementation application (decoded)
	 * @param specArch
	 *            the specification architecture
	 */
	public void interpretArchitectureRouting(Architecture<Resource, Link> implArch,
			Routings<Task, Resource, Link> implRoutings, Application<Task, Dependency> implAppl,
			Architecture<Resource, Link> specArch) {
		// create an overspecified implArch
		makeOverspecifiedArch(specArch, implArch);
		// create the routings
		createRoutings(implArch, implAppl, implRoutings);
		// remove the unused stuff from the arch
		removeUnused(implArch, implRoutings, specArch);
	}

	/**
	 * Removes the unused elements from the architecture.
	 * 
	 * @param implArch
	 *            the architecture
	 * @param implRoutings
	 *            the routings
	 * @param specArch
	 *            the specification architecture
	 */
	protected void removeUnused(Architecture<Resource, Link> implArch, Routings<Task, Resource, Link> implRoutings,
			Architecture<Resource, Link> specArch) {
		// gather the resources and links used in the routings
		Set<Resource> usedRes = new HashSet<>();
		Set<Link> usedLinks = new HashSet<>();
		for (Architecture<Resource, Link> routing : implRoutings.getRoutings()) {
			for (Resource res : routing) {
				usedRes.add(res);
				Set<Resource> sameTileResources = getSameTileResources(res, specArch);
				usedRes.addAll(sameTileResources);
				usedLinks.addAll(getLinksBetweenOnTileResources(sameTileResources, specArch));
			}
			for (Link link : routing.getEdges()) {
				usedLinks.add(link);
			}
		}
		// remove everything that is not used
		Set<Resource> toRemove = new HashSet<>();
		Set<Link> toRemoveL = new HashSet<>();
		for (Resource res : implArch) {
			if (!usedRes.contains(res)) {
				toRemove.add(res);
			}
		}
		for (Link link : implArch.getEdges()) {
			if (!usedLinks.contains(link)) {
				toRemoveL.add(link);
			}
		}
		for (Link l : toRemoveL) {
			implArch.removeEdge(l);
		}
		for (Resource res : toRemove) {
			implArch.removeVertex(res);
		}
	}

	/**
	 * Returns all the resources on the same tile as the given used resource
	 * 
	 * @param usedResource
	 *            the used resource
	 * @param specArch
	 *            the specification architecture
	 * @return all the resources on the same tile
	 */
	protected Set<Resource> getSameTileResources(Resource usedResource, Architecture<Resource, Link> specArch) {
		Set<Resource> sameTileResources = new HashSet<>();
		String routerId = ResourceInvasicPropertyService.getRouterId(usedResource);
		for (Resource res : specArch) {
			if (ResourceInvasicPropertyService.getRouterId(res).equals(routerId)) {
				sameTileResources.add(res);
			}
		}
		return sameTileResources;
	}

	/**
	 * Returns the links between the given resources which are on the same tile.
	 * 
	 * @param onTileResources
	 *            the resources on the tile
	 * @param specArch
	 *            the specification architectures
	 * @return the links between the given resources which are on the same tile
	 */
	protected Set<Link> getLinksBetweenOnTileResources(Set<Resource> onTileResources,
			Architecture<Resource, Link> specArch) {
		Set<Link> tileLinks = new HashSet<>();
		for (Resource res : onTileResources) {
			for (Link link : specArch.getIncidentEdges(res)) {
				Resource other = specArch.getOpposite(res, link);
				if (onTileResources.contains(other)) {
					tileLinks.add(link);
				}
			}
		}
		return tileLinks;
	}

	/**
	 * Creates the implementation routings.
	 * 
	 * @param implArch
	 *            the overspecified implementation architecture
	 * @param implAppl
	 *            the implementation application
	 * @return the implementation routings
	 */
	protected void createRoutings(Architecture<Resource, Link> implArch, Application<Task, Dependency> implAppl,
			Routings<Task, Resource, Link> result) {
		// iterate all communications
		for (Task task : implAppl) {
			if (TaskPropertyService.isCommunication(task)) {
				Architecture<Resource, Link> routing = new Architecture<>();
				Task predecessor = implAppl.getPredecessors(task).iterator().next();
				Task successor = implAppl.getSuccessors(task).iterator().next();

				Resource source = implArch.getVertex(TaskParamPropertyService.getActiveResource(predecessor));
				Resource destination = implArch.getVertex(TaskParamPropertyService.getActiveResource(successor));
				if (source.equals(destination)) {
					// the case where src equals dest
					if (implArch.getVertex(source) == null) {
						Resource archRes = copy(source);
						implArch.addVertex(archRes);
					}
					Resource routingRes = copy(implArch.getVertex(source));
					routing.addVertex(routingRes);
				} else {
					Set<DirectedLink> routingLinks = new HashSet<>();
					// get the routers
					Resource sourceRouter = getRouter(source, implArch);
					Resource destRouter = getRouter(destination, implArch);
					if (sourceRouter.equals(destRouter)) {
						// the case where src and dest are on the same tile
						routingLinks.addAll(getRouteOnTile(source, destination, implArch));
					} else {
						// the case with communication between tiles
						Set<DirectedLink> toSourceRouter = getRouterRoute(source, true, implArch);
						Set<DirectedLink> betweenRouters = getRouteBetweenRouters(sourceRouter, destRouter, implArch);
						Set<DirectedLink> fromDestRouter = getRouterRoute(destination, false, implArch);

						routingLinks.addAll(toSourceRouter);
						routingLinks.addAll(betweenRouters);
						routingLinks.addAll(fromDestRouter);
					}
					// create the routing
					for (DirectedLink dLink : routingLinks) {
						// fix the allocation
						if (!implArch.containsEdge(dLink.getLink())) {
							implArch.addEdge(dLink.getLink(), dLink.getSource(), dLink.getDest(), EdgeType.UNDIRECTED);
						}
						routing.addEdge(copy(implArch.getEdge(dLink.getLink())),
								copy(implArch.getVertex(dLink.getSource())), copy(implArch.getVertex(dLink.getDest())),
								EdgeType.DIRECTED);
					}
				}
				result.set(task, routing);
			}
		}
	}

	/**
	 * Creates an impl arch that is contains every element from the specArch, but is
	 * its child
	 * 
	 * @param specArch
	 *            the specification architecture
	 * @param result
	 *            the implementation architecture
	 */
	protected void makeOverspecifiedArch(Architecture<Resource, Link> specArch, Architecture<Resource, Link> result) {
		// copy the vertices
		for (Resource res : specArch) {
			Resource copy = copy(res);
			result.addVertex(copy);
		}
		// copy the edges
		for (Link l : specArch.getEdges()) {
			Link copy = copy(l);
			Resource src = result.getVertex(specArch.getEndpoints(l).getFirst());
			Resource dest = result.getVertex(specArch.getEndpoints(l).getSecond());
			result.addEdge(copy, src, dest, EdgeType.UNDIRECTED);
		}
	}

	protected DirectedLink copyDirLink(DirectedLink original) {
		return new DirectedLink(copy(original.getLink()), copy(original.getSource()), copy(original.getDest()));
	}

	/**
	 * Returns the directed links that build the route between the two given tile
	 * resources.
	 * 
	 * @param tileResource1
	 *            the src tile resource
	 * @param tileResource2
	 *            the dest tile resource
	 * @return the directed links that build the route between the two given tile
	 *         resources
	 */
	protected Set<DirectedLink> getRouteOnTile(Resource tileResource1, Resource tileResource2,
			Architecture<Resource, Link> implArch) {
		Set<DirectedLink> result = new HashSet<>();
		Link busLink = implArch.getIncidentEdges(tileResource1).iterator().next();
		Resource bus = implArch.getOpposite(tileResource1, busLink);
		Link otherBusLink = null;
		for (Link l : implArch.getIncidentEdges(bus)) {
			if (implArch.getOpposite(bus, l).equals(tileResource2)) {
				otherBusLink = l;
			}
		}
		result.add(copyDirLink(new DirectedLink(busLink, tileResource1, bus)));
		result.add(copyDirLink(new DirectedLink(otherBusLink, bus, tileResource2)));
		return result;
	}

	/**
	 * Returns the directed links that build the route between the two given routers
	 * 
	 * @param router1
	 *            the source router
	 * @param router2
	 *            the destination router
	 * @return the directed links that build the route between the two given routers
	 */
	protected Set<DirectedLink> getRouteBetweenRouters(Resource router1, Resource router2,
			Architecture<Resource, Link> specArch) {
		Set<DirectedLink> route = new HashSet<>();
		// get the coordinates
		int xCoordSrc = RouterPropertyService.getXposition(router1);
		int yCoordSrc = RouterPropertyService.getYposition(router1);
		int xCoordDest = RouterPropertyService.getXposition(router2);
		int yCoordDest = RouterPropertyService.getYposition(router2);
		// get the links forming the XY route
		int xDist = xCoordDest - xCoordSrc;
		int yDist = yCoordDest - yCoordSrc;
		Resource curRes = router1;
		while (xDist != 0) {
			boolean goingRight = xDist > 0;
			int curX = RouterPropertyService.getXposition(curRes);
			int curY = RouterPropertyService.getYposition(curRes);
			for (Link l : specArch.getIncidentEdges(curRes)) {
				if (ResourceInvasicPropertyService.isRouter(specArch.getOpposite(curRes, l))) {
					Resource other = specArch.getOpposite(curRes, l);
					int otherX = RouterPropertyService.getXposition(other);
					int otherY = RouterPropertyService.getYposition(other);
					boolean rightRes = (otherY == curY) && (goingRight ? (otherX == curX + 1) : (otherX == curX - 1));
					if (rightRes) {
						route.add(copyDirLink(new DirectedLink(l, curRes, other)));
						curRes = other;
						if (goingRight) {
							xDist--;
						} else {
							xDist++;
						}
						break;
					}
				}
			}
		}
		while (yDist != 0) {
			boolean goingUp = yDist > 0;
			int curX = RouterPropertyService.getXposition(curRes);
			int curY = RouterPropertyService.getYposition(curRes);
			for (Link l : specArch.getIncidentEdges(curRes)) {
				if (ResourceInvasicPropertyService.isRouter(specArch.getOpposite(curRes, l))) {
					Resource other = specArch.getOpposite(curRes, l);
					int otherX = RouterPropertyService.getXposition(other);
					int otherY = RouterPropertyService.getYposition(other);
					boolean rightRes = (otherX == curX) && (goingUp ? (otherY == curY + 1) : (otherY == curY - 1));
					if (rightRes) {
						route.add(copyDirLink(new DirectedLink(l, curRes, other)));
						curRes = other;
						if (goingUp) {
							yDist--;
						} else {
							yDist++;
						}
						break;
					}
				}
			}
		}
		return route;
	}

	/**
	 * Returns the directed links that build the route between the given tile
	 * resource and its router
	 * 
	 * @param tileResource
	 *            the {@link Resource} on the tile
	 * @param towardsRouter
	 *            {@code true} if the route goes from the resource to the router
	 * @return the directed links that built the route between the given tile
	 *         resource and its router
	 */
	protected Set<DirectedLink> getRouterRoute(Resource tileResource, boolean towardsRouter,
			Architecture<Resource, Link> implArch) {
		Set<DirectedLink> result = new HashSet<>();
		Link busLink = implArch.getIncidentEdges(tileResource).iterator().next();
		Resource bus = implArch.getOpposite(tileResource, busLink);
		Resource router = null;
		Link routerLink = null;
		for (Link l : implArch.getIncidentEdges(bus)) {
			if (LinkInvasicPropertyService.isNocLink(l)) {
				router = implArch.getOpposite(bus, l);
				routerLink = l;
			}
		}
		if (router == null) {
			throw new IllegalArgumentException("No router found for resource " + tileResource.getId());
		}
		result.add(towardsRouter ? copyDirLink(new DirectedLink(busLink, tileResource, bus))
				: copyDirLink(new DirectedLink(busLink, bus, tileResource)));
		result.add(towardsRouter ? copyDirLink(new DirectedLink(routerLink, bus, router))
				: copyDirLink(new DirectedLink(routerLink, router, bus)));
		return result;
	}

	/**
	 * Returns the router of the given tile resource
	 * 
	 * @param tileResource
	 *            the resource on the tile
	 * @param implArch
	 *            the architecture graph
	 * @return the router
	 */
	protected Resource getRouter(Resource tileResource, Architecture<Resource, Link> implArch) {
		Resource bus = implArch.getOpposite(tileResource, implArch.getIncidentEdges(tileResource).iterator().next());
		Resource router = null;
		for (Link l : implArch.getIncidentEdges(bus)) {
			if (LinkInvasicPropertyService.isNocLink(l)) {
				router = implArch.getOpposite(bus, l);
			}
		}
		if (router == null || !ResourceInvasicPropertyService.isRouter(router)) {
			throw new IllegalArgumentException("No router found for resource " + tileResource.getId());
		}
		return router;
	}

}
