/**
 * SAHARA Rig Client
 * 
 * Software abstraction of physical rig to provide rig session control
 * and rig device control. Automatically tests rig hardware and reports
 * the rig status to ensure rig goodness.
 *
 * @license See LICENSE in the top level directory for complete license terms.
 *
 * Copyright (c) 2009, University of Technology, Sydney
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Technology, Sydney nor the names 
 *    of its contributors may be used to endorse or promote products derived from 
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Michael Diponio (mdiponio)
 * @date 6th November2009
 *
 * Changelog:
 * - 06/11/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.rig.internal.tests;

import java.lang.reflect.Method;
import java.util.Calendar;

import junit.framework.TestCase;

import org.junit.Before;

import au.edu.uts.eng.remotelabs.rigclient.rig.internal.BatchMacroSubstituter;

/**
 * Tests the macro substituter class.
 */
public class BatchMacroSubstituerTester extends TestCase
{
    /** Object of class under test. */
    private BatchMacroSubstituter substituter;
    
    /** Current time to give to substituter. */
    private final Calendar calendar = Calendar.getInstance();
    
    /** File. */
    private final String file;
    
    /** User name. */
    private final String user;
    
    /**
     * Constructor.
     */
    public BatchMacroSubstituerTester()
    {
        this.file = System.getProperty("user.dir");
        this.user = "mdiponio";
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    @Override
    public void setUp() throws Exception
    {
        this.substituter = new BatchMacroSubstituter.MacroBuilder(this.file, this.user).setCalendar(this.calendar).build();
    }
    
    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.internal.BatchMacroSubstituter#addLeadingZeros}
     */
    public void testAddLeadingZeros()
    {
        try
        {
            Method meth = BatchMacroSubstituter.class.getDeclaredMethod("addLeadingZeros", int.class);
            meth.setAccessible(true);
            String str = (String)meth.invoke(this.substituter, 10);
            assertEquals(2, str.length());
            assertEquals("10", str);
            
            str = (String)meth.invoke(this.substituter, 1);
            assertEquals(2, str.length());
            assertEquals("01", str);
        }
        catch (Exception e)
        {
            fail("Exception: " + e.getClass().getName() + ",  message " + e.getMessage());
        }
        
    }
    
    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.internal.BatchMacroSubstituter#substituteMacros(String)}
     */
    public void testSubstitute()
    {
        assertEquals(this.file, this.substituter.substituteMacros("__FILE__"));
        assertEquals(this.user, this.substituter.substituteMacros("__USER__"));
        assertEquals("/" + this.user + "/" + this.file + "/", this.substituter.substituteMacros("/__USER__/__FILE__/"));
        
        assertEquals(String.valueOf(this.calendar.get(Calendar.YEAR)), this.substituter.substituteMacros("__YEAR__"));
        assertEquals(String.valueOf(this.calendar.getTimeInMillis() / 1000), this.substituter.substituteMacros("__EPOCH__"));
        
        String isoTime = this.substituter.substituteMacros("__ISO8601__");
        String isoParts[] = isoTime.split("T");
        assertEquals(2, isoParts.length);
        String dateParts[] = isoParts[0].split("-");
        String timeParts[] = isoParts[1].split(":");
        assertEquals(3, dateParts.length);
        assertEquals(String.valueOf(this.calendar.get(Calendar.YEAR)), dateParts[0]);
        assertEquals(3, timeParts.length);
    }

}
