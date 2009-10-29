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
 * @author <First name> <Last name> (mdiponio)
 * @date <day> <month> 2009
 *
 * Changelog:
 * - 29/10/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.rig.control.tests;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

import au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner;
import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;

/**
 * Tests the abstract batch runner.  Batch runner is intended to be run in 
 * its own thread, but for the purposes of testing it, runs in the same
 * thread as the test case.
 */
public class AbstractBatchRunnerTester extends TestCase
{
    /** Object of class under test. */
    private AbstractBatchRunner runner;
    
    /** Mock configuration class. */
    private IConfig mockConfig;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.mockConfig = createMock(IConfig.class);
        expect(this.mockConfig.getProperty("Logger_Type"))
                .andReturn("SystemErr");
        expect(this.mockConfig.getProperty("Log_Level"))
                .andReturn("DEBUG");
        replay(this.mockConfig);
        
        Field configField = ConfigFactory.class.getDeclaredField("instance");
        configField.setAccessible(true);
        configField.set(null, this.mockConfig);
        
        this.runner = new MockBatchRunner(null);
    }
    
    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#invoke()}.
     */
    @Test
    public void testInvoke()
    {
        reset(this.mockConfig);
        expect(this.mockConfig.getProperty("Clean_Up_Batch"));
        
        
        
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#cleanup()}.
     */
    @Test
    public void testCleanup()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#getBatchStandardOut()}.
     */
    @Test
    public void testGetBatchStandardOut()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#getBatchStandardError()}.
     */
    @Test
    public void testGetBatchStandardError()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#isStarted()}.
     */
    @Test
    public void testIsStarted()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#isRunning()}.
     */
    @Test
    public void testIsRunning()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#isFailed()}.
     */
    @Test
    public void testIsFailed()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#getResultsFiles()}.
     */
    @Test
    public void testGetResultsFiles()
    {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#getTimeStamp(char, char, char)}.
     */
    @Test
    public void testGetTimeStamp()
    {
        fail("Not yet implemented"); // TODO
    }

}
