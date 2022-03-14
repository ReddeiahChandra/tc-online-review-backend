/*
 * Copyright (C) 2007 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder.util.scheduler.scheduling;

/**
 * <p>
 * This is a marker extension of <code>DateUnit</code> that represents a base interval unit of a day.
 * </p>
 *
 * <p>
 * Jobs configured with it will be run in intervals of days, depending on the associated interval setting.
 * </p>
 *
 * <p>
 * Thread Safety : This class is immutable and so thread safe.
 * </p>
 *
 * @author argolite, TCSDEVELOPER
 * @version 3.0
 * @since 3.0
 */
public class Day implements DateUnit {
    /**
     * <p>
     * Default constructor.
     * </p>
     *
     * <p>
     * This constructor does nothing in current implementation.
     * </p>
     */
    public Day() {
        // empty
    }
}
