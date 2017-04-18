package org.opensrp.register.mcare.report.mis1.familyPlanning.birthControlMethdoUsagesCalculation;


import org.opensrp.register.mcare.domain.Members;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class BirthControlMethodUsagesCalculator {

    private String birthControlMethodToCalculate;
    private int countOfTotalUsages;
    private int countOfNewUsages;
    private int countOfLeftUsagesButNoneTaken;
    private int countOfLeftUsagesButOtherTaken;


    private BirthControlMethodUsagesCalculator() {

    }

    protected BirthControlMethodUsagesCalculator(String birthControlMethodToCalculate) {
        this.birthControlMethodToCalculate = birthControlMethodToCalculate;
        this.initCountVariables();
    }

    public void initCountVariables() {
        this.countOfTotalUsages = 0;
        this.countOfNewUsages = 0;
        this.countOfLeftUsagesButOtherTaken = 0;
        this.countOfLeftUsagesButNoneTaken = 0;
    }

    public void calculate(Members member) {
        this.countOfTotalUsages += addToTheCountOfTotalUsages(member);
        this.countOfNewUsages += addToTheCountOfNewUsages(member);
        this.countOfLeftUsagesButNoneTaken += addToTheCountOfLeftUsagesButNoneTaken(member);
        this.countOfLeftUsagesButOtherTaken += addToTheCountOfLeftUsagesButOtherTaken(member);
    }


    public int totalUsages() {
        return this.countOfTotalUsages;
    }


    public int newUsages() {
        return countOfNewUsages;
    }


    public int unitTotal() {
        return 0;
    }


    public int leftUsagesButTakenNone() {
        return countOfLeftUsagesButNoneTaken;
    }


    public int leftUsagesButTakenOther() {
        return countOfLeftUsagesButOtherTaken;
    }


    private int addToTheCountOfTotalUsages(Members member) {
        boolean usingThisBirthControlMethodInMemberDetail =
                checkValueOfKeyIn(member.details(), Members.BIRTH_CONTROL_KEY, birthControlMethodToCalculate);
        if (usingThisBirthControlMethodInMemberDetail) {
            return 1;
        }
        return 0;
    }

    private int addToTheCountOfNewUsages(Members member) {
        boolean usingThisBirthControlMethodInMemberDetail =
                checkValueOfKeyIn(member.details(), Members.BIRTH_CONTROL_KEY, birthControlMethodToCalculate);
        if (usingThisBirthControlMethodInMemberDetail) {
            Map<String, String> previousMonthElcoFollowUpData = getPreviousMonthElcoFollowUp(member.elco_Followup());
            boolean firstElcoFollowUP = previousMonthElcoFollowUpData.isEmpty();
            if (!firstElcoFollowUP) {
                boolean usedThisBirthControlMethodInPreviousElcoFollowUp =
                        checkValueOfKeyIn(previousMonthElcoFollowUpData, Members.BIRTH_CONTROL_KEY,
                                birthControlMethodToCalculate);
                if (!usedThisBirthControlMethodInPreviousElcoFollowUp) {
                    return 1;
                }
            } else {
                return 1;
            }
        }
        return 0;
    }

    private int addToTheCountOfLeftUsagesButNoneTaken(Members member) {
        boolean notUsingAnyBirthControlMethodInMemberDetail =
                checkValueOfKeyIn(member.details(), Members.BIRTH_CONTROL_KEY, Members.BIRTH_CONTROL_NOT_USING_ANY_METHOD);
        boolean notUsingFamilyPlanningInMemberDetail =
                checkValueOfKeyIn(member.details(), Members.USING_FAMILY_PLANNING_KEY, Members.NOT_USING_FAMILY_PLANNING_VALUE);
        if (notUsingAnyBirthControlMethodInMemberDetail || notUsingFamilyPlanningInMemberDetail) {
            Map<String, String> previousMonthElcoFollowUpData = getPreviousMonthElcoFollowUp(member.elco_Followup());
            boolean usedThisBirthControlMethodInPreviousElcoFollowUp =
                    checkValueOfKeyIn(previousMonthElcoFollowUpData, Members.BIRTH_CONTROL_KEY,
                            birthControlMethodToCalculate);
            if (usedThisBirthControlMethodInPreviousElcoFollowUp) {
                return 1;
            }
        }

        return 0;
    }

    private int addToTheCountOfLeftUsagesButOtherTaken(Members member) {
        boolean usingThisBirthControlMethodInMemberDetail =
                checkValueOfKeyIn(member.details(), Members.BIRTH_CONTROL_KEY, birthControlMethodToCalculate);
        if (!usingThisBirthControlMethodInMemberDetail) {
            Map<String, String> previousMonthElcoFollowUpData = getPreviousMonthElcoFollowUp(member.elco_Followup());
            boolean usedThisBirthControlMethodInPreviousElcoFollowUp =
                    checkValueOfKeyIn(previousMonthElcoFollowUpData, Members.BIRTH_CONTROL_KEY,
                            birthControlMethodToCalculate);
            if (usedThisBirthControlMethodInPreviousElcoFollowUp) {
                String currentBirthControlMethod = getValueBasedOnKeyIn(member.details(), Members.BIRTH_CONTROL_KEY);
                if (checkIfValidBirthControlMethod(currentBirthControlMethod)) {
                    return 1;
                }
            }
        }
        return 0;
    }

    private Map<String, String> getPreviousMonthElcoFollowUp(List<Map<String, String>> allElcoFollowUp) {
        if (allElcoFollowUp.size() >= 2) {
            return allElcoFollowUp.get(allElcoFollowUp.size() - 2);
        } else {
            return Collections.<String, String>emptyMap();
        }
    }

    private boolean checkValueOfKeyIn(Map<String, String> memberData, String key,
                                      String expectedValue) {
        String birthControlValue = this.getValueBasedOnKeyIn(memberData, key);
        return birthControlValue.equalsIgnoreCase(expectedValue);
    }

    private String getValueBasedOnKeyIn(Map<String, String> memberData, String key) {
        if (memberData.containsKey(key)) {
            return memberData.get(key);
        } else {
            return Members.BIRTH_CONTROL_NULL_VALUE;
        }
    }

    private boolean checkIfValidBirthControlMethod(String birthControlMethod) {
        return !birthControlMethod.equalsIgnoreCase(Members.BIRTH_CONTROL_NULL_VALUE) && !birthControlMethod.equalsIgnoreCase(Members.BIRTH_CONTROL_NOT_USING_ANY_METHOD);
    }
}


