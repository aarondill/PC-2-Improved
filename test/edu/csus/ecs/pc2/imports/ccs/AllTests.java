package edu.csus.ecs.pc2.imports.ccs;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 *  Test Suite for core util.
 *  
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

public final class AllTests {

    private AllTests() {

    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for edu.csus.ecs.pc2.core.util");
        //$JUnit-BEGIN$
        suite.addTestSuite(ICPCCSVLoaderTest.class);
        suite.addTestSuite(ContestYAMLLoaderTest.class);
        //$JUnit-END$
        return suite;
    }

}