package eu.trentorise.smartcampus.common;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.Attribute;
import it.unitn.disi.sweb.webapi.model.entity.DataType;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.entity.Value;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.Community;
import it.unitn.disi.sweb.webapi.model.ss.SemanticString;
import it.unitn.disi.sweb.webapi.model.ss.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SemanticHelper {

	private static final String ATTR_NAME = "name";
	private static final String ATTR_DESCRIPTION = "description";
	private static final String ATTR_TEXT_TAG = "text";
	private static final String ATTR_SEMANTIC_TAG = "semantic";
	private static final String ATTR_RELATION = "entity";
	
	private static EntityBase scCommunityEntityBase = null;
	private static Long scCommunityActorId = null;
	
	private static Map<String, EntityType> cachedTypeMap = new HashMap<String, EntityType>();
	private static Log logger = LogFactory.getLog(SemanticHelper.class);
	
	public static void init(SCWebApiClient client) throws WebApiException {
		getSCCommunityEntityBase(client);
	}
	
	public static EntityBase getSCCommunityEntityBase(SCWebApiClient client) throws WebApiException {
		if (scCommunityEntityBase != null) return scCommunityEntityBase;
		Community community = client.readCommunity(Constants.SMARTCAMPUS_COMMUNITY);
		if (community == null) {
			throw new WebApiException("SC community does not exist");
		}
		
		scCommunityEntityBase = client.readEntityBase(community.getEntityBaseId());
		if (scCommunityEntityBase == null) {
			throw new WebApiException("SC community entity base does not exist");
		}
		scCommunityActorId = community.getId();
		return scCommunityEntityBase;
	}

	private static EntityBase getEntityBase(SCWebApiClient client, Long actorId) throws WebApiException {
		if (actorId == scCommunityActorId) return scCommunityEntityBase;
		Entity actor = client.readEntity(actorId, null);
		if (actor == null) {
			throw new WebApiException("Actor with id "+actorId+" is not found.");
		}
		return actor.getEntityBase();
	}
	public static Entity createSCEntity(SCWebApiClient client, String type, String name, String description, List<Concept> concepts, List<Long> relations) throws WebApiException {
		EntityBase eb = getSCCommunityEntityBase(client);
		EntityType et = getEntityType(client, eb, type);
		Entity entity = new Entity();
		entity.setEntityBase(eb);
		entity.setEtype(et);

		updateAttributes(client, name, description, concepts, relations, entity);
		long eid = client.create(entity);
		entity = client.readEntity(eid, null);
		return entity;
	}
	
	public static Entity createEntity(SCWebApiClient client, Long actorId, String type, String name, String description, List<Concept> concepts, List<Long> relations) throws WebApiException {
		EntityBase eb = getEntityBase(client, actorId);
		EntityType et = getEntityType(client, eb, type);
		Entity entity = new Entity();
		entity.setEntityBase(eb);
		entity.setEtype(et);

		updateAttributes(client, name, description, concepts, relations, entity);
		long eid = client.create(entity);
		entity = client.readEntity(eid, null);
		return entity;
	}

	public static Entity updateEntity(SCWebApiClient client, Long id, String name, String description, List<Concept> concepts, List<Long> relations) throws WebApiException {
		Entity entity = client.readEntity(id, null);
		if (entity == null) {
			throw new WebApiException("Entity with id " + id + " does not exist");
		}
		entity.setEtype(getEntityType(client, entity.getEntityBase(), entity.getEtype().getName()));
		updateAttributes(client, name, description, concepts, relations, entity);
		client.updateEntity(entity);
		entity = client.readEntity(id, null);
		return entity;
	}

	public static boolean deleteEntity(SCWebApiClient client, Long id) throws WebApiException {
		return client.deleteEntity(id);
	}

	private static void updateAttributes(SCWebApiClient client, String name, String description, List<Concept> concepts, List<Long> relations, Entity entity) throws WebApiException {
		List<Attribute> attrs = new ArrayList<Attribute>();
		
		// name attribute
		if (name != null) {
			attrs.add(createTextAttribute(ATTR_NAME, new String[]{name}, entity.getEtype()));
		}
		// description attribute
		if (description != null) {
			Attribute a = createSemanticAttribute(client, ATTR_DESCRIPTION, new String[]{description}, null, entity.getEtype(), entity.getEntityBase());
			attrs.add(a);
		}
		List<String> textTags = new ArrayList<String>();
		List<Long> semanticTags = new ArrayList<Long>();
		if (concepts != null) {
			for (Concept c : concepts) {
				if (c.getId() == null || c.getId() <= 0) textTags.add(c.getName());
				else semanticTags.add(c.getId());
			}
		}
		// text tags attribute
		if (!textTags.isEmpty()) {
			attrs.add(createTextAttribute(ATTR_TEXT_TAG, textTags.toArray(new String[textTags.size()]), entity.getEtype()));
		}
		// semantic tags attribute
		if (!semanticTags.isEmpty()) {
			Attribute a = createSemanticAttribute(client, ATTR_SEMANTIC_TAG, null, semanticTags.toArray(new Long[semanticTags.size()]), entity.getEtype(), entity.getEntityBase()); 
			attrs.add(a);
		}
		if (relations != null && relations.size() > 0) {
			attrs.add(createRelationAttribute(client, ATTR_RELATION, relations.toArray(new Long[relations.size()]), entity.getEtype(), entity.getEntityBase()));
		}
		
		entity.setAttributes(attrs);
	}

	private static Attribute createRelationAttribute(SCWebApiClient client, String aName, Long[] array, EntityType et, EntityBase eb) {
		List<Value> valueList = new ArrayList<Value>();
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName(aName));
		for (Long s : array) {
			Entity related = null;
			try {
				related = client.readEntity(s, null);
			} catch (Exception e) {
				logger.error("Failed to find entity with id "+s+": "+e.getMessage());
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

	private static Attribute createSemanticAttribute(SCWebApiClient client, String aName, String[] text, Long[] array, EntityType et, EntityBase eb) throws WebApiException {
		List<Value> valueList = new ArrayList<Value>();
		Attribute a = new Attribute();
		a.setAttributeDefinition(et.getAttributeDefByName(aName));
		if (text == null && array == null) return null;
		if (text == null) text = new String[array.length];
		else if (array == null) array = new Long[text.length];
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
					concept = client.readConceptByGlobalId(array[i], eb.getKbLabel());
				} catch (Exception e) {
					logger.error("Failed to find concept with id "+array[i]+": "+e.getMessage());
				}
				if (concept == null) {
					logger.error("Failed to find concept with id "+array[i]+".");
					continue;
				}
				if (ss.getString() == null) ss.setString(concept.getLabel());
				ss.setTokens(Arrays.asList(new Token[]{new Token(concept.getLabel(),concept.getLabel(),concept.getId(), Arrays.asList(new it.unitn.disi.sweb.webapi.model.ss.Concept[]{concept}))}));
			}
			v.setSemanticStringValue(ss);
			valueList.add(v);
		}
		a.setValues(valueList);
		return a;
	}

	private static Attribute createTextAttribute(String aName, String[] values, EntityType et) {
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
	
	private static EntityType getEntityType(SCWebApiClient client, EntityBase eb, String type) throws WebApiException {
		EntityType et = cachedTypeMap .get(type);
		if (et == null) {
			et = client.readEntityType(type, eb.getKbLabel());
			cachedTypeMap.put(type, et);
		}
		return et;
	}
}
