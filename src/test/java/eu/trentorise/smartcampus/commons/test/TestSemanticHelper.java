package eu.trentorise.smartcampus.commons.test;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.ss.Word;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
		client = SCWebApiClient.getInstance(Locale.ENGLISH, SE_HOST, SE_PORT);

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
		Entity e = SemanticHelper.createSCEntity(client, "event", "some event", null, null, null);
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
	public void testCleanEB() throws WebApiException {
		EntityBase eb = SemanticHelper.getSCCommunityEntityBase(client);
		System.err.println(client.readEntity(682L, null));
		
		List<Entity> list = client.readEntities("event", eb.getLabel(), null);
		for (Entity e : list) {
			client.deleteEntity(e.getId());
		}
		System.err.println(client.readEntityBases());
		System.err.println(client.readCommunities());
	}
}
