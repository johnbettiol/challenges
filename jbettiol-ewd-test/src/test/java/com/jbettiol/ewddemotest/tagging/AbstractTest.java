package com.jbettiol.ewddemotest.tagging;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbettiol.ewddemo.EwdDemoApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EwdDemoApplication.class)
public abstract class AbstractTest {

	protected static final String STR_EXCEPTION_TAGGED_FILES_EXCEED = "Tagged files exceed";

	protected static final String TEST_FOLDER_PATH = "/EWD-TestFolder";

	protected static final int DEF_MAX_FILES_PER_TAG = 2;
	protected static final int DEF_MAX_BYTES_SIZE = 256;
	protected static final String TAG_BEACH = "beach";
	protected static final String TAG_MIAMI = "miami";
	protected static final String TAG_WORK = "work";
	protected static final String TAG_CV = "cv";
	protected static final String TAG_APPLICATION = "application";
	protected static final Set<String> DEF_TAGS_TO_ADD = new HashSet<String>() {
		private static final long serialVersionUID = -321784417496157575L;
		{
			add(TAG_BEACH);
			add(TAG_MIAMI);
			add(TAG_WORK);
			add(TAG_CV);
			add(TAG_APPLICATION);
		}
	};
	protected static final Set<String> DEF_TAGS_MINUS_CV = new HashSet<String>() {
		private static final long serialVersionUID = 2811436662715165644L;
		{
			add(TAG_BEACH);
			add(TAG_MIAMI);
			add(TAG_WORK);
			add(TAG_APPLICATION);
		}
	};

	protected MockMvc mvc;
	@Autowired
	WebApplicationContext webApplicationContext;

	protected void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	protected String mapToJson(Object obj) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}

	protected <T> T mapFromJson(String json, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, clazz);
	}

}
