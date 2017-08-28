package org.opensrp.web.rest.it;

import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.domain.Client;
import org.opensrp.domain.User;
import org.opensrp.repository.AllUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sun.security.pkcs11.P11Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

public class ProviderResourceTest extends BaseResourceTest {

	private final static String BASE_URL = "/rest/provider/";

	private final static String AUTHENTICATE_URL = "authenticate/";

	public static final String SALT = "salt";

	public static final String PASSWORD = "password";

	public static final String USER_NAME = "userName";

	public static final String BASE_ENTITY_ID = "1";

	@Autowired
	AllUsers allUsers;

	@Before
	public void setUp() {
		User user = new User(BASE_ENTITY_ID, USER_NAME, PASSWORD, SALT);
		allUsers.add(user);
	}

	@After
	public void cleanUp() {
		allUsers.removeAll();
	}

	@Test
	@Ignore
	public void shouldReturnEmptyResponseWithOutTeamId() throws Exception {
		String query = "?u=" + USER_NAME + "&p=" + PASSWORD;
		String urlWithQuery = BASE_URL + AUTHENTICATE_URL + query;
		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(urlWithQuery).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andReturn();
		String responseString = mvcResult.getResponse().getContentAsString();
		assertTrue(responseString.equals("{}"));
	}

	@Test
	public void shouldReturnErrorMessageWithOutTeamIdAndUserName() throws Exception {
		String query = "?p=" + PASSWORD;
		String urlWithQuery = BASE_URL + AUTHENTICATE_URL + query;
		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(urlWithQuery).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andReturn();
		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		Map<String, String> response = mapper.treeToValue(actualObj, Map.class);
		assertTrue(response.containsKey("ERROR"));
		assertEquals("Username and Password not provided.", response.get("ERROR"));
	}

	@Test
	public void shouldReturnErrorMessageWithOutTeamIdAndPassword() throws Exception {
		String query = "?u=" + PASSWORD;
		String urlWithQuery = BASE_URL + AUTHENTICATE_URL + query;
		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(urlWithQuery).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andReturn();
		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		Map<String, String> response = mapper.treeToValue(actualObj, Map.class);
		assertTrue(response.containsKey("ERROR"));
		assertEquals("Username and Password not provided.", response.get("ERROR"));
	}

	@Ignore
	@Test
	public void shouldReturnEmptyMessageWithOutOpenmrsAuthentication() throws Exception {
		String query = "?u=" + USER_NAME + "&p=" + PASSWORD;
		String urlWithQuery = BASE_URL + AUTHENTICATE_URL + query;
		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(urlWithQuery).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andReturn();
		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		Map<String, String> response = mapper.treeToValue(actualObj, Map.class);
		assertTrue(response.containsKey("ERROR"));
		assertEquals("Authentication failed with given credentials", response.get("ERROR"));
	}

	//TODO: Write rest of the tests  after openmrs intigration.
}