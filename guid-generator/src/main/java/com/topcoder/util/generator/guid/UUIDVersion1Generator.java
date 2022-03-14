/**
 * Copyright (C) 2004, TopCoder, Inc. All rights reserved
 */
package com.topcoder.util.generator.guid;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * <p>
 * creates a UUID draft version 1 generator that generates UUID based on time.
 * This class will, by default, be created using the SecureRandom implementation. Application can choose to
 * supply their own random implementation instead.
 * </p>
 * <p>
 * If the application provides their own random implementation, it should use a cryptographic strength random 
 * number generator.
 * </p>
 * <p>
 * <strong>Thread Safety: </strong>this class is thread safe, being the getNextUUID synchronized
 * as per recommendations of the UUID specs.
 * </p>
 * 
 * @author TCSDEVELOPER
 * @author TCSDESIGNER
 * @version 1.0
 */
public class UUIDVersion1Generator extends AbstractGenerator {

    /**
     * Represents the last timestamp seen when generating a UUID.
     * This timestamp has at best a millisecond precision, but often it's 10 to 50 ms, and thus it's very 
     * feasible that many UUIDs are genearted during that period of time.  When this happens, the clockSeq
     * variable is used to simulate a higher precision clock of 100 ns intervals and a clock sequence to provide
     * even more calls available in the same time slice.
     * Because the clock sequence has 14 available bits (16,384 numbers) and there are place for 10,000 intervals 
     * of 100 ns in 1 millisecond, the limit is 160 million calls in the same time slice, a very high number
     * considering even the more pesimistic time slice of 50 ms.
     */
    private long lastTime = 0;

    /**
     * Represents the clock sequence and the 100 ns resolution of the clock.  One variable is used for both purpouses,
     * being clockSeq % 10,000 the number of 100 ns intervals in the current 1 ms interval, and clockSeq / 10,000
     * the clock sequence as defined in the draft.
     */
    private int clockSeq = 0;

    /**
     * This should be the MAC address if available; but this is not the case in Java.
     * node will have always exactly 6 bytes, where bytes 2 to 5 are the IP address if available or 
     * random values if not available.
     * Bytes 0 and 1 will be random, with the MSB of byte 0 being always 1 to indicate that the node was not obtained
     * from a MAC address, and because MAC addresses should have this bit set to 0, it avoids collisions. 
     */
    private byte[] node = null;

    /**
     * <p>
     * Creates this generator using the SecureRandom generator.
     * </p>
     * <p>
     * It initializes the lastTime and clockSeq variables, and tries to get the IP address that will represent
     * the node, filling the remaining bytes with random values. 
     * </p>
     *  
     */
    public UUIDVersion1Generator() {
        super();
        initialize();
    } 

    /**
     * <p>
     * Creates this generator using the specified Random generator.
     * </p>
     * <p>
     * It initializes the lastTime and clockSeq variables, and tries to get the IP address that will represent
     * the node, filling the remaining bytes with random values. 
     * </p>
     * 
     * @param random
     *            a non-null random implementation
     * @throws NullPointerException
     *             if the random is null
     */
    public UUIDVersion1Generator(Random random) {
        // super will throw NullPointerException if random is null
        super(random);
        initialize();
    } 

    /**
     * This method perform the initialization common to both constructors.
     * <ol>
     * <li>Save the current system time to the last time variable</li>
     * <li>Set the clock seq to a random number generated by the getRandom()</li>
     * <li>Define the node value, if possible using IP, if not using random values</li>
     * </ol>
     * 
     */
    private void initialize() {
        boolean generateRandomIP = false; 
        byte[] ipAddress = null;
        
        // Set the 6 bytes of node to random values that could be overwritten with the IP address if found
        node = new byte [6];
        getRandom().nextBytes(node);

        lastTime = System.currentTimeMillis();
        clockSeq = Math.abs (getRandom().nextInt());

        try {
            ipAddress = InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            // failed to get the ipAddress, so ipAddress is null and
            // the bytes will be filled with random values.
        }

        // If the ipAddress has 4 bytes, copy to the node
        if ((ipAddress != null) && (ipAddress.length == 4)) {
            System.arraycopy(ipAddress, 0, node, 2, 4);
        }

        // Set the first bit of the first byte to indicate that the node was not generated by a MAC address,
        // as stated in the UUIDs and GUIDs draft, section 4
        node [0] |= 0x80; 

    }
    /**
     * <p>
     * this implements the version 1 generation of random UUID as specified in the UUID draft specification.
     * This UUID generator uses mainly the current time and a "computer identification" (IP) to provide uniqueness.
     * Because it is very feasible that this method is called many times in the same clock "slice", the clock sequence
     * is used to differentiate the identifiers generated. 
     * </p>
     * <p>
     * The RFC specifies that the time should be the number of 100 nanosecond intervals from a fixed moment of time;
     * but the best that Java provides is, at best, 1 millisecond interval; and depending on the Java
     * version and operating system it can also be 10,20, 50 ms interval.
     * Because only 14 bits of the clock sequence are used, this gives 2^14=16384 calls in the same "time slice", 
     * something that might not be impossible to reach in a very fast machine wit big time slices.
     * </p>
     * <p>
     * The workaround is to "simulate" a higher resolution.
     * For that purpouse, the number of milliseconds is multiplied by 10,000 in order to have 100 nanosecond
     * intervals, and those "steps" of 100 nanoseconds are filled with the 10,000 modulus of the clock sequence, and
     * the remaining digits (clock sequence / 10,000) are stored in the clock sequence field of the UUID.
     * That way, the resolution is multiplied by 10,000 enabling 160 millions calls in a same time slice, which is not
     * possible in a near future.
     * </p>
     * <p>
     * The byte layout of the 128 bit UUID is as follows: 
     * </p>
     * <ol>
     * <li> 4 bytes - TIME_LOW</li>
     * <li> 2 bytes - TIME_MID</li>
     * <li> 2 bytes - TIME_HI_AND_VERSION</li>
     * <li> 1 byte  - CLOCK_SEQUENCE_HI_AND_RESERVED</li>
     * <li> 1 byte  - CLOCK_SEQUENCE_LOW</li>
     * <li> 6 bytes - NODE</li>
     * </ol> 
     * <p>
     *  TIME_HI_AND_VERSION stores the version number (1 in this case) in its 4 most significant bits (MSB), and the
     * highest value part of the time in the others 12 bits.
     * </p>
     * <p>
     * CLOCK_SEQUENCE_HI_AND_RESERVED uses a variable length field in its MSB to determine the UUID layout.
     * For this UUID implementation, the 2 MSB must be set to 1 (the most significant of the two) and 0.
     * The other 14 bits are used for the high part of the clock sequence.
     * </p>   
     * <p>
     * NODE should represent the MAC address of the computer, but since this is unavailable, the IP of the computer
     * will be used in its place when possible, setting the remaining bits to random values.
     * </p>
     * <p>
     * When this method is called, the current time is checked against the last time this function was called.
     * If the time is earlier, is because the time was changed in the computer, so a new random clock sequence is
     * generated.
     * </p>
     * <p>
     * If the current time is the same as the last time, the clock sequence is incremented in order to differentiate
     * the calls in the same "time slice". 
     * </p>
     * 
     * @return a non-null UUID128Implemenation
     */
    public synchronized UUID getNextUUID() {
        byte[] bytes  = new byte[16];
        long currentTime = System.currentTimeMillis();
        long simulatedTime;  // the "simulated" time with 100 ns resolution
        

        if (currentTime < lastTime) {
            clockSeq = Math.abs (getRandom().nextInt());
        } else 
        if (currentTime == lastTime) {
            clockSeq++;
        }
        lastTime = currentTime;

        // calculate the simulated time with a 100 ns resolution, using the last 4 decimal digits of clock sequence
        // as the number of 100 ns intervals passed in this 1 ms time interval
        simulatedTime = currentTime * 10000 + clockSeq % 10000;
        
        // Get the 32 LSB of the time
        int timeLow = (int) (simulatedTime & 0xFFFFFFFF);
        
        // get the 16 LSB after the first 32 LSB of the time
        short timeMid = (short) ((simulatedTime >>> 32) & 0xFFFF);
        
        // Get the 16 MSB of the time.
        // Apply "& 0x0FFF" to erase the 4 MSB
        // Set the version number in the 4 MSB erased to 1, using "| 0x1000"
        short timeHighAndVersion = (short) (((simulatedTime >>> 48) & 0x0FFF) | 0x1000);
        
        // calculate the clock sequence (as stated in the draft), using the clockSeq value except for
        // the last 5 decimal digits, and taking into account the 14 LSB of this value.
        // The 2 MSB of ckSeq are reserved, and should be set to 1 (the MSB of the two) and 0 (the other bit),
        // which is accomplished using "| 0x8000"
        short ckSeq =  (short) (((clockSeq / 10000) & 0x3FFF) | 0x8000); 
        
       
        setBytes(bytes, 0, 4, timeLow);
        setBytes(bytes, 4, 2, timeMid);
        setBytes(bytes, 6, 2, timeHighAndVersion);
        setBytes(bytes, 8, 2, ckSeq);

        // set the last 6 bytes to node 
        System.arraycopy(node, 0, bytes, 10, 6);
        
        return new UUID128Implementation(bytes);
    }
    
    /**
     * Helper method to set some bytes from an integer value.
     * It will set the len bytes of the array, starting from offset, to represent the number n
     * (or the len less significant bytes).
     * Example: if n is 0x1234, and len is 2, then calling to this method will set:
     *  bytes [offset] = 0x12
     *  bytes [offset+1] = 0x34
     * 
     * @param bytes the array  where the values will be put 
     * @param offset where to start putting values in the array
     * @param len how many bytes will be set
     * @param n the number that be set in those bytes
     */
    private void setBytes(byte[] bytes, int offset, int len, int n) {
        for (int i = len - 1; i >= 0; i--) {
            bytes [offset + i] = (byte) (n & 0xFF);
            n >>>= 8;
        }
            
    }
} 

