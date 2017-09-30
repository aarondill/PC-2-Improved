package edu.csus.ecs.pc2.services.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.list.AccountComparator;
import edu.csus.ecs.pc2.core.list.ClarificationComparator;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.Clarification;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.SampleContest;
import edu.csus.ecs.pc2.core.util.AbstractTestCase;

/**
 * Unit Test.
 * 
 * @author Douglas A. Lane, PC^2 Team, pc2@ecs.csus.edu
 */
public class EventFeedJSONTest extends AbstractTestCase {

    /**
     * Unit tets data
     * 
     * @author Douglas A. Lane, PC^2 Team, pc2@ecs.csus.edu
     */
    class UnitTestData {

        private IInternalContest contest = new SampleContest().createStandardContest();

        private SampleContest sampleContest = new SampleContest();

        /**
         * Construct contest with accounts, runs, clars, etc.
         * 
         * @throws Exception
         */
        public UnitTestData() throws Exception {
            String[] runsData = {
                    "1,1,A,1,No,No,4", // 0 (a No before first yes Security Violation)
                    "2,1,A,1,No,No,2", // 0 (a No before first yes Compilation Error)
                    "3,1,A,1,No,No,1", // 20 (a No before first yes)
                    "4,1,A,3,Yes,No,0", // 3 (first yes counts Minute points but never Run Penalty points)
                    "5,1,A,5,No,No,1", // zero -- after Yes
                    "6,1,A,7,Yes,No,0", // zero -- after Yes
                    "7,1,A,9,No,No,1", // zero -- after Yes
                    "8,1,B,11,No,No,1", // zero -- not solved
                    "9,2,A,48,No,No,4", // 0 (a No before first yes Security Violation)
                    "10,2,A,50,Yes,No,0", // 50 (minute points; no Run points on first Yes)
                    "11,2,B,35,No,No,1", // zero -- not solved
                    "12,2,B,40,No,No,1", // zero -- not solved
            };

            // Assign half eams random team member names
            addTeamMembers(contest, getTeamAccounts(contest).length / 2, 5);

            assertEquals("Expectig team member names", 5, getFirstAccount(contest, Type.TEAM).getMemberNames().length);

            assertEquals("team count", 120, contest.getAccounts(Type.TEAM).size());

            for (String runInfoLine : runsData) {
                SampleContest.addRunFromInfo(contest, runInfoLine);
            }

            Problem problem = contest.getProblems()[0];
            Account judge = getFirstAccount(contest, Type.JUDGE);
            generateClarifications(contest, 20, problem, judge.getClientId(), false, false);
            generateClarifications(contest, 20, problem, judge.getClientId(), true, false);
            generateClarifications(contest, 20, problem, judge.getClientId(), true, true);

            sampleContest.assignSampleGroups(contest, "North Group", "South Group");

            assertEquals("Runs", 12, contest.getRuns().length);

        }

        /**
         * Get contest populated with test data.
         * @return
         */
        public IInternalContest getContest() {
            return contest;
        }

    }

    public void simpleTest() throws Exception {

        EventFeedJSON eventFeedJSON = new EventFeedJSON();
        IInternalContest contest = new SampleContest().createStandardContest();

        String json = eventFeedJSON.createJSON(contest);

        assertNotNull(json);

    }

    /**
     * Test every name and value that event feed JSON can output.
     * 
     */
    public void testCompleteEventFeed() throws Exception {

        UnitTestData data = new UnitTestData();
        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        String json = eventFeedJSON.createJSON(data.getContest());

        assertNotNull(json);

        //      System.out.println("debug JSON is:\n" + json);

        //        String filename = "/tmp/stuf.new.json";
        //        writeFile(new File(filename), json);

        validateJSON(json);

        String eventCounts[] = {
                //
                EventFeedJSON.CLARIFICATIONS_KEY + ": 60", //
                EventFeedJSON.CONTEST_KEY + ": 1", //
                EventFeedJSON.GROUPS_KEY + ": 2", //

                EventFeedJSON.JUDGEMENT_KEY + ":  12", //
                EventFeedJSON.JUDGEMENT_TYPE_KEY + ": 9", //
                EventFeedJSON.LANGUAGE_KEY + ":   6", //

                EventFeedJSON.PROBLEM_KEY + ":    6", //
                EventFeedJSON.RUN_KEY + ":    12", //
                EventFeedJSON.SUBMISSION_KEY + ": 12", //

                EventFeedJSON.TEAM_MEMBERS_KEY + ":  300", //
        };

        for (String line : eventCounts) {
            String[] fields = line.split(":");
            String name = fields[0];
            int value = Integer.parseInt(fields[1].trim());
            assertCountEvent(value, name, json);
        }

        assertMatchCount(24, "\"judgement_type_id\"", json);
        assertMatchCount(422, "\"icpc_id\"", json);

    }

    private void writeFile(File file, String string) throws FileNotFoundException, IOException {

        String[] lines = { string };
        Utilities.writeLinesToFile(file.getCanonicalPath(), lines);
        System.out.println("debug - Wrote file to " + file.getCanonicalPath());
    }

    /**
     * Add team member names to account.
     * 
     * @param contest
     * @param count number of accounts to add team names to.
     * @param numberOnMtea number of team names per team
     */
    private void addTeamMembers(IInternalContest contest, int count, int numberOnMtea) {

        Account[] accounts = getTeamAccounts(contest);

        for (int tid = 0; tid < accounts.length && tid < count; tid++) {

            Account account = accounts[tid];
            if (account.getMemberNames() == null || account.getMemberNames().length == 0) {

                List<String> names = new ArrayList<>();

                for (int i = 0; i < numberOnMtea; i++) {
                    String name = pickRandom(SampleContest.GIRL_NAMES);
                    names.add(name);
                }

                String[] newNames = (String[]) names.toArray(new String[names.size()]);
                account.setMemberNames(newNames);
            }
            contest.updateAccount(account);
        }
    }

    public void testPickRandom() throws Exception {

        for (int i = 0; i < 3000; i++) {
            String name = pickRandom(SampleContest.GIRL_NAMES);
            assertNotNull("Found null at iteration " + i, name);
        }
    }

    private static String pickRandom(String[] stringArray) {

        //      return stringArray[0]; // return first name

        Random random = new Random();
        int nameIndex = random.nextInt(stringArray.length);
        return stringArray[nameIndex];
    }

    private void assertMatchCount(int count, String regex, String json) {
        assertEquals("Expecting to find" + regex, count, matchCount(regex, json));
    }

    /**
     * Expect count of elementName in JSON.
     * 
     * @param exepectedCount
     * @param eleementName
     * @param json
     */
    private void assertCountEvent(int exepectedCount, String eleementName, String json) {

        //      System.out.println("debug "+eleementName+" "+matchEventCount(eleementName, json));
        assertEquals("For event '" + eleementName + "' expecting count", exepectedCount, matchEventCount(eleementName, json));
    }

    /**
     * Return count of events in json.
     * 
     * @param eleementName element to match
     * @param json
     * @return
     */
    private int matchEventCount(String eleementName, String json) {

        String regex = "\"event\":\"" + eleementName + "\"";

        return matchCount(regex, json);
    }

    private int matchCount(String regex, String json) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * Generate a clarification for every team accounts.
     * 
     * @param contest
     * @param problem
     * @param judgeId
     * @param answerAll add an answer for every generated clarification
     * @param sendToAll mark clarifications send to All.
     * @throws Exception
     */
    public static void generateClarifications(IInternalContest contest, int maxCountToCreate, Problem problem, ClientId judgeId, boolean answerAll, boolean sendToAll) throws Exception {
        // SOMEDAY move generateClarifications into SampleContest

        String judgesDefaultAnswer = "No response, read problem statement";

        Account[] accounts = getTeamAccounts(contest);

        int count = 0;

        for (Account account : accounts) {

            count++;
            if (count > maxCountToCreate) {
                break;
            }

            Clarification clarification = new Clarification(account.getClientId(), problem, "Why? from " + account);

            clarification = new Clarification(account.getClientId(), problem, "Why #2? from " + account);
            if (answerAll) {
                String answer = judgesDefaultAnswer;
                clarification.setAnswer(answer, judgeId, contest.getContestTime(), sendToAll);
            }
            try {
                contest.acceptClarification(clarification);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Get all team accounts (all sites).
     * 
     */
    public static Account[] getTeamAccounts(IInternalContest contest) {
        // SOMEDAY move getTeamAccounts into AccountsUtility class

        return getAllAccounts(contest, Type.TEAM);
    }

    /**
     * Get all first account
     */
    public static Account getFirstAccount(IInternalContest contest, ClientType.Type type) {
        // SOMEDAY move getFirstAccount into AccountsUtility class

        Account[] accounts = getAllAccounts(contest, type);
        return accounts[0];
    }

    /**
     * Get all  accounts (all sites).
     */
    public static Account[] getAllAccounts(IInternalContest contest, ClientType.Type type) {
        // SOMEDAY move getAllAccounts into AccountsUtility class

        Vector<Account> accountVector = contest.getAccounts(type);
        Account[] accounts = (Account[]) accountVector.toArray(new Account[accountVector.size()]);
        Arrays.sort(accounts, new AccountComparator());

        return accounts;
    }

    /**
     * TEst single teams JSON line.
     * 
     * @throws Exception
     */
    public void testTeamJSON() throws Exception {

        EventFeedJSON eventFeedJSON = new EventFeedJSON();
        IInternalContest contest = new SampleContest().createStandardContest();

        Account[] account = getAccounts(contest, Type.TEAM);

        String json = eventFeedJSON.getTeamJSON(contest, account[0]);
        json = wrapBrackets(json);

        //        System.out.println("debug team json = "+json);

        //  {"id":1, "icpc_id":"3001", "name":"team1", "organization_id": null}

        asertEqualJSON(json, "id", "1");
        asertEqualJSON(json, "name", "team1");
    }

    /**
     * Wrap with brackets
     * @param teamJSON
     * @return
     */
    private String wrapBrackets(String s) {
        return "{" + s + "}";
    }

    /**
     * Test single clarification JSON line.
     * 
     * @throws Exception
     */
    public void testClarificationJSON() throws Exception {

        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        IInternalContest contest = new UnitTestData().getContest();

        Clarification[] clarifications = contest.getClarifications();
        Arrays.sort(clarifications, new ClarificationComparator());

        Clarification clarification = clarifications[4];
        String json = eventFeedJSON.getClarificationJSON(contest, clarification);
        json = wrapBrackets(json);

        //        System.out.println("debug json = "+json);

        //      "id":1, "from_team_id":1, "to_team_id":1, "reply_to_id": null, "problem_id":-1, "text":"Why #2? from team1", "start_time": null, "start_contest_time":"0.000"

        asertEqualJSON(json, "id", "5");
        asertEqualJSON(json, "from_team_id", "5");
        asertEqualJSON(json, "text", "Why #2? from team5");
        asertEqualJSON(json, "reply_to_id", "null");

    }

    public void testContestJSON() throws Exception {
        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        IInternalContest contest = new UnitTestData().getContest();

        String json = eventFeedJSON.getContestJSONFields(contest);
        json = wrapBrackets(json);
        //        System.out.println("debug cont json = "+json);

        //         {"id":"Pdf9051a6-c092-4d3b-abda-04e362a60a77", "name":"Programming Contest", "formal_name":"Programming Contest", "start_time": null, "duration":"5:00:00", 
        // "scoreboard_freeze_duration":"01:00:00", "penalty_time":20, "state":{"state.running":false, "state.frozen":false, "state.final":false}}

        asertPresentJSON(json, "id");
        asertEqualJSON(json, "name", "Programming Contest");
        asertEqualJSON(json, "duration", "5:00:00");
        asertEqualJSON(json, "scoreboard_freeze_duration", "01:00:00");
        asertEqualJSON(json, "penalty_time", "20");

    }

    public void testProblemJSON() throws Exception {
        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        IInternalContest contest = new UnitTestData().getContest();

        Problem problem = contest.getProblems()[0];
        String json = eventFeedJSON.getProblemJSON(contest, problem, 3);
        json = wrapBrackets(json);

        //        System.out.println("debug prob json = "+json);

        // {"id":3, "label":"A", "name":"Sumit", "ordinal":3, "test_data_coun":0}

        asertEqualJSON(json, "id", "3");
        asertEqualJSON(json, "label", "A");
        asertEqualJSON(json, "name", "Sumit");
        asertEqualJSON(json, "ordinal", "3");
    }

    public void testLanguageJSON() throws Exception {
        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        IInternalContest contest = new UnitTestData().getContest();

        Language language = contest.getLanguages()[0];
        String json = eventFeedJSON.getLanguageJSON(contest, language, 3);
        json = wrapBrackets(json);

        //        System.out.println("debug lang json = "+json);

        //  {"id":3, "name":"Java"}

        asertEqualJSON(json, "id", "3");
        asertEqualJSON(json, "name", "Java");
    }

    /**
     * Assert that JSON field has value.
     * 
     * Parses JSON, compares expected valu eot actual value.
     * 
     * @param json
     * @param fieldName - field name
     * @param expectedValue - expected value
     */
    private void asertEqualJSON(String json, String fieldName, String expectedValue) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            JsonNode rootNode = objectMapper.readTree(json);
            String value = rootNode.path(fieldName).asText();
            assertEquals("Expected for field <" + fieldName + "> value", expectedValue, value);

        } catch (JsonParseException e) {
            System.out.println("Trouble trying to check " + e.getMessage()); // TODO  better message
            throw e;
        }

    }

    private void validateJSON(String json) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        String filename = "/tmp/stuf.validateJSON.json";
        writeFile(new File(filename), json);

        try {

            JsonNode rootNode = objectMapper.readTree(json);
            assertNotNull("Expecting parsed json root node, rootNode");

        } catch (JsonParseException e) {
            System.out.println(json);
            System.out.println("Trouble trying to validate JSON " + e.getMessage()); // TODO  better message
            throw e;
        }

    }

    /**
     * Assert that JSON field has a value/is preent
     * 
     * Parses JSON, compares expected valu eot actual value.
     * 
     * @param json
     * @param fieldName - field name
     */
    private void asertPresentJSON(String json, String fieldName) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            JsonNode rootNode = objectMapper.readTree(json);
            String value = rootNode.path(fieldName).asText();
            assertNotNull("Expected for field <" + fieldName + "> value", value);

        } catch (JsonParseException e) {
            System.out.println("Trouble trying to check " + e.getMessage()); // TODO better message 
            throw e;
        }

    }

    public void testconvertToMs() throws Exception {

        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        long ms;
        String hhmmss;

        hhmmss = "00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 0, ms);

        hhmmss = "00:00:00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 0, ms);

        hhmmss = "00:00:01";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 1000, ms);

        hhmmss = "34:00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 2040000, ms);

        hhmmss = "01:00:00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 3600000, ms);

        hhmmss = "04:12:00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 15120000, ms);

        hhmmss = "01:22:00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 4920000, ms);

        hhmmss = "45:00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 2700000, ms);

        hhmmss = "01:00:00";
        ms = eventFeedJSON.convertToMs(hhmmss);
        assertEquals("Expecting ms for " + hhmmss, 3600000, ms);
    }

    public void testStartAtContestEvent() throws Exception {

        UnitTestData data = new UnitTestData();
        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        String jsonBefore = new EventFeedJSON().createJSON(data.getContest());

        eventFeedJSON.setStartEventId(eventFeedJSON.getEventId(1));
        String json = eventFeedJSON.createJSON(data.getContest());

        assertNotNull(json);

        assertCountEvent(1, EventFeedJSON.CONTEST_KEY, json);

        assertEquals("Expected JSON length when started with event 1 (contest event) ", jsonBefore.length(), json.length());
    }

    public void testStartAtEvent40() throws Exception {

        UnitTestData data = new UnitTestData();
        EventFeedJSON eventFeedJSON = new EventFeedJSON();

        eventFeedJSON.setStartEventId(eventFeedJSON.getEventId(40));
        String json = eventFeedJSON.createJSON(data.getContest());

        //        System.out.println("debug after event 40  json = "+json);

        assertNotNull(json);

        assertMatchCount(501, "\"event\"", json);

    }

}
