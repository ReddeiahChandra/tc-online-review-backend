/*
 * Copyright (c) 2004, TopCoder, Inc. All rights reserved
 */
package com.topcoder.search.builder.stresstests;

import com.topcoder.search.builder.SearchBundle;
import com.topcoder.search.builder.SearchBundleManager;
import com.topcoder.search.builder.filter.EqualToFilter;
import com.topcoder.search.builder.filter.Filter;

import com.topcoder.util.datavalidator.IntegerValidator;
import com.topcoder.util.datavalidator.StringValidator;
import com.topcoder.util.sql.databaseabstraction.CustomResultSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * <p>
 * This class contains the stress unit tests for the EqualToFilter. change according to 1.3.
 * </p>
 *
 * @author PE, brain_cn
 * @version 1.3
 */
public class EqualToFilterStressTest extends TestCase {
    /** The number of threads used for testing. */
    private static final int THREAD_COUNT = StressTestHelper.THREAD_COUNT;

    /** The name of the searchBundle. */
    private static final String BUNDLE_NAME1 = "FirstSearchBundle";

    /** The name of the searchBundle. */
    private static final String BUNDLE_NAME2 = "SecondSearchBundle";

    /**
     * <p>
     * Creates a test suite of the tests contained in this class.
     * </p>
     *
     * @return a test suite of the tests contained in this class.
     */
    public static Test suite() {
        return new TestSuite(EqualToFilterStressTest.class);
    }

    /**
     * <p>
     * Sets up the test environment.
     * </p>
     *
     * @throws Exception pass any unexpected exception to JUnit.
     */
    protected void setUp() throws Exception {
        StressTestHelper.clearConfig();
        StressTestHelper.addConfig();
        StressTestHelper.addEntries();
    }

    /**
     * Clears the environment.
     *
     * @throws Exception to JUnit
     */
    protected void tearDown() throws Exception {
        StressTestHelper.clearConfig();
        StressTestHelper.delEntries();
    }

    /**
     * Run stress test.
     *
     * @throws Exception to JUnit
     */
    public void testEqualToFilterStressOfDatabaseConnectionStrategy()
        throws Exception {
        NullFilterThread[] threads = new NullFilterThread[THREAD_COUNT];
        SearchBundle sb = new SearchBundleManager(StressTestHelper.NAMESPACE).getSearchBundle(BUNDLE_NAME1);
        Map fields = new HashMap();
        fields.put("price", IntegerValidator.inRange(0, 20));
        sb.setSearchableFields(fields);

        Filter filter = new EqualToFilter("price", new Integer(1));

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new NullFilterThread();
            threads[i].setUp(sb, filter);
        }

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        long dure = System.currentTimeMillis() - startTime;
        System.out.println("EqualToFilterStressOfDatabaseConnectionStrategy() tested: " +
            Integer.toString(THREAD_COUNT) + " threads, " + Long.toString(dure) + " ms.");

        for (int i = 0; i < threads.length; i++) {
            threads[i].checkException();

            CustomResultSet result = (CustomResultSet) threads[i].getResult();
            int sum = 0;

            while (result.next()) {
                sum++;
            }

            assertEquals("validation of result should be ok", 1, sum);
        }
    }

    /**
     * Run stress test.
     *
     * @throws Exception to JUnit
     */
    public void testEqualToFilterStressOfLDAPConnectionStrategy()
        throws Exception {
        NullFilterThread[] threads = new NullFilterThread[THREAD_COUNT];
        SearchBundle sb = new SearchBundleManager(StressTestHelper.NAMESPACE).getSearchBundle(BUNDLE_NAME2);
        Map fields = new HashMap();
        fields.put("sn", StringValidator.hasLength(IntegerValidator.inRange(0, 20)));
        sb.setSearchableFields(fields);

        Filter filter = new EqualToFilter("sn", "4");

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new NullFilterThread();
            threads[i].setUp(sb, filter);
        }

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        long dure = System.currentTimeMillis() - startTime;
        System.out.println("testEqualToFilterStressOfLDAPConnectionStrategy() tested: " +
            Integer.toString(THREAD_COUNT) + " threads, " + Long.toString(dure) + " ms.");

        for (int i = 0; i < threads.length; i++) {
            threads[i].checkException();

            Iterator result = (Iterator) threads[i].getResult();
            int sum = 0;

            while ((result != null) && result.hasNext()) {
                result.next();
                sum++;
            }
        }
    }
}


class EqualToFilterThread extends Thread {
    /** The searchBundle instance for testing. */
    private SearchBundle sb = null;

    /** The Filter instance for testing. */
    private Filter filter = null;

    /** Result. */
    private Object result = null;

    /** Exception instance. */
    private Exception exception = null;

    /**
     * Setup the environment.
     *
     * @param sb to JUnit
     * @param filter to JUnit
     *
     * @throws Exception to JUnit
     */
    public void setUp(SearchBundle sb, Filter filter) throws Exception {
        this.sb = sb;
        this.filter = filter;
    }

    /**
     * Run the test.
     */
    public void run() {
        try {
            for (int i = 0; i < StressTestHelper.STRESS_LOADS; ++i) {
                result = sb.search(filter);
            }
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Check for exception during testing.
     *
     * @throws Exception to JUnit
     */
    public void checkException() throws Exception {
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Return the result.
     *
     * @return result
     */
    public Object getResult() {
        return result;
    }
}
