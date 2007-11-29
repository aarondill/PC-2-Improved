package edu.csus.ecs.pc2.core.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IController;
import edu.csus.ecs.pc2.core.list.SiteComparatorBySiteNumber;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IContest;
import edu.csus.ecs.pc2.core.model.Site;

/**
 * Create sitelist.txt file.
 * 
 * The format for the site list file is:
 * 
 * <pre>
 *   &lt;site #&gt;|&lt;site title&gt;|&lt;site password&gt;|&lt;site IP&gt;|&lt;site Port&gt;|&lt;site element Id&gt;
 * </pre>
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class SiteListReport implements IReport {

    /**
     * 
     */
    private static final long serialVersionUID = 7194244117875846407L;

    private IContest contest;

    private IController controller;

    private Log log;

    private Filter filter;

    public void writeReport(PrintWriter printWriter) {

        // Judgements
        printWriter.println();

        Site[] sites = contest.getSites();

        Arrays.sort(sites, new SiteComparatorBySiteNumber());

        printWriter.println("# -- " + sites.length + " sites --");

        for (Site site : sites) {
            printWriter.print(site.getSiteNumber());
            printWriter.print("|");
            printWriter.print(site.getDisplayName());
            printWriter.print("|");
            printWriter.print(site.getPassword());
            printWriter.print("|");
            printWriter.print(site.getConnectionInfo().getProperty(Site.IP_KEY));
            printWriter.print("|");
            printWriter.print(site.getConnectionInfo().getProperty(Site.PORT_KEY));
            printWriter.print("|");
            printWriter.print(site.getElementId());
            printWriter.print("|");
        }
    }

    private void printHeader(PrintWriter printWriter) {
        printWriter.println("# " + new VersionInfo().getSystemName());
        printWriter.println("# Date: " + new Date());
        printWriter.println("# " + new VersionInfo().getSystemVersionInfo());
    }

    private void printFooter(PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("# end report");
    }

    public void createReportFile(String filename, Filter inFilter) throws IOException {

        PrintWriter printWriter = new PrintWriter(new FileOutputStream(filename, false), true);

        try {
            printHeader(printWriter);

            try {
                writeReport(printWriter);
            } catch (Exception e) {
                printWriter.println("Exception in report: " + e.getMessage());
                e.printStackTrace(printWriter);
            }

            printFooter(printWriter);

            printWriter.close();
            printWriter = null;

        } catch (Exception e) {
            log.log(Log.INFO, "Exception writing report", e);
            printWriter.println("Exception generating report " + e.getMessage());
        }
    }

    public String[] createReport(Filter inFilter) {
        throw new SecurityException("Not implemented");
    }

    public String createReportXML(Filter inFilter) {
        throw new SecurityException("Not implemented");
    }

    public String getReportTitle() {
        return "Site List";
    }

    public void setContestAndController(IContest inContest, IController inController) {
        this.contest = inContest;
        this.controller = inController;
        log = controller.getLog();
    }

    public String getPluginTitle() {
        return "Site List Report";
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

}
