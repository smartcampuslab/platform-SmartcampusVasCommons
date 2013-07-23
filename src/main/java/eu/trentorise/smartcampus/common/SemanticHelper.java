/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.common;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.Attribute;
import it.unitn.disi.sweb.webapi.model.entity.DataType;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.entity.Value;
import it.unitn.disi.sweb.webapi.model.smartcampus.ac.Operation;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.Community;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;
import it.unitn.disi.sweb.webapi.model.ss.SemanticString;
import it.unitn.disi.sweb.webapi.model.ss.Token;
import it.unitn.disi.sweb.webapi.model.ss.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.trentorise.smartcampus.social.model.Concept;
import eu.trentorise.smartcampus.social.model.Constants;
import eu.trentorise.smartcampus.social.model.ShareVisibility;

public class SemanticHelper {

	private static final String ATTR_NAME = "name";
	private static final String ATTR_DESCRIPTION = "description";
	private static final String ATTR_TEXT_TAG = "text";
	private static final String ATTR_SEMANTIC_TAG = "semantic";
	private static final String ATTR_RELATION = "entity";

	private EntityBase scCommunityEntityBase = null;
	private Long scCommunityActorId = null;

	private Map<String, EntityType> cachedTypeMap = new HashMap<String, EntityType>();
	private Log logger = LogFactory.getLog(getClass());

	private SCWebApiClient client;

	private static SemanticHelper instance = null;

	public SemanticHelper(SCWebApiClient client) throws WebApiException {
		super();
		this.client = client;
	}

	private static SemanticHelper getInstance(SCWebApiClient client)
			throws WebApiException {
		if (instance == null)
			instance = new SemanticHelper(client);
		return instance;
	}

	public static EntityBase getSCCommunityEntityBase(SCWebApiClient client)
			throws WebApiException {
		return getInstance(client).getSCCommunityEntityBase();
	}

	private EntityBase getSCCommunityEntityBase() throws WebApiException {
		synchronized (client) {
			if (scCommunityEntityBase != null)
				return scCommunityEntityBase;
			Community community = client
					.readCommunity(Constants.SMARTCAMPUS_COMMUNITY);
			if (community == null) {
				throw new WebApiException("SC community does not exist");
			}

			scCommunityEntityBase = client.readEntityBase(community
					.getEntityBaseId());
			if (scCommunityEntityBase == null) {
				throw new WebApiException(
						"SC community entity base does not exist");
			}
			scCommunityActorId = community.getId();
			return scCommunityEntityBase;
		}
	}

	private EntityBase getEntityBase(SCWebApiClient client, Long actorId)
			throws WebApiException {
		if (actorId == scCommunityActorId)
			return scCommunityEntityBase;
		User actor = client.readUser(actorId);

		if (actor == null) {
			throw new WebApiException("Actor with id " + actorId
					+ " is not found.");
		}
		Long ebid = actor.getEntityBaseId();
		if (ebid == null) {
			throw new WebApiException("Actor with id " + actorId
					+ " has null entitybase reference");
		}
		return client.readEntityBase(ebid);
	}

	public static Entity createSCEntity(SCWebApiClient client, String type,
			String name, String description, List<Concept> concepts,
			List<Long> relations) throws WebApiException {
		return getInstance(client).createSCEntity(type, name, description,
				concepts, relations);
	}

	private Entity createSCEntity(String type, String name, String description,
			List<Concept> concepts, List<Long> relations)
			throws WebApiException {
		synchronized (client) {
			EntityBase eb = getSCCommunityEntityBase(client);
			EntityType et = getEntityType(client, eb, type);
			Entity entity = new Entity();
			entity.setEntityBase(eb);
			entity.setEtype(et);
			updateAttributes(client, name, description, concepts, relations,
					entity);
			long eid = client.create(entity);
			ShareVisibility visibility = new ShareVisibility();
			visibility.setAllUsers(true);
			shareEntity(eid, scCommunityActorId, visibility);
			entity = client.readEntity(eid, null);
			return entity;
		}
	}

	public static Entity createEntity(SCWebApiClient client, Long actorId,
			String type, String name, String description,
			List<Concept> concepts, List<Long> relations)
			throws WebApiException {
		return getInstance(client).createEntity(actorId, type, name,
				description, concepts, relations);
	}

	private Entity createEntity(Long actorId, String type, String name,
			String description, List<Concept> concepts, List<Long> relations)
			throws WebApiException {
		synchronized (client) {
			EntityBase eb = getEntityBase(client, actorId);
			EntityType et = getEntityType(client, eb, type);
			Entity entity = new Entity();
			entity.setEntityBase(eb);
			entity.setEtype(et);
			updateAttributes(client, name, description, concepts, relations,
					entity);
			long eid = client.create(entity);
			entity = client.readEntity(eid, null);
			return entity;
		}
	}

	public static Entity updateEntity(SCWebApiClient client, Long id,
			String name, String description, List<Concept> concepts,
			List<Long> relations) throws WebApiException {
		return getInstance(client).updateEntity(id, name, description,
				concepts, relations);
	}

	private Entity updateEntity(Long id, String name, String description,
			List<Concept> concepts, List<Long> relations)
			throws WebApiException {
		synchronized (client) {
			Entity entity = client.readEntity(id, null);
			if (entity == null) {
				throw new WebApiException("Entity with id " + id
						+ " does not exist");
			}
			entity.setEtype(getEntityType(client, entity.getEntityBase(),
					entity.getEtype().getName()));
			updateAttributes(client, name, description, concepts, relations,
					entity);
			client.updateEntity(entity);
			entity = client.readEntity(id, null);
			return entity;
		}
	}

	public static boolean deleteEntity(SCWebApiClient client, Long id)
			throws WebApiException {
		return getInstance(client).deleteEntity(id);
	}

	private boolean deleteEntity(Long id) throws WebApiException {
		synchronized (client) {
			return client.deleteEntity(id);
		}
	}

	private void updateAttributes(SCWebApiClient client, String name,
			String description, List<Concept> concepts, List<Long> relations,
			Entity entity) throws WebApiException {
		List<Attribute> attrs = new ArrayList<Attribute>();

		// name attribute
		if (name != null) {
			attrs.add(createTextAttribute(ATTR_NAME, new String[] { name },
					entity.getEtype()));
		}
		// description attribute
//		if (description != null) {
//			Attribute a = createSemanticAttribute(client, ATTR_DESCRIPTION,
//					new String[] { description }, null, entity.getEtype(),
//					entity.getEntityBase());
////			Attribute a = createTextAttribute(ATTR_DESCRIPTION,
////					new String[] { description }, entity.getEtype());
//			if (a != null)
//				attrs.add(a);
//		}
		List<String> textTags = new ArrayList<String>();
		List<Concept> semanticTags = new ArrayList<Concept>();
		if (concepts != null) {
			for (Concept c : concepts) {
				if (c.getId() == null || c.getId().equals("-1"))
					textTags.add(c.getName());
				else
					semanticTags.add(c);
			}
		}
		// text tags attribute
		if (!textTags.isEmpty()) {
			attrs.add(createTextAttribute(ATTR_TEXT_TAG,
					textTags.toArray(new String[textTags.size()]),
					entity.getEtype()));
		}
		// semantic tags attribute
		if (!semanticTags.isEmpty()) {
			Attribute a = createSemanticAttribute(client, ATTR_SEMANTIC_TAG,
					null,
					semanticTags.toArray(new Concept[semanticTags.size()]),
					entity.getEtype(), entity.getEntityBase());
			if (a != null)
				attrs.add(a);
		}
		if (relations != null && relations.size() > 0) {
			attrs.add(createRelationAttribute(client, ATTR_RELATION,
					relations.toArray(new Long[relations.size()]),
					entity.getEtype(), entity.getEntityBase()));
		}

		entity.setAttributes(attrs);
	}

	private Attribute createRelationAttribute(SCWebApiClient client,
			String aName, Long[] array, EntityType et, EntityBase eb) {
		List<Value> valueList = new ArrayList<Value>();
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName(aName));
		for (Long s : array) {
			Entity related = null;
			try {
				related = client.readEntity(s, null);
			} catch (Exception e) {
				logger.error("Failed to find entity with id " + s + ": "
						+ e.getMessage());
				continue;
			}
			Value v = new Value();
			v = new Value();
			v.setType(DataType.RELATION);
			v.setRelationEntity(related);
			valueList.add(v);
		}
		a.setValues(valueList);
		return a;
	}

	private Attribute createSemanticAttribute(SCWebApiClient client,
			String aName, String[] text, Concept[] array, EntityType et,
			EntityBase eb) throws WebApiException {
		List<Value> valueList = new ArrayList<Value>();
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName(aName));
		if (text == null && array == null)
			return null;
		if (text == null)
			text = new String[array.length];
		else if (array == null)
			array = new Concept[text.length];
		for (int i = 0; i < Math.max(text.length, array.length); i++) {
			Value v = new Value();
			v.setType(DataType.SEMANTIC_STRING);
			SemanticString ss = new SemanticString();
			if (text[i] != null) {
				ss.setString(text[i]);
			}
			if (array[i] != null) {
				it.unitn.disi.sweb.webapi.model.ss.Concept concept = null;
				try {
					concept = client.readConcept(Long.parseLong(array[i].getId()));
				} catch (Exception e) {
					logger.error("Exception looking for concept with id "
							+ array[i] + ": " + e.getMessage());
				}
				if (concept == null) {
					logger.error("Failed to find concept with id " + array[i]
							+ ".");
					continue;
				}
				String token = array[i].getName();
				if (token == null) {
					logger.warn("Token for tag concept " + concept.getId()
							+ " is not specified!");
					token = concept.getLabel();
				}

				if (ss.getString() == null)
					ss.setString(token);
				ss.setTokens(Collections.singletonList(new Token(token, concept
						.getLabel(), concept.getId(), Collections
						.singletonList(concept))));
			}
			v.setSemanticStringValue(ss);
			valueList.add(v);
		}
		if (!valueList.isEmpty()) {
			a.setValues(valueList);
			return a;
		}
		return null;
	}

	private Attribute createTextAttribute(String aName, String[] values,
			EntityType et) {
		List<Value> valueList = new ArrayList<Value>();
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName(aName));
		for (String s : values) {
			Value v = new Value();
			v.setType(DataType.STRING);
			v.setStringValue(s);
			valueList.add(v);
		}
		a.setValues(valueList);
		return a;
	}

	private EntityType getEntityType(SCWebApiClient client, EntityBase eb,
			String type) throws WebApiException {
		EntityType et = cachedTypeMap.get(type);
		if (et == null) {
			et = client.readEntityType(type, eb.getKbLabel());
			cachedTypeMap.put(type, et);
		}
		return et;
	}

	public static List<Concept> getSuggestions(SCWebApiClient client,
			String prefix, int max) throws WebApiException {
		return getInstance(client).getSuggestions(prefix, max);
	}

	private List<Concept> getSuggestions(String prefix, int max)
			throws WebApiException {
		synchronized (client) {
			List<Word> list = client.readConceptsByWordPrefix(prefix, max);
			if (list != null) {
				List<Concept> result = new ArrayList<Concept>();
				for (Word w : list) {
					for (it.unitn.disi.sweb.webapi.model.ss.Concept c : w
							.getConcepts()) {
						Concept nc = new Concept();
						nc.setName(w.getLemma());
						nc.setDescription(c.getDescription());
						nc.setSummary(c.getSummary());
						nc.setId(c.getId().toString());
						result.add(nc);
					}
				}
				return result;
			}
			return Collections.emptyList();
		}
	}

	public static void unshareEntity(SCWebApiClient client, Long entityId,
			Long ownerId) throws WebApiException {
		getInstance(client).unshareEntity(entityId, ownerId);
	}

	private void unshareEntity(Long entityId, Long ownerId)
			throws WebApiException {
		synchronized (client) {
			LiveTopicSource assignments = client.readAssignments(entityId,
					Operation.READ, ownerId);
			client.updateAssignments(entityId, Operation.READ, ownerId,
					clearSource(assignments));
		}
	}

	public static void unshareEntityWith(SCWebApiClient client, Long entityId,
			Long ownerId, Long actorId) throws WebApiException {
		getInstance(client).unshareEntityWith(entityId, ownerId, actorId);
	}

	private void unshareEntityWith(Long entityId, Long ownerId, Long actorId)
			throws WebApiException {
		synchronized (client) {
			LiveTopicSource assignments = client.readAssignments(entityId,
					Operation.READ, ownerId);
			if (assignments != null && assignments.getUserIds() != null) {
				assignments.getUserIds().remove(actorId);
				client.updateAssignments(entityId, Operation.READ, ownerId,
						assignments);
			}
		}
	}

	private LiveTopicSource clearSource(LiveTopicSource src) {
		src.setAllCommunities(false);
		src.setAllKnownCommunities(false);
		src.setAllKnownUsers(false);
		src.setAllUsers(false);

		src.setCommunityIds(Collections.<Long> emptySet());
		src.setGroupIds(Collections.<Long> emptySet());
		src.setUserIds(Collections.<Long> emptySet());

		return src;
	}

	public static void shareEntity(SCWebApiClient client, Long entityId,
			Long ownerId, ShareVisibility visibility) throws WebApiException {
		getInstance(client).shareEntity(entityId, ownerId, visibility);
	}

	private void shareEntity(Long entityId, Long ownerId,
			ShareVisibility visibility) throws WebApiException {
		synchronized (client) {
			LiveTopicSource assignments = client.readAssignments(entityId,
					Operation.READ, ownerId);
			client.updateAssignments(entityId, Operation.READ, ownerId,
					updateSource(assignments, visibility));
		}
	}

	public static void shareEntityWith(SCWebApiClient client, Long entityId,
			Long ownerId, Long actorId) throws WebApiException {
		getInstance(client).shareEntityWith(entityId, ownerId, actorId);
	}

	private void shareEntityWith(Long entityId, Long ownerId, Long actorId)
			throws WebApiException {
		synchronized (client) {
			LiveTopicSource assignments = client.readAssignments(entityId,
					Operation.READ, ownerId);
			if (assignments != null) {
				if (assignments.getUserIds() == null) {
					assignments.setUserIds(new HashSet<Long>());
				}
				assignments.getUserIds().add(actorId);
			}

			client.updateAssignments(entityId, Operation.READ, ownerId,
					assignments);
		}
	}

	private LiveTopicSource updateSource(LiveTopicSource src, ShareVisibility sv) {
		src.setAllCommunities(sv.isAllCommunities());
		src.setAllKnownCommunities(sv.isAllKnownCommunities());
		src.setAllKnownUsers(sv.isAllKnownUsers());
		src.setAllUsers(sv.isAllUsers());
		src.getGroupIds().clear();
		src.getUserIds().clear();
		if (sv.getGroupIds() != null) {
			for (String sid : sv.getGroupIds()) {
				Long id = Long.parseLong(sid);
				if (!src.getGroupIds().contains(id)) {
					src.getGroupIds().add(id);
				}
			}
		} else {
			src.setGroupIds(null);
		}
		if (sv.getCommunityIds() != null) {
			for (String sid : sv.getCommunityIds()) {
				Long id = Long.parseLong(sid);
				if (!src.getCommunityIds().contains(id)) {
					src.getCommunityIds().add(id);
				}
			}
		} else {
			src.setCommunityIds(null);
		}
		if (sv.getUserIds() != null) {
			for (String sid : sv.getUserIds()) {
				Long id = Long.parseLong(sid);
				if (!src.getUserIds().contains(id)) {
					src.getUserIds().add(id);
				}
			}
		} else {
			src.setUserIds(null);
		}

		return src;
	}

	public static boolean isEntitySharedWithUser(SCWebApiClient client,
			Long entityId, long actorId) throws WebApiException {
		return getInstance(client).isEntitySharedWithUser(entityId, actorId);
	}

	private boolean isEntitySharedWithUser(Long entityId, long actorId)
			throws WebApiException {
		return client.readPermission(actorId, entityId, Operation.READ);
	}

}
