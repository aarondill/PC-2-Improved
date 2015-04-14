package edu.csus.ecs.pc2.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JOptionPane;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.log.StaticLog;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import edu.csus.ecs.pc2.core.report.IReport;
import edu.csus.ecs.pc2.ui.FrameUtilities;
import edu.csus.ecs.pc2.ui.MultipleFileViewer;

/**
 * Various common routines.
 *
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public final class Utilities {
    
    private static boolean debugMode = false;
    /**
     * LETTERS is a lookup always 1-26
     * String.substring starts at 0, so letters begin at 1
     */
    private static final String LETTERS = " ABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
    
    public static final String DATE_TIME_FORMAT_STRING = "yyyyddMMhhmmss.SSS";
    
    /**
     * CCS directory where data files are stored.
     */
    private static final String SECRET_DATA_DIR = "data" + File.separator + "secret";

    private static SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
    
    /**
     * File Types.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    
    // $HeadURL$
    public enum  DataFileType {
        /**
         * Judge's input/test data file.
         */
        JUDGE_DATA_FILE,
        /**
         * Judge's solution/answer file.
         */
        JUDGE_ANSWER_FILE,
    }

    /**
     * Constructor is private as this is a utility class which
     * should not be extended or invoked.
     */
    private Utilities() {
        super();
    }

    /**
     * Insure directory exists, if does not exist create it.
     *
     * @param dirName
     *            directory to create.
     * @return whether directory exists.
     */
    public static boolean insureDir(String dirName) {
        File dir = null;

        // insure that the ./newExecute directory is there
        dir = new File(dirName);
        if (!dir.exists() && !dir.mkdir()) {
            // TODO show user that couldn't create this directory
            System.out.println("insureDir Directory " + dir.getName()
                    + " could not be created.");
        }

        return dir.isDirectory();
    }

    /**
     * Read serialized object from file.
     *
     * @param filename
     * @return the object
     * @throws ClassNotFoundException
     * @throws IOException
     */
//    public static Object readObjectFromFile(String filename)
//            throws IOException, ClassNotFoundException {
//        
//        try {
//            return FileSecurity.readSealedFile(filename);
//        } catch (Exception e) {
//            throw new IOException(e.getMessage());
//        }
//    }

    /**
     *
     * @param filename
     * @param serializable
     * @return true, otherwise throws an exception
     * @throws IOException
     */
//    public static boolean writeObjectToFile(String filename,
//            Serializable serializable) throws IOException {
//        try {
//            FileSecurity.writeSealedFile(filename, serializable);
//        } catch (Exception e) {
//            throw new IOException(e.getMessage());
//        }
//        return true;
//    }

    /**
     *
     * @param filename
     *            String name of file
     * @return boolean true if file exists.
     */
    public static boolean isFileThere(String filename) {
        File file = new File(filename);
        return file.isFile();
    }

    /**
     * Compares 2 char arrays for equality.
     * 
     * @param oldBuffer
     * @param newBuffer
     * @return true if oldBuffer is the same size and has the same contents of newBuffer
     */
    public static boolean isEquals(char[] oldBuffer, char[] newBuffer) {
        if (oldBuffer == null) {
            return(newBuffer == null);
        } else if (newBuffer == null) {
            // oldBuffer not null, but new buffer is
            return false;
        }
        // sizes no not match
        if (oldBuffer.length != newBuffer.length) {
            return false;
        }
        for (int i = 0; i < newBuffer.length; i++) {
            if (newBuffer[i] != oldBuffer[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns lines from file.
     * 
     * @param filename
     *            String file to load
     * @return lines from file
     * @throws IOException
     */
    public static String[] loadFile(String filename) throws IOException {
        Vector<String> lines = new Vector<String>();

        if (filename == null) {
            throw new IllegalArgumentException("filename is null");
        }

        if (!new File(filename).exists()) {
            return new String[0];
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
        String line = in.readLine();
        while (line != null) {
            lines.addElement(line);
            line = in.readLine();
        }
        in.close();
        in = null;

        if (lines.size() == 0) {
            return new String[0];
        }

        String[] out = new String[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            out[i] = lines.elementAt(i);
        }

        return out;

    }

    /**
     * Get Current Working Directory.
     * @return current working directory.
     */
    public static String getCurrentDirectory() {
        File curdir = new File(".");

        try {
            return curdir.getCanonicalPath();
        } catch (Exception e) {
            // ignore exception
            return ".";
        }
    }
    
    /**
     * @return the current dateTime in a local-sensitive manner in the full date/long time style
     */
    public static String getL10nDateTime() {
        Locale currentLocale = Locale.getDefault();
        Date today = new Date();
        DateFormat dateFormatter;
        dateFormatter = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG, currentLocale);
        return(dateFormatter.format(today));
    }
    
    /**
     * @param dateStyle
     * @param timeStyle
     * @param currentLocale
     * @return current date in localization format
     */
    public static String getL10nDateTime(int dateStyle, int timeStyle, Locale currentLocale) {
        Date today = new Date();
        DateFormat dateFormatter;
        dateFormatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, currentLocale);
        return(dateFormatter.format(today));
    }
    
    /**
     * Returns a date-time String as defined by RFC 2822 section 3.3
     * Useful for mail messages.
     * 
     * @return the date in rfc2822 format
     */
    public static String getRFC2822DateTime() {
        Calendar calendar = new GregorianCalendar();
        // per rfc 2822
        // date-time = [ day-of-week "," ] date time CFWS
        //date-time       =       [ day-of-week "," ] date FWS time [CFWS]
        //day-of-week     =       ([FWS] day-name) / obs-day-of-week
        //day-name        =       "Mon" / "Tue" / "Wed" / "Thu" /
        //                        "Fri" / "Sat" / "Sun"
        //date            =       day month year
        //year            =       4*DIGIT / obs-year
        //month           =       (FWS month-name FWS) / obs-month
        //month-name      =       "Jan" / "Feb" / "Mar" / "Apr" /
        //                        "May" / "Jun" / "Jul" / "Aug" /
        //                        "Sep" / "Oct" / "Nov" / "Dec"
        //day             =       ([FWS] 1*2DIGIT) / obs-day
        //time            =       time-of-day FWS zone
        //time-of-day     =       hour ":" minute [ ":" second ]
        //hour            =       2DIGIT / obs-hour
        //minute          =       2DIGIT / obs-minute
        //second          =       2DIGIT / obs-second
        //zone            =       (( "+" / "-" ) 4DIGIT) / obs-zone

        // Formatter Date/Time Conversions
        // 'a '   Locale-specific short name of the day of the week, e.g. "Sun", "Mon"
        // 'e'    Day of month, formatted as two digits, i.e. 1 - 31.
        // 'b'    Locale-specific abbreviated month name, e.g. "Jan", "Feb".
        // 'Y'    Year, formatted as at least four digits with leading zeros as necessary, e.g. 0092 equals 92 CE for the Gregorian calendar.
        // 'T'    Time formatted for the 24-hour clock as "%tH:%tM:%tS".
        // 'z'    RFC 822  style numeric time zone offset from GMT, e.g. -0800.
        return String.format("%1$ta, %1$te %1$tb %1$tY %1$tT %1$tz", calendar);
    }

    /**
     * Returns Yes if true, No if false.
     * @param b
     * @return Yes or No
     */
    public static String yesNoString(boolean b) {
        if (b) {
            return "Yes";
        } else {
            return "No";
        }
    }

    /**
     * Load INI file.
     * 
     * This will read a text file and strip out blank/empty lines
     * and lines that start with a hash mark.
     * <P>
     * This will also trim the input lines.
     * 
     * @param filename file to be read
     * @return String [] null if can't read/find file, else lines infile
     */
    public static String[] loadINIFile(String filename) {

        Vector<String> v = new Vector<String>();

        try {
            RandomAccessFile file = new RandomAccessFile(filename, "r");
            String line;

            while ((line = file.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.charAt(0) != '#') {
                        v.addElement(line);
                    }
                }
            }

            file.close();
            file = null;
        } catch (Exception e) {
            return null;
        }
        
        return (String[]) v.toArray(new String[v.size()]);
    }

    
    public static String forHTML(String aText) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '&') {
                result.append("&amp;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '(') {
                result.append("&#040;");
            } else if (character == ')') {
                result.append("&#041;");
            } else if (character == '#') {
                result.append("&#035;");
            } else if (character == '%') {
                result.append("&#037;");
            } else if (character == ';') {
                result.append("&#059;");
            } else if (character == '+') {
                result.append("&#043;");
            } else if (character == '-') {
                result.append("&#045;");
            } else if (character == '\n') {
                result.append("<br>");
            } else {
                // the char is not a special one
                // add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        Utilities.debugMode = debugMode;
    }
    
    public static String basename(String path) {
        int lastIndex = path.lastIndexOf(File.separator);
        if (lastIndex == -1) {
            return path;
        } else {
            return path.substring(lastIndex + 1);
        }
    }

    /**
     * Converts index > 0 to an uppercase letter, eg 1 => 'A'
     *
     * @param index > 0 and index < 703 (A..ZZ)
     * @return letter corresponding to number (base 26)
     */
    public static String convertNumber(int index) {
        String letter = String.valueOf(index);
        if (index > 26) {
            int i2=index;
            int count = 0;
            while(index > 26) {
                index = index - 26;
                count++;
            }
            int mod=i2 -(count * 26);
            letter = convertBase26Number(count)+convertBase26Number(mod);
        } else {
            letter = convertBase26Number(index);
        }
        return letter;
    }

    private static String convertBase26Number(int index) {
        return LETTERS.substring(index, index+1);
    }

    public static String dirname(String path) {
        int lastIndex = path.lastIndexOf(File.separator);
        if (lastIndex == -1) {
            return path;
        } else if (lastIndex == 0){
            return path;
        } else {
            return path.substring(0, lastIndex);
        }
    }

    public static String getReportFilename (IReport selectedReport) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd.SSS");
        // "yyMMdd HHmmss.SSS");
        String reportName = selectedReport.getReportTitle();
        
        while (reportName.indexOf(' ') > -1){
            reportName = reportName.replace(" ", "_");
        }
        return "report."+ reportName+ "." + simpleDateFormat.format(new Date()) + ".txt";

    }
    
    /**
     * Create/Write a report to file.
     * 
     * @param report
     *            IReport to write
     * @param contest
     * @param controller
     * @return name of created report file.
     * @throws FileNotFoundException
     */
    public static String createReport(IReport report, IInternalContest contest, IInternalController controller, boolean printHeaderAndFooter) throws FileNotFoundException {

        String filename = getReportFilename(report);

        report.setContestAndController(contest, controller);

        PrintWriter printWriter = null;
        printWriter = new PrintWriter(new FileOutputStream(filename, false), true);

        try {

            if (printHeaderAndFooter) {

                printReportHeader(printWriter, report, contest);

            }

            try {
                report.writeReport(printWriter);
            } catch (Exception e) {
                printWriter.println();
                printWriter.println("Exception in report: " + e.getMessage());
                e.printStackTrace(printWriter);
            }

            if (printHeaderAndFooter) {

                report.printFooter(printWriter);

            }

        } catch (Exception e) {
            controller.getLog().log(Log.INFO, "Exception creating report", e);
            printWriter.println("Exception creating report " + e.getMessage());
            e.printStackTrace(printWriter);
        } finally {
            printWriter.close();
            printWriter = null;
        }
        return filename;
    }
    
    private static void printReportHeader(PrintWriter printWriter, IReport report, IInternalContest contest) {

        printWriter.println(new VersionInfo().getSystemName());
        printWriter.println(new VersionInfo().getSystemVersionInfo());
        ContestInformation contestInformation = contest.getContestInformation();
        String contestTitle = "(Contest title not defined)";

        if (contestInformation != null) {
            contestTitle = contestInformation.getContestTitle();
        }
        printWriter.println("Contest Title: " + contestTitle);
        printWriter.println("On: " + Utilities.getL10nDateTime());
        GregorianCalendar resumeTime = null;
        if (contest.getContestTime() != null) {
            resumeTime = contest.getContestTime().getResumeTime();
        }
        if (resumeTime == null) {
            printWriter.print("  Contest date/time: never started");
        } else {
            printWriter.print("  Contest date/time: " + resumeTime.getTime());

        }
        printWriter.println();
        printWriter.println();
        printWriter.println("** " + report.getReportTitle() + " Report");
        printWriter.println("Site " + contest.getSiteNumber());
        printWriter.println();

    }

    /**
     * Create and view report.
     * 
     * @param report
     * @param title title for tab in pane for this report
     * @param contest
     * @param controller
     */
    public static void viewReport(IReport report, String title, IInternalContest contest, IInternalController controller, boolean printHeaderAndFooter) {

        try {
            String filename = createReport(report, contest, controller, printHeaderAndFooter);

            MultipleFileViewer multipleFileViewer = new MultipleFileViewer(controller.getLog());
            multipleFileViewer.addFilePane(title, filename);
            multipleFileViewer.setTitle("PC^2 Report (Build " + new VersionInfo().getBuildNumber() + ")");
            FrameUtilities.centerFrameFullScreenHeight(multipleFileViewer);
            multipleFileViewer.setVisible(true);
        } catch (FileNotFoundException e) {

            JOptionPane.showMessageDialog(null, "Unable to show report: " + e.getMessage());
            if (StaticLog.getLog() != null) {
                StaticLog.getLog().log(Log.WARNING, "Unable to show/view report", e);
            }
        }
    }

    /**
     * Dump String array.
     * 
     * @param out
     *            stream to output to
     * @param prefix
     *            prefix each line with this string
     * @param lines
     *            lines to be printed
     * @param prefixWithLineNumber
     *            after prefix output linenumber
     */
    public static void dumpStringArray(PrintStream out, String prefix, String[] lines, boolean prefixWithLineNumber) {

        int counter = 1;
        for (String string : lines) {
            out.print(prefix);
            if (prefixWithLineNumber) {
                out.printf("%3d: ", counter);
            }
            out.println(string);
            counter++;
        }
    }

    public static void debugPrint(String s) {
        if (debugMode){
            System.err.println(new Date() + " debug: "+s);
            System.err.flush();
        }
    }

    public static void debugPrint(Exception e) {
        if (debugMode){
            System.err.println(new Date() + " debug: Exception "+e.getMessage());
            e.printStackTrace(System.err);
            System.err.flush();
        }
    }

    /**
     * View report
     * 
     * @see #viewReport(IReport, String, IInternalContest, IInternalController, boolean)
     * @param report
     * @param title
     * @param contest
     * @param controller
     */
    public static void viewReport(IReport report, String title, IInternalContest contest, IInternalController controller) {
       viewReport(report, title, contest, controller, true);
    }
    


    /**
     * Create disk file for input SerializedFile.
     * 
     * Returns true if file is written to disk and is not null.
     * 
     * @param file
     * @param outputFileName
     * @return true if file written to disk.
     * @throws IOException
     */
    public static boolean createFile(SerializedFile file, String outputFileName) throws IOException {
        if (file != null && outputFileName != null) {
            file.writeFile(outputFileName);
            return new File(outputFileName).isFile();
        }

        return false;
    }
    
    
    
    /**
     * Join a list with a delimiter.
     */
    public static StringBuffer join(String delimiter, List<String> list) {

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < list.size() - 1; i++) {
            buffer.append(list.get(i));
            buffer.append(delimiter);
        }
        if (list.size() > 0) {
            buffer.append(list.get(list.size()-1));
        }
        return buffer;
    }
    
    /**
     * Join an array of string with a delimiter.
     */
    public static StringBuffer join(String delimiter, String [] strings) {
        return join(delimiter, Arrays.asList(strings));
    }

    /**
     * return full path for input relative path/filename.
     * 
     * @param relativePath
     */
    public static String getFullPath(String relativePath) {
        if (relativePath.startsWith(".\\") || relativePath.startsWith("./")) {
            relativePath = relativePath.substring(2);
        }
        String fullpath = Utilities.getCurrentDirectory() + "/" + relativePath;

        return new File(fullpath).getAbsolutePath();
    }
    
    /**
     * Convert String to second. 
     * 
     * @param s  string in form hh:mm:ss, ss or mm:ss
     * @return -1 if invalid time string, else returns number of seconds
     */
    public static long HHMMSStoString(String s) {

        if (s == null || s.trim().length() == 0) {
            return -1;
        }

        String[] fields = s.split(":");
        long hh = 0;
        long mm = 0;
        long ss = 0;
        
        switch (fields.length ) {
            case 3:
                hh = stringToLong(fields[0]);
                mm = stringToLong(fields[1]);
                ss = stringToLong(fields[2]);
                break;
            case 2:
                mm = stringToLong(fields[0]);
                ss = stringToLong(fields[1]);
                break;
            case 1:
                ss = stringToLong(fields[0]);
                break;

            default:
                break;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss);

        long totsecs = 0;
        if (hh != -1) {
            totsecs = hh;
        }
        if (mm != -1) {
            totsecs = (totsecs * 60) + mm;
        }
        if (ss != -1) {
            totsecs = (totsecs * 60) + ss;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss+" secs="+totsecs);

        if (hh == -1 || mm == -1 || ss == -1) {
            return -1;
        }

        return totsecs;
    }

    /**
     * Parse and return positive long.
     * 
     * @param s1
     * @return -1 if non-long string, else long value
     */
    public static long stringToLong(String s1) {
        if (s1 == null) {
            return -1;
        }
        try {
            return Long.parseLong(s1);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * return date/time string.
     * 
     * @return curernt date using format {@value Utilities#DATE_TIME_FORMAT_STRING}.
     */
    public static String getDateTime() {
        return format.format(new Date());
    }
    
    /**
     * Convert String to second. Expects input in form: ss or mm:ss or hh:mm:ss
     * 
     * @param s
     *            string to be converted to seconds
     * @return -1 if invalid time string, 0 or >0 if valid
     */
    public long stringToLongSecs(String s) {

        if (s == null || s.trim().length() == 0) {
            return -1;
        }

        String[] fields = s.split(":");
        long hh = 0;
        long mm = 0;
        long ss = 0;
        
        switch (fields.length ) {
            case 3:
                hh = stringToLong(fields[0]);
                mm = stringToLong(fields[1]);
                ss = stringToLong(fields[2]);
                break;
            case 2:
                mm = stringToLong(fields[0]);
                ss = stringToLong(fields[1]);
                break;
            case 1:
                ss = stringToLong(fields[0]);
                break;

            default:
                break;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss);

        long totsecs = 0;
        if (hh != -1) {
            totsecs = hh;
        }
        if (mm != -1) {
            totsecs = (totsecs * 60) + mm;
        }
        if (ss != -1) {
            totsecs = (totsecs * 60) + ss;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss+" secs="+totsecs);

        if (hh == -1 || mm == -1 || ss == -1) {
            return -1;
        }

        return totsecs;
    }

    /**
     * Locate judges data file on disk.
     * 
     * @param problem
     * @param serializedFile
     * @param judgeDataFile 
     * @return
     */
    public static String locateJudgesDataFile(Problem problem, SerializedFile serializedFile, DataFileType judgeDataFile) {

        if (serializedFile.isExternalFile()){

            String testFileName;

            // Search under CCS secret
            
            testFileName = problem.getCCSfileDirectory() + File.separator  + problem.getShortName() + SECRET_DATA_DIR + File.separator + serializedFile.getName();
            if (fileExists(testFileName)){
                return testFileName;
            }

            testFileName = problem.getExternalDataFileLocation() + File.separator  + serializedFile.getName();
            if (fileExists(testFileName)){
                return testFileName;
            }

            testFileName = serializedFile.getAbsolutePath();

            if (fileExists(testFileName)){
                return testFileName;
            }
        }

        return null;
    }

    public static boolean fileExists(String filename) {
        return new File(filename).isFile();
    }

}
