package org.ei.drishti.event;

import org.ei.drishti.dto.FormSubmission;
import org.motechproject.scheduler.domain.MotechEvent;

import java.util.HashMap;
import java.util.List;

public class FormSubmissionEvent {
    public static final String SUBJECT = "FORM-SUBMISSION";

    private List<FormSubmission> formSubmissions;

    public FormSubmissionEvent(List<FormSubmission> formSubmissions) {
        this.formSubmissions = formSubmissions;
    }

    public MotechEvent toEvent() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("data", formSubmissions);
        return new MotechEvent(SUBJECT, parameters);
    }
}
