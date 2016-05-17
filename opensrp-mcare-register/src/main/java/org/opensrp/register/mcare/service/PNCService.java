/**
 * @author julkar nain
 */

package org.opensrp.register.mcare.service;

import static java.text.MessageFormat.format;
import static org.opensrp.common.AllConstants.BnfFollowUpVisitFields.*;
import static org.opensrp.common.AllConstants.CommonFormFields.ID;
import static org.opensrp.common.AllConstants.HHRegistrationFields.*;
import static org.opensrp.common.AllConstants.ANCVisitOneFields.*;
import static org.opensrp.common.AllConstants.PNCVisitOneFields.*;
import static org.opensrp.common.AllConstants.PNCVisitTwoFields.*;
import static org.opensrp.common.AllConstants.PNCVisitThreeFields.*;
import static org.opensrp.common.AllConstants.DeliveryOutcomeFields.CHILD_REGISTRATION_SUB_FORM_NAME;
import static org.opensrp.common.AllConstants.ELCORegistrationFields.FW_GOBHHID;
import static org.opensrp.common.AllConstants.ELCORegistrationFields.FW_WOMFNAME;
import static org.opensrp.register.mcare.OpenSRPScheduleConstants.MotherScheduleConstants.SCHEDULE_PNC;
import static org.opensrp.register.mcare.OpenSRPScheduleConstants.MotherScheduleConstants.SCHEDULE_PNC_1;
import static org.opensrp.register.mcare.OpenSRPScheduleConstants.MotherScheduleConstants.SCHEDULE_PNC_2;
import static org.opensrp.register.mcare.OpenSRPScheduleConstants.MotherScheduleConstants.SCHEDULE_PNC_3;
import static org.opensrp.common.util.EasyMap.create;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opensrp.form.domain.FormSubmission;
import org.opensrp.form.domain.SubFormData;
import org.opensrp.register.mcare.domain.Elco;
import org.opensrp.register.mcare.domain.Mother;
import org.opensrp.register.mcare.domain.Child;
import org.opensrp.register.mcare.repository.AllChilds;
import org.opensrp.register.mcare.repository.AllElcos;
import org.opensrp.register.mcare.repository.AllMothers;
import org.opensrp.register.mcare.service.scheduling.ELCOScheduleService;
import org.opensrp.register.mcare.service.scheduling.ChildSchedulesService;
import org.opensrp.register.mcare.service.scheduling.PNCSchedulesService;
import org.opensrp.register.mcare.service.scheduling.ScheduleLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PNCService {

	private static Logger logger = LoggerFactory.getLogger(ANCService.class
			.toString());
	private AllElcos allElcos;
	private AllMothers allMothers;
	private AllChilds allChilds;
	private ELCOScheduleService elcoSchedulesService;
	private PNCSchedulesService pncSchedulesService;
	private ChildSchedulesService childSchedulesService;
	private ScheduleLogService scheduleLogService;

	@Autowired
	public PNCService(AllElcos allElcos, AllMothers allMothers, AllChilds allChilds, ELCOScheduleService elcoSchedulesService, PNCSchedulesService pncSchedulesService, ChildSchedulesService childSchedulesService,ScheduleLogService scheduleLogService) {
		this.allElcos = allElcos;
		this.allMothers = allMothers;
		this.allChilds = allChilds;
		this.elcoSchedulesService = elcoSchedulesService;
		this.pncSchedulesService = pncSchedulesService;
		this.childSchedulesService = childSchedulesService;
		this.scheduleLogService = scheduleLogService;
	}
	
	public void deliveryOutcome(FormSubmission submission) {
		String pattern = "yyyy-MM-dd";
		//DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
		
		DateTime dateTime = DateTime.parse(submission
				.getField(FWBNFDTOO));
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		String referenceDate = fmt.print(dateTime);

		if (submission.getField(FWBNFDTOO) != null) {
						
			Mother mother = allMothers.findByCaseId(submission.entityId());

			if (mother == null) {
				logger.warn(format(
						"Failed to handle PNC as there is no Mother enroll with ID: {0}",
						submission.entityId()));
				return;
			}
			
			Elco elco = allElcos.findByCaseId(mother.relationalid());
			
			if (elco != null) {
				logger.info("Closing EC case. Ec Id: "+ elco.caseId());
				elco.setIsClosed(false);
				allElcos.update(elco);
				elcoSchedulesService.imediateEnrollIntoMilestoneOfPSRF(elco.caseId(), elco.TODAY(), elco.PROVIDERID(),elco.INSTANCEID());
			}
								
			if (submission.getField(FWBNFSTS).equals(STS_WD)) {
				logger.info("Closing Mother as the mother died during delivery. Mother Id: "
						+ mother.caseId());
				closeMother(mother);
			} 
			else if (submission.getField(FWBNFSTS).equals(STS_LB)) {
				logger.info("Generating schedule for Mother when Child is Live Birth. Mother Id: "
						+ mother.caseId());
				logger.info("FWBNFWOMVITSTS:"+submission.getField(FWBNFWOMVITSTS));
				if(submission.getField(FWBNFWOMVITSTS).equalsIgnoreCase("0")){
					logger.info("Mother died");
				}else{					
					pncSchedulesService.enrollPNCRVForMother(submission.entityId(), LocalDate.parse(referenceDate));
					logger.info("Generating schedule for Child when Child is Live Birth. Mother Id: "
							+ mother.caseId());
				}
				SubFormData subFormData = submission.getSubFormByName(CHILD_REGISTRATION_SUB_FORM_NAME);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date today = Calendar.getInstance().getTime();								
				for (Map<String, String> childFields : subFormData.instances()) {
					
					Child child = allChilds.findByCaseId(childFields.get(ID))
						.withINSTANCEID(submission.instanceId())
						.withPROVIDERID(submission.anmId())
						.withTODAY(submission.getField(REFERENCE_DATE))
						.withSTART(submission.getField(START_DATE))
						.withEND(submission.getField(END_DATE))
						.withSUBMISSIONDATE(scheduleLogService.getTimeStampMills())
						.setIsClosed(false);					
					
			    	child.details().put(relationalid, childFields.get(relationalid));
			    	child.details().put(FWBNFGEN, childFields.get(FWBNFGEN));
			    	child.details().put(FWBNFCHLDVITSTS, childFields.get(FWBNFCHLDVITSTS));	
			    	child.details().put(received_time,formats.format(today).toString());
			    	child.details().put(FWBNFNAMECHECK, childFields.get(FWBNFNAMECHECK));
			    	child.details().put(FWBNFNAME, childFields.get(FWBNFNAME));
			    	child.details().put(FWBNFCHILDNAME, childFields.get(FWBNFCHILDNAME));	
			    	child.details().put(FWBNFDOB, childFields.get(FWBNFDOB));
			    	child.details().put("referenceDate", referenceDate);

					allChilds.update(child);
					logger.info("FWBNFCHLDVITSTS:"+childFields.get(FWBNFCHLDVITSTS));
					if(childFields.get(FWBNFCHLDVITSTS).equalsIgnoreCase("0")){
						logger.info("Child died");
					}else{
						childSchedulesService.enrollENCCForChild(childFields.get(ID),  LocalDate.parse(referenceDate));
					}
				}
				
							
				
			} 
			else if (submission.getField(FWBNFSTS).equals(STS_SB)) {
				if(submission.getField("user_type").equalsIgnoreCase("FD")){
					logger.info("Generating schedule for Mother when Child is Still Birth. Mother Id: "
							+ mother.caseId());
					pncSchedulesService.enrollPNCRVForMother(submission.entityId(),  LocalDate.parse(referenceDate));
				}else{
					logger.info("FWA submit form for Still Birth so nothing happend ");
				}
			}
		}
	}

	public void pncVisitOne(FormSubmission submission) {

		Mother mother = allMothers.findByCaseId(submission.entityId());

		if (mother == null) {
			logger.warn(format(
					"Failed to handle PNC-1 as there is no Mother enroll with ID: {0}",
					submission.entityId()));
			return;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date today = Calendar.getInstance().getTime();
		Map<String, String> pncVisitOne = create(FWPNC1DATE,
				submission.getField(FWPNC1DATE))
				.put(FWCONFIRMATION, submission.getField(FWCONFIRMATION))
				.put(FWPNC1REMSTS, submission.getField(FWPNC1REMSTS))
				.put(FWPNC1INT, submission.getField(FWPNC1INT))
				.put(FWPNC1KNWPRVDR, submission.getField(FWPNC1KNWPRVDR))
				.put(FWPNC1FVR, submission.getField(FWPNC1FVR))
				.put(FWPNC1TEMP, submission.getField(FWPNC1TEMP))
				.put(FWPNC1DNGRSIGN, submission.getField(FWPNC1DNGRSIGN))
				.put(FWPNC1DELTYPE, submission.getField(FWPNC1DELTYPE))
				.put(FWPNC1DELCOMP, submission.getField(FWPNC1DELCOMP))				
				.put(FW_GOBHHID, submission.getField(FW_GOBHHID))
				.put(FW_JiVitAHHID, submission.getField(FW_JiVitAHHID))
				.put(FW_WOMBID, submission.getField(FW_WOMBID))
				.put(FW_WOMNID, submission.getField(FW_WOMNID))
				.put(FW_WOMFNAME, submission.getField(FW_WOMFNAME))
				.put(FW_HUSNAME, submission.getField(FW_HUSNAME))
				.put(FWBNFDTOO, submission.getField(FWBNFDTOO))
				.put(FWBNFSTS, submission.getField(FWBNFSTS))
				.put(REFERENCE_DATE, submission.getField(REFERENCE_DATE))
				.put(START_DATE, submission.getField(START_DATE))
				.put(END_DATE, submission.getField(END_DATE))
				.put(pnc1_current_formStatus, submission.getField(pnc1_current_formStatus))
				.put(relationalid, submission.getField(relationalid))
				.put(user_type, submission.getField(user_type))
				.put(external_user_ID, submission.getField(external_user_ID))
				.put("received_time", format.format(today).toString())
				.map();

		mother.withPNCVisitOne(pncVisitOne);

		allMothers.update(mother);
		
		pncSchedulesService.fullfillMilestone(submission.entityId(), submission.anmId(), SCHEDULE_PNC, new LocalDate());
		
		String pattern = "yyyy-MM-dd";
		//DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
		
		DateTime dateTime = DateTime.parse(mother.getbnfVisitDetails(FWBNFDTOO));
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		String referenceDate = fmt.print(dateTime);
		
		pncSchedulesService.enrollPNCForMother(submission.entityId(), SCHEDULE_PNC_2, LocalDate.parse(referenceDate));
	}

	public void pncVisitTwo(FormSubmission submission) {
		Mother mother = allMothers.findByCaseId(submission.entityId());

		if (mother == null) {
			logger.warn(format(
					"Failed to handle PNC-2 as there is no Mother enroll with ID: {0}",
					submission.entityId()));
			return;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date today = Calendar.getInstance().getTime();
		Map<String, String> pncVisitTwo = create(FWPNC2DATE,
				submission.getField(FWPNC2DATE))
				.put(FWCONFIRMATION, submission.getField(FWCONFIRMATION))
				.put(FWPNC2REMSTS, submission.getField(FWPNC2REMSTS))
				.put(FWPNC2INT, submission.getField(FWPNC2INT))
				.put(FWPNC2KNWPRVDR, submission.getField(FWPNC2KNWPRVDR))
				.put(FWPNC2FVR, submission.getField(FWPNC2FVR))
				.put(FWPNC2TEMP, submission.getField(FWPNC2TEMP))
				.put(FWPNC2DNGRSIGN, submission.getField(FWPNC2DNGRSIGN))
				.put(FWPNC2DELCOMP, submission.getField(FWPNC2DELCOMP))
				.put(FW_GOBHHID, submission.getField(FW_GOBHHID))
				.put(FW_JiVitAHHID, submission.getField(FW_JiVitAHHID))
				.put(FW_WOMBID, submission.getField(FW_WOMBID))
				.put(FW_WOMNID, submission.getField(FW_WOMNID))
				.put(FW_WOMFNAME, submission.getField(FW_WOMFNAME))
				.put(FW_HUSNAME, submission.getField(FW_HUSNAME))
				.put(FWBNFDTOO, submission.getField(FWBNFDTOO))
				.put(FWBNFSTS, submission.getField(FWBNFSTS))
				.put(REFERENCE_DATE, submission.getField(REFERENCE_DATE))
				.put(START_DATE, submission.getField(START_DATE))
				.put(END_DATE, submission.getField(END_DATE))
				.put(pnc2_current_formStatus, submission.getField(pnc2_current_formStatus))
				.put(relationalid, submission.getField(relationalid))
				.put(user_type, submission.getField(user_type))
				.put(external_user_ID, submission.getField(external_user_ID))
				.put("received_time", format.format(today).toString())
				.map();

		mother.withPNCVisitTwo(pncVisitTwo);

		allMothers.update(mother);

		pncSchedulesService.fullfillMilestone(submission.entityId(), submission.anmId(), SCHEDULE_PNC, new LocalDate());
		
		String pattern = "yyyy-MM-dd";
		//DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
		
		DateTime dateTime = DateTime.parse(mother.getbnfVisitDetails(FWBNFDTOO));
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		String referenceDate = fmt.print(dateTime);
		
		pncSchedulesService.enrollPNCForMother(submission.entityId(), SCHEDULE_PNC_3, LocalDate.parse(referenceDate));
	}

	public void pncVisitThree(FormSubmission submission) {

		Mother mother = allMothers.findByCaseId(submission.entityId());

		if (mother == null) {
			logger.warn(format(
					"Failed to handle PNC-3 as there is no Mother enroll with ID: {0}",
					submission.entityId()));
			return;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date today = Calendar.getInstance().getTime();
		Map<String, String> pncVisitThree = create(FWPNC3DATE,
				submission.getField(FWPNC3DATE))
				.put(FWCONFIRMATION, submission.getField(FWCONFIRMATION))
				.put(FWPNC3REMSTS, submission.getField(FWPNC3REMSTS))
				.put(FWPNC3INT, submission.getField(FWPNC3INT))
				.put(FWPNC3KNWPRVDR, submission.getField(FWPNC3KNWPRVDR))
				.put(FWPNC3FVR, submission.getField(FWPNC3FVR))
				.put(FWPNC3TEMP, submission.getField(FWPNC3TEMP))
				.put(FWPNC3DNGRSIGN, submission.getField(FWPNC3DNGRSIGN))
				.put(FWPNC3DELCOMP, submission.getField(FWPNC3DELCOMP))
				.put(FW_GOBHHID, submission.getField(FW_GOBHHID))
				.put(FW_JiVitAHHID, submission.getField(FW_JiVitAHHID))
				.put(FW_WOMBID, submission.getField(FW_WOMBID))
				.put(FW_WOMNID, submission.getField(FW_WOMNID))
				.put(FW_WOMFNAME, submission.getField(FW_WOMFNAME))
				.put(FW_HUSNAME, submission.getField(FW_HUSNAME))
				.put(FWBNFDTOO, submission.getField(FWBNFDTOO))
				.put(FWBNFSTS, submission.getField(FWBNFSTS))
				.put(REFERENCE_DATE, submission.getField(REFERENCE_DATE))
				.put(START_DATE, submission.getField(START_DATE))
				.put(END_DATE, submission.getField(END_DATE))
				.put(pnc3_current_formStatus, submission.getField(pnc3_current_formStatus))
				.put(relationalid, submission.getField(relationalid))
				.put(user_type, submission.getField(user_type))
				.put(external_user_ID, submission.getField(external_user_ID))
				.put("received_time", format.format(today).toString())
				.map();

		mother.withPNCVisitThree(pncVisitThree);

		allMothers.update(mother);
		
		pncSchedulesService.unEnrollFromSchedule(submission.entityId(), submission.anmId(), SCHEDULE_PNC);
	}
	
	public void pncClose(String entityId) {
		
		Mother mother = allMothers.findByCaseId(entityId);
		
		 if (mother == null) {
	            logger.warn("Tried to close case without registered mother for case ID: " + entityId);
	            return;
	        }

		 allMothers.close(entityId);
		
		 pncSchedulesService.unEnrollFromAllSchedules(entityId);
	}
	
	public void closeMother(Mother mother) {

		mother.setIsClosed(true);
		allMothers.update(mother);
		pncSchedulesService.unEnrollFromAllSchedules(mother.caseId());

		Elco elco = allElcos.findByCaseId(mother.relationalid());
		if (elco != null) {
			logger.info("Closing EC case along with PNC case. Ec Id: "+ elco.caseId());
			elco.setIsClosed(true);
			allElcos.update(elco);
		}
	}
	
	public void deleteBlankChild(FormSubmission submission){
		SubFormData subFormData = submission.getSubFormByName(CHILD_REGISTRATION_SUB_FORM_NAME);
		for (Map<String, String> childFields : subFormData.instances()) {			
			Child child = allChilds.findByCaseId(childFields.get(ID));
			if (child != null) {
				allChilds.remove(child);
			}
		}
		
	}
}
