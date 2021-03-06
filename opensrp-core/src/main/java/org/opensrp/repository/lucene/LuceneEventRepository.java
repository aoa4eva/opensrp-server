package org.opensrp.repository.lucene;

import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.Event.ENTITY_TYPE;
import static org.opensrp.common.AllConstants.Event.EVENT_DATE;
import static org.opensrp.common.AllConstants.Event.EVENT_TYPE;
import static org.opensrp.common.AllConstants.Event.LOCATION_ID;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.opensrp.common.AllConstants.Event.TEAM;
import static org.opensrp.common.AllConstants.Event.TEAM_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.ldriscoll.ektorplucene.CouchDbRepositorySupportWithLucene;
import com.github.ldriscoll.ektorplucene.LuceneQuery;
import com.github.ldriscoll.ektorplucene.LuceneResult;
import com.github.ldriscoll.ektorplucene.designdocument.annotation.FullText;
import com.github.ldriscoll.ektorplucene.designdocument.annotation.Index;
import com.mysql.jdbc.StringUtils;

@FullText({
		@Index(name = "by_all_criteria", analyzer = "perfield:{baseEntityId:\"keyword\",locationId:\"keyword\"}", index = "function(doc) {   if(doc.type !== 'Event') return null;   var arr1 = ['baseEntityId','eventType','entityType','providerId','locationId','teamId','team'];   var ret = new Document(); var serverVersion = doc.serverVersion;ret.add(serverVersion, {'field': 'serverVersion'});  for (var i in arr1){     ret.add(doc[arr1[i]], {'field':arr1[i]});   }   if(doc.eventDate){     var bd=doc.eventDate.substring(0,19);      ret.add(bd, {'field':'eventDate','type':'date'});   }          var crd = doc.dateCreated.substring(0, 19);     ret.add(crd, {'field' : 'lastEdited','type' : 'date'});          if(doc.dateEdited){     var led = doc.dateEdited.substring(0, 19);     ret.add(led, {'field' : 'lastEdited','type' : 'date'});         }        return ret;   }"),
		@Index(name = "by_all_criteria_v2", analyzer = "perfield:{baseEntityId:\"keyword\",locationId:\"keyword\"}", index = "function(doc) {   if(doc.type !== 'Event') return null;   var arr1 = ['baseEntityId','eventType','entityType','providerId','locationId','teamId','team'];   var ret = new Document(); var serverVersion = doc.serverVersion;ret.add(serverVersion, {'field': 'serverVersion'});  for (var i in arr1){     ret.add(doc[arr1[i]], {'field':arr1[i]});   }   if(doc.eventDate){     var bd=doc.eventDate.substring(0,19);      ret.add(bd, {'field':'eventDate','type':'date'});   }          var crd = doc.dateCreated.substring(0, 19);     ret.add(crd, {'field' : 'lastEdited','type' : 'date'});          if(doc.dateEdited){     var led = doc.dateEdited.substring(0, 19);     ret.add(led, {'field' : 'lastEdited','type' : 'date'});         }        return ret;   }") })
@Component
public class LuceneEventRepository extends CouchDbRepositorySupportWithLucene<Event> {

	private LuceneDbConnector ldb;

	@Autowired
	protected LuceneEventRepository(LuceneDbConnector db) {
		super(Event.class, db);
		this.ldb = db;
		initStandardDesignDocument();
	}

	public List<Event> getByCriteria(String baseEntityId, DateTime eventDatefrom, DateTime eventDateto, String eventType,
	                                 String entityType, String providerId, String locationId, DateTime lastEditFrom,
	                                 DateTime lastEditTo,String team, String teamId) {
		// create a simple query against the view/search function that we've created
		LuceneQuery query = new LuceneQuery("Event", "by_all_criteria");

		Query qf = new Query(FilterType.AND);
		if (eventDatefrom != null && eventDateto != null) {
			qf.between(EVENT_DATE, eventDatefrom, eventDateto);
		}
		if (lastEditFrom != null && lastEditTo != null) {
			qf.between(LAST_UPDATE, lastEditFrom, lastEditTo);
		}
		if (!StringUtils.isEmptyOrWhitespaceOnly(baseEntityId)) {
			qf.eq(BASE_ENTITY_ID, baseEntityId);
		}
		if (!StringUtils.isEmptyOrWhitespaceOnly(eventType)) {
			qf.eq(EVENT_TYPE, eventType);
		}
		if (!StringUtils.isEmptyOrWhitespaceOnly(entityType)) {
			qf.eq(ENTITY_TYPE, entityType);
		}
		if (!StringUtils.isEmptyOrWhitespaceOnly(providerId)) {
			qf.eq(PROVIDER_ID, providerId);
		}
		if (!StringUtils.isEmptyOrWhitespaceOnly(locationId)) {
			qf.eq(LOCATION_ID, locationId);
		}
		if (!StringUtils.isEmptyOrWhitespaceOnly(team)) {
			qf.eq(TEAM, team);
		}
		if (!StringUtils.isEmptyOrWhitespaceOnly(teamId)) {
			qf.eq(TEAM_ID, teamId);
		}

		if (StringUtils.isEmptyOrWhitespaceOnly(qf.query())) {
			throw new RuntimeException("Atleast one search filter must be specified");
		}
		query.setQuery(qf.query());
		// stale must not be ok, as we've only just loaded the docs
		query.setStaleOk(false);
		query.setIncludeDocs(true);

		try {
			LuceneResult result = db.queryLucene(query);
			return ldb.asList(result, Event.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param providerId-   health worker id or comma separated health worker ids
	 * @param locationId
	 * @param baseEntityId
	 * @param serverVersion
	 * @param sortBy        Prefix with / for ascending order and \ for descending order (ascending is the
	 *                      default if not specified).
	 * @param sortOrder     either descending or ascending
	 * @param limit
	 * @param team          this is a comma separated string of team names
	 * @return
	 */
	public List<Event> getByCriteria(String team, String teamId,String providerId, String locationId, String baseEntityId,
	                                 Long serverVersion, String sortBy, String sortOrder, int limit) {
		// create a simple query against the view/search function that we've created
		LuceneQuery query = new LuceneQuery("Event", "by_all_criteria_v2");

		Query qf = new Query(FilterType.AND);

		if (serverVersion != null) {
			qf.between(BaseEntity.SERVER_VERSIOIN, serverVersion, Long.MAX_VALUE);
		}

		if (team != null && !StringUtils.isEmptyOrWhitespaceOnly(team)) {
			if (team.contains(",")) {
				String[] teamArray = org.apache.commons.lang.StringUtils.split(team, ",");
				List<String> teams = new ArrayList<>(Arrays.asList(teamArray));
				qf.inList(TEAM, teams);
			} else {
				qf.eq(TEAM, team);
			}
		}
		
		if (teamId != null && !StringUtils.isEmptyOrWhitespaceOnly(teamId)) {
			if (teamId.contains(",")) {
				String[] teamArray = org.apache.commons.lang.StringUtils.split(teamId);
				List<String> teams = new ArrayList<>(Arrays.asList(teamArray));
				qf.inList(TEAM_ID, teams);
			} else {
				qf.eq(TEAM_ID, teamId);
			}
		}
		
		if ((providerId != null && !StringUtils.isEmptyOrWhitespaceOnly(providerId))) {
			if (providerId.contains(",")) {
				String[] providerArray = org.apache.commons.lang.StringUtils.split(providerId, ",");
				List<String> providers = new ArrayList<>(Arrays.asList(providerArray));
				qf.inList(PROVIDER_ID, providers);
			} else {
				qf.eq(PROVIDER_ID, providerId);
			}
		}

		if (!StringUtils.isEmptyOrWhitespaceOnly(locationId)) {
			qf.eq(LOCATION_ID, locationId);
		}
		
		if (locationId != null || !StringUtils.isEmptyOrWhitespaceOnly(locationId)) {
			if (locationId.contains(",")) {
				String[] locationArray = org.apache.commons.lang.StringUtils.split(locationId, ",");
				List<String> locations = new ArrayList<>(Arrays.asList(locationArray));
				qf.inList(LOCATION_ID, locations);
			} else {
				qf.eq(LOCATION_ID, locationId);
			}
		}

		if (!StringUtils.isEmptyOrWhitespaceOnly(baseEntityId)) {
			if (baseEntityId.contains(",")) {
				Query q = new Query(FilterType.OR);
				String[] idsArray = org.apache.commons.lang.StringUtils.split(baseEntityId, ",");
				List<String> ids = new ArrayList<String>(Arrays.asList(idsArray));
				q.inList(BASE_ENTITY_ID, ids);

				qf.addToQuery(q);
			} else {
				qf.eq(BASE_ENTITY_ID, baseEntityId);
			}
		}

		if (StringUtils.isEmptyOrWhitespaceOnly(qf.query())) {
			throw new RuntimeException("Atleast one search filter must be specified");
		}
		query.setQuery(qf.query());
		// stale must not be ok, as we've only just loaded the docs
		query.setStaleOk(false);
		query.setIncludeDocs(true);
		query.setLimit(limit);
		query.setSort((sortOrder.toLowerCase().contains("desc") ? "\\" : "/") + sortBy);

		try {
			LuceneResult result = db.queryLucene(query);
			return ldb.asList(result, Event.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Event> getByCriteria(String query) {
		// create a simple query against the view/search function that we've created
		LuceneQuery q = new LuceneQuery("Event", "by_all_criteria");

		q.setQuery(query);
		// stale must not be ok, as we've only just loaded the docs
		q.setStaleOk(false);
		q.setIncludeDocs(true);

		try {
			LuceneResult result = db.queryLucene(q);
			return ldb.asList(result, Event.class);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
