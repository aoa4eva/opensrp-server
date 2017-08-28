package org.opensrp.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensrp.domain.*;
import org.opensrp.domain.Event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SampleFullDomainObject {

	//*** ADDRESS RELATED
	public static final String addressType = "addressType";

	public static final String country = "country";

	public static final String stateProvince = "stateProvince";

	public static final String cityVillage = "cityVillage";

	public static final String countryDistrict = "countryDistrict";

	public static final String subDistrict = "subDistrict";

	public static final String town = "town";
	//**

	public static final String name = "name";

	public static final String male = "male";

	public static final DateTime birthDate = new DateTime(0l, DateTimeZone.UTC);

	public static final DateTime deathDate = new DateTime(1l, DateTimeZone.UTC);

	public static final String FULL_NAME = "full name";

	public static Map<String, String> identifier = new HashMap<>();

	public static final Map<String, Object> attributes = new HashMap<>();

	public static final String IDENTIFIER_TYPE = "identifierType";

	public static final String IDENTIFIER_VALUE = "identifierValue";

	public static final String ATTRIBUTES_VALUE = "attributesValue";

	public static final String ATTRIBUTES_TYPE = "attributesType";

	static {
		identifier.put(IDENTIFIER_TYPE, IDENTIFIER_VALUE);
		attributes.put(ATTRIBUTES_TYPE, ATTRIBUTES_VALUE);
	}

	public static final String BASE_ENTITY_ID = "baseEntityId";

	public static final String DIFFERENT_BASE_ENTITY_ID = "differentBaseEntityId";

	public static final String FIRST_NAME = "firstName";

	public static final String MIDDLE_NAME = "middleName";

	public static final String LAST_NAME = "lastName";

	public static final boolean BIRTH_DATE_APPROX = true;

	public static final boolean DEATH_DATE_APPROX = false;

	public static final String FEMALE = "female";

	//** EVENT RELATED
	public static final String EVENT_TYPE = "eventType";

	public static final DateTime EVENT_DATE = new DateTime(0l, DateTimeZone.UTC);

	public static final String ENTITY_TYPE = "entityType";

	public static final String PROVIDER_ID = "providerId";

	public static final String LOCATION_ID = "locationId";

	public static final String FORM_SUBMISSION_ID = "formSubmissionId";

	public static final Map<String, String> DETAILS = new HashMap<>();

	public static final String DETAIL_KEY = "detailKey";

	public static final String DETAIL_VALUE = "detailValue";

	static {
		DETAILS.put(DETAIL_KEY, DETAIL_VALUE);
	}
	//**

	//** OBS RELATED
	public static final String CONCEPT = "concept";

	public static final String FIELD_DATA_TYPE = "fieldDataTyp";

	public static final String FIELD_CODE = "fieldCode";

	public static final String PARENT_CODE = null;

	public static final String VALUE = "value";

	public static final String COMMENTS_TEST = "commentsTest";

	public static final String FORM_SUBMISSION_FIELD = "formSubmissionField";
	//**

	//** ERROR TRACE RELATED
	public static final String RECORD_ID = "recordId";

	public static final DateTime EPOCH_DATE_TIME = new DateTime(0l, DateTimeZone.UTC);

	public static final String ERROR_NAME = "errorName";

	public static final String OCCURED_AT = "occuredAt";

	public static final String STACK_TRACE = "stackTrace";

	public static final String SOLVED = "solved";

	//** APP STATE TOKEN RELATED
	public enum AppStateTokenName {
		APP_STATE_TOKEN_NAME("appStateTokenName"), DIFFERENT_APP_STATE_TOKEN_NAME("differentAppStateToken");

		String value;

		AppStateTokenName(String value) {
			this.value = value;
		}

	}

	public static final int LAST_EDIT_DATE = 1222;

	public static final String APP_STATE_TOKEN_DESCRIPTION = "description";
	//**

	public static Address getAddress() {
		Address address = new Address().withAddressType(addressType).withCountry(country).withStateProvince(stateProvince)
				.withCityVillage(cityVillage).withCountyDistrict(countryDistrict).withSubDistrict(subDistrict)
				.withTown(town);
		return address;
	}

	public static Client getClient() {
		Client client = new Client(BASE_ENTITY_ID, FIRST_NAME, MIDDLE_NAME, LAST_NAME, birthDate, deathDate,
				BIRTH_DATE_APPROX, DEATH_DATE_APPROX, FEMALE, Collections.singletonList(getAddress()), identifier,
				attributes);
		return client;
	}

	public static Obs getObs() {
		Obs obs = new Obs(CONCEPT, FIELD_DATA_TYPE, FIELD_CODE, PARENT_CODE, VALUE, COMMENTS_TEST, FORM_SUBMISSION_FIELD);
		return obs;
	}

	public static Event getEvent() {
		Event event = new Event(BASE_ENTITY_ID, EVENT_TYPE, EVENT_DATE, ENTITY_TYPE, PROVIDER_ID, LOCATION_ID,
				FORM_SUBMISSION_ID);
		event.setIdentifiers(identifier);
		event.setDetails(DETAILS);
		event.addObs(getObs());
		return event;
	}

	public static AppStateToken getAppStateToken() {
		AppStateToken appStateToken = new AppStateToken(AppStateTokenName.APP_STATE_TOKEN_NAME.name(), VALUE, LAST_EDIT_DATE,
				APP_STATE_TOKEN_DESCRIPTION);
		return appStateToken;
	}

	public static ErrorTrace getErrorTrace() {
		ErrorTrace errorTrace = new ErrorTrace(RECORD_ID, EPOCH_DATE_TIME, ERROR_NAME, OCCURED_AT, STACK_TRACE, SOLVED);
		return errorTrace;
	}

	public static Provider getProvider() {
		Provider provider = new Provider(BASE_ENTITY_ID, FULL_NAME);
		return provider;
	}
}