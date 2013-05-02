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

package eu.trentorise.smartcampus.commons.test;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopic;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicContentType;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicNews;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSource;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicStatus;
import it.unitn.disi.sweb.webapi.model.smartcampus.livetopics.LiveTopicSubject;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;
import it.unitn.disi.sweb.webapi.model.ss.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.trentorise.smartcampus.common.Concept;
import eu.trentorise.smartcampus.common.SemanticHelper;

public class TestSemanticHelper {

	private static final String SE_HOST = "213.21.154.85";
	private static final int SE_PORT = 8080;
	
	private static SCWebApiClient client = null;
	
	@BeforeClass
	public static void setupClient() {
		client = SCWebApiClient.getInstance(Locale.ITALIAN, SE_HOST, SE_PORT);

	}

	@Test
	public void testCreateSCEntity() throws WebApiException, JSONException {
		Entity e = create();
		System.err.println(e.toString());
		
		boolean result = SemanticHelper.deleteEntity(client, e.getId());
		System.err.println(result);
	}

	@Test
	public void testCreateSCEntityWOTags() throws WebApiException, JSONException {
		Entity e = SemanticHelper.createSCEntity(client, "event", "Mensa via Zanella", "Posti liberi al 2012_08_16 05.45.00 : 526", null, null);
		System.err.println(e.toString());
		
		boolean result = SemanticHelper.deleteEntity(client, e.getId());
		System.err.println(result);
		
		boolean deleteResult = SemanticHelper.deleteEntity(client, e.getId());
		System.err.println(deleteResult);

	}

	private Entity create() throws WebApiException {
		Concept textTag = new Concept();
		textTag.setName("java text");
		Concept semTag = new Concept();
		Word word = client.readConceptsByWordPrefix("java", 1).get(0);
		semTag.setId(word.getConcepts().get(0).getGlobalId());
		semTag.setName(word.getConcepts().get(0).getLabel());
		
		Entity e = SemanticHelper.createSCEntity(client, "event", "some event", "text description", Arrays.asList(new Concept[]{textTag, semTag}), null);
		return e;
	}
	
	@Test
	public void testUpdateSCEntity() throws WebApiException, JSONException {
		Entity e = create();
		System.err.println(e);

		try {
			Entity updated = SemanticHelper.updateEntity(client, e.getId(), "new name", "test", null, null);
			System.err.println(updated);
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			boolean result = SemanticHelper.deleteEntity(client, e.getId());
			System.err.println(result);
		}
	}
	
	@Test
	public void testSuggestions() throws WebApiException {
		System.err.println(SemanticHelper.getSuggestions(client, "test", 10));
	}
	
	public static void main(String[] args) throws Exception {
		client = SCWebApiClient.getInstance(Locale.ENGLISH, SE_HOST, SE_PORT);
		Entity entity = client.readEntity(3121L, null);
		System.err.println(entity);

//		System.err.println(SemanticHelper.isEntitySharedWithUser(client, 6627L, 103L));
		
//		Entity e = SemanticHelper.createEntity(client, 396L, "event", "test evento Università degli ", null, null, null);
//		System.err.println(client.readEntity(e.getId(), null));
//		client.deleteEntity(e.getId());
		
		readNews(396L);
		
//		testReadingSharedContent(396L, 0, 100, null); 
		
//		User u = client.readUser(321);
//		System.err.println(u.getId());
//		Community community = client
//				.readCommunity(Constants.SMARTCAMPUS_COMMUNITY);
//
//		EntityBase eb = SemanticHelper.getSCCommunityEntityBase(client);
//		System.err.println(client.readEntity(6483L, null));
//		LiveTopicSource assignments = client.readAssignments(6483L,
//				Operation.READ, community.getId());
//		System.err.println(assignments.isAllUsers()); 
		
//		Entity e = SemanticHelper.createSCEntity(client, "event", "Mensa via Zanella", "Posti liberi al 2012_08_16 05.45.00 : 526", null, null);
//		System.err.println(e);
//		
//		List<Entity> list = client.readEntities("event", eb.getLabel(), null);
//		for (Entity e : list) {
//			System.err.println(e);
//			ShareVisibility v = new ShareVisibility();
//			v.setAllUsers(true);
//			SemanticHelper.shareEntity(client, e.getId(), 124L, v);
////			client.deleteEntity(e.getId());
//		}
//		list = client.readEntities("location", eb.getLabel(), null);
//		for (Entity e : list) {
//			System.err.println(e);
//			ShareVisibility v = new ShareVisibility();
//			v.setAllUsers(true);
//			SemanticHelper.shareEntity(client, e.getId(), 124L, v);
////			client.deleteEntity(e.getId());
//		}
//		System.err.println(client.readEntityBases());
//		System.err.println(client.readCommunities());
	}
	
	private static final String[] supportedTypes = new String[]{"event", "experience", "computer file",
					"journey", "location", "portfolio", "narrative" };
	private static Map<String,Long> typeIds = null;
	
	private static Map<String, Long> getTypesIds() throws WebApiException {
		if (typeIds == null) {
			typeIds = new HashMap<String, Long>(supportedTypes.length);
			for (int i = 0; i < supportedTypes.length; i++) {
				it.unitn.disi.sweb.webapi.model.entity.EntityType eType = client
						.readEntityType(supportedTypes[i], SemanticHelper.getSCCommunityEntityBase(client).getKbLabel());
				if (eType != null) {
					typeIds.put(supportedTypes[i], eType.getId());
				} else {
					// TODO signal error
				}
			}
		}
		return typeIds;
	}

	private static void readNews(long actorId) throws WebApiException {
		List<LiveTopic> topics = client.readLiveTopics(actorId,
				LiveTopicStatus.ACTIVE, false);
		int i = 0;
		for (LiveTopic lt : topics) {
			for (LiveTopicNews news : client.readLiveTopicNews(lt.getId(), null)) {
				System.err.println(news.getNewsEntityId()+":"+news.getTopicId());
				i++;
			}
		}
		System.err.println("total "+i);
	}
	
	private static void testReadingSharedContent(long ownerId, int pos, int size, String type) throws Exception {
		System.err.println(new Date(1348752590483L));
		
		User u = client.readUser(ownerId);
		EntityBase eb = client.readEntityBase(u
				.getEntityBaseId());

		LiveTopic filter = new LiveTopic();
		filter.setActorId(ownerId);
		LiveTopicSource filterSource = new LiveTopicSource();
		filterSource.setUserIds(Collections.singleton(ownerId));
		filter.setSource(filterSource);
		LiveTopicContentType contentType = new LiveTopicContentType();
		if (type != null && getTypesIds().containsKey(type)) {
			contentType.setEntityTypeIds(Collections.singleton(getTypesIds().get(type)));
		} else {
			contentType.setEntityTypeIds(new HashSet<Long>(getTypesIds().values()));
		}
		filter.setType(contentType); // <-- mandatory
		filter.setStatus(LiveTopicStatus.ACTIVE); // <-- mandatory
		LiveTopicSubject lts = new LiveTopicSubject();
		lts.setAllSubjects(true);
		Set<LiveTopicSubject> subjects = new HashSet<LiveTopicSubject>();
		subjects.add(lts);
		filter.setSubjects(subjects);

		List<Long> sharedIds = client.computeEntitiesForLiveTopic(filter, null, null);
		Collections.sort(sharedIds, new Comparator<Long>() {
			public int compare(Long o1, Long o2) {
				return o2 > o1 ? 1 : o2 == o1 ? 0 : -1;
			}
		});
		if (pos < sharedIds.size()) {
			sharedIds = sharedIds.subList(pos, Math.min(pos + size,sharedIds.size()));
		} else {
			// TODO return Collections.emptyList();
		}
		if (!sharedIds.isEmpty()) {
			List<Entity> results = client.readEntities(sharedIds, null);
			for (Entity e : results) {
				System.err.println(e.getId());
			}
		} else {
			// TODO return Collections.emptyList();
		}
	}
}
