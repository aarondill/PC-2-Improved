// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.execute;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;

import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.log.LogUtilities;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Judgement;
import edu.csus.ecs.pc2.core.model.JudgementRecord;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;

/**
 * Judgement utilities.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public final class JudgementUtilites {

    /**
     * 
     */
    private JudgementUtilites() {
        super();
    }

    /**
     * Create judgementRecord, create record based on execution results.
     * 
     * @param contest
     * @param run
     * @param executionData
     * @param validationResults
     * @return
     */
    public static JudgementRecord createJudgementRecord(IInternalContest contest, Run run, ExecutionData executionData, String validationResults) {

        JudgementRecord judgementRecord = null;

        if (executionData.getExecutionException() != null) {

            // Some sort of JE or execution error.

            // Default to a "wrong answer" judgment that cannot have its scoring properties changed 
            // (i.e., always applies penalty)

            ElementId elementId = contest.getJudgements()[2].getElementId();
            judgementRecord = new JudgementRecord(elementId, contest.getClientId(), false, true, true);

            //see if we can find a "Judging Error" judgment, if so use that
            //TODO: NOTE: this code block seems useless, since variable "judgment" is never used
            Judgement judgement = findJudgementByAcronym(contest, "JE");
            if (judgement == null) {
                //we didn't find a JE; choose a non-variable-penalty "no" judgment
                judgement = contest.getJudgements()[2];
            }

            judgementRecord.setValidatorResultString("Execption during execution " + executionData.getExecutionException().getMessage());

        } else if (!executionData.isCompileSuccess()) {
            // Compile failed, darn!

            ElementId elementId = contest.getJudgements()[1].getElementId();
            judgementRecord = new JudgementRecord(elementId, contest.getClientId(), false, true, true);
            // TODO this needs to be flexible
            judgementRecord.setValidatorResultString("No - Compilation Error");

        } else if (executionData.isValidationSuccess()) {

            // We got stuff from validator!!
            String results = validationResults;

            if (results == null) {
                results = "Undetermined";
            } else {
                results = results.trim();
            }

            if (results.length() == 0) {
                results = "Undetermined";
            }

            boolean solved = false;

            // Try to find result text in judgement list
            //  (default to a non-variable-penalty "no" judgment to start)
            ElementId elementId = contest.getJudgements()[2].getElementId();
            for (Judgement judgement : contest.getJudgements()) {
                if (judgement.getDisplayName().trim().equalsIgnoreCase(results)) {
                    //found a matching judgment; use that instead of the default
                    elementId = judgement.getElementId();
                }
            }

            // Or perhaps it is a yes? yes?
            Judgement yesJudgement = contest.getJudgements()[0];
            // bug 280 ICPC Validator Interface Standard calls for "accepted" in any case.
            if (results.equalsIgnoreCase("accepted")) {
                results = yesJudgement.getDisplayName();
            }
            if (yesJudgement.getDisplayName().equalsIgnoreCase(results)) {
                elementId = yesJudgement.getElementId();
                solved = true;
            }

            judgementRecord = new JudgementRecord(elementId, contest.getClientId(), solved, true, true);
            judgementRecord.setValidatorResultString(results);

        } else {
            // Something went wrong either during validation or execution
            // Unable to validate result: Undetermined

            //default to a non-variable-scoring "no" judgment
            ElementId elementId = contest.getJudgements()[2].getElementId();
            judgementRecord = new JudgementRecord(elementId, contest.getClientId(), false, true, true);
            judgementRecord.setValidatorResultString("Undetermined");

        }

        return judgementRecord;
    }

    private static Judgement findJudgementByAcronym(IInternalContest contest, String acronym) {

        Judgement[] judgements = contest.getJudgements();
        for (Judgement judgement : judgements) {
            if (judgement.getAcronym().equals(acronym)) {
                return judgement;
            }
        }

        return null;
    }

    public static void dumpJudgementResultsToLog(Log log, ClientId judgeId, Run run, String executeDirctoryName, Problem problem, ExecutionData executionData, String prefixString, Properties properties) {

        // TODO handle max number of AC/Yes test cases to dump

        int maximumLinesToOutput = 20; // TODO assign max lines to dump from properties

        // For reference, Sample file list from multiple test case problem
        //    cstderr.pc2
        //    cstdout.pc2
        //    estderr.pc2
        //    estdout.pc2
        //    
        //    teamoutput.0.txt
        //    teamoutput.1.txt
        //    teamoutput.2.txt
        //    
        //    NO stderr
        //    
        //    valerr.0.txt
        //    valerr.1.txt
        //    valerr.2.txt

        try {

            int numberOfTestCases = problem.getNumberTestCases();
            log.info(prefixString + " Dumping for " + run + " judge id=" + judgeId);
            log.info(prefixString + " There are " + numberOfTestCases + " for problem " + problem.getDisplayName() + " '" + problem.getShortName() + "'");

            String[] otherFiles = { "cstderr.pc2", "cstdout.pc2", "estderr.pc2", "estdout.pc2" };
            
            /**
             * Other Files
             */

            for (String otherName : otherFiles) {
                String fullName = executeDirctoryName + File.separator + otherName;
                if (new File(fullName).isFile())
                {
                    LogUtilities.addFileToLog(log, fullName, maximumLinesToOutput, prefixString);
                }
                else
                {
                    log.info(prefixString + " File does not exist " + fullName);
                }
            }
            
            /*
             * Test case files.
             */

            for (int dataSetNumber = 0; dataSetNumber < numberOfTestCases; dataSetNumber++) {

                String teamOutputFilename = "teamoutput." + dataSetNumber + ".txt";
                String fullName = executeDirctoryName + File.separator + teamOutputFilename;

                if (new File(fullName).isFile())
                {
                    LogUtilities.addFileToLog(log, fullName, maximumLinesToOutput, prefixString);
                }
                else
                {
                    log.info(prefixString + " File does not exist " + fullName);
                }
            }
            
            if (executionData != null)
            {
                Exception ex = executionData.getExecutionException();
                if (ex != null)
                {
                    log.log(Level.INFO, "Exception "+ex.getMessage(), ex);
                }
            }
            
            
            log.info(prefixString + "end of dump, run id="+run.getNumber());

        } catch (Exception e) {
            log.log(Log.WARNING, prefixString + "Warning - while dumping result files " + e.getMessage(), e);
        }
    }

    public static String getExecuteDirectoryName(ClientId id) {
        return "executesite" + id.getSiteNumber() + id.getName();
    }

}
