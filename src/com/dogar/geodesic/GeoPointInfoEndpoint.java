package com.dogar.geodesic;

import com.dogar.geodesic.EMF;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;
import com.google.appengine.api.users.User;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "geopointinfoendpoint", namespace = @ApiNamespace(ownerDomain = "dogar.com", ownerName = "dogar.com", packagePath = "geodesic"), version = "v1", clientIds = {
		ID_Constants.WEB_CLIENT_ID, ID_Constants.ANDROID_CLIENT_ID,
		com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID }, audiences = { ID_Constants.ANDROID_AUDIENCE })
public class GeoPointInfoEndpoint {

	/**
	 * This method lists all the entities inserted in datastore. It uses HTTP
	 * GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 *         persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listGeoPointInfo")
	public CollectionResponse<GeoPointInfo> listGeoPointInfo(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit, User user)
			throws UnauthorizedException {
		if (user == null)
			throw new UnauthorizedException("User is Not Valid");
		setNamespace(user);
		EntityManager mgr = null;
		Cursor cursor = null;
		List<GeoPointInfo> execute = null;

		try {
			mgr = getEntityManager();
			
			Query query = mgr
					.createQuery("select from GeoPointInfo as GeoPointInfo WHERE isDeleted = false");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<GeoPointInfo>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and
			// accomodate
			// for lazy fetch.
			for (GeoPointInfo obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<GeoPointInfo> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET
	 * method.
	 *
	 * @param id
	 *            the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getGeoPointInfo")
	public GeoPointInfo getGeoPointInfo(@Named("id") Long id, User user)
			throws UnauthorizedException {
		if (user == null)
			throw new UnauthorizedException("User is Not Valid");
		setNamespace(user);
		EntityManager mgr = getEntityManager();
		GeoPointInfo geopointinfo = null;
		try {
			geopointinfo = mgr.find(GeoPointInfo.class, id);
		} finally {
			mgr.close();
		}
		return geopointinfo;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity
	 * already exists in the datastore, an exception is thrown. It uses HTTP
	 * POST method.
	 *
	 * @param geopointinfo
	 *            the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertGeoPointInfo")
	public GeoPointInfo insertGeoPointInfo(GeoPointInfo geopointinfo, User user)
			throws UnauthorizedException {
		if (user == null)
			throw new UnauthorizedException("User is Not Valid");
		setNamespace(user);
		EntityManager mgr = getEntityManager();
		try {
			if (containsGeoPointInfo(geopointinfo)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(geopointinfo);
		} finally {
			mgr.close();
		}
		return geopointinfo;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does
	 * not exist in the datastore, an exception is thrown. It uses HTTP PUT
	 * method.
	 *
	 * @param geopointinfo
	 *            the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateGeoPointInfo")
	public GeoPointInfo updateGeoPointInfo(GeoPointInfo geopointinfo, User user)
			throws UnauthorizedException {
		if (user == null)
			throw new UnauthorizedException("User is Not Valid");
		setNamespace(user);
		EntityManager mgr = getEntityManager();
		try {
			if (!containsGeoPointInfo(geopointinfo)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(geopointinfo);
		} finally {
			mgr.close();
		}
		return geopointinfo;
	}

	/**
	 * This method removes the entity with primary key id. It uses HTTP DELETE
	 * method.
	 *
	 * @param id
	 *            the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeGeoPointInfo")
	public void removeGeoPointInfo(@Named("id") Long id, User user)
			throws UnauthorizedException {
		if (user == null)
			throw new UnauthorizedException("User is Not Valid");
		setNamespace(user);
		EntityManager mgr = getEntityManager();
		try {
			GeoPointInfo geopointinfo = mgr.find(GeoPointInfo.class, id);
			mgr.remove(geopointinfo);
		} finally {
			mgr.close();
		}
	}

	private boolean containsGeoPointInfo(GeoPointInfo geopointinfo) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			if (geopointinfo.getId() == null)
				return false;
			GeoPointInfo item = mgr.find(GeoPointInfo.class,
					geopointinfo.getId());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

	private void setNamespace(User user) {
		if (NamespaceManager.get() == null) {
			String[] avoidDogSymbol = user.getEmail().split("@");//Namespace pattern [0-9A-Za-z._-]{0,100}
			String namespace = avoidDogSymbol[0] + avoidDogSymbol[1];
			NamespaceManager.set(namespace);
		}
	}
}
