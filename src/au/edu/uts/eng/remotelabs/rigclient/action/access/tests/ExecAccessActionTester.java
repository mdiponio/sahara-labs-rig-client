/**
 * SAHARA Rig Client
 * 
 * Software abstraction of physical rig to provide rig session control
 * and rig device control. Automatically tests rig hardware and reports
 * the rig status to ensure rig goodness.
 *
 * @license See LICENSE in the top level directory for complete license terms.
 *
 * Copyright (c) 2010, University of Technology, Sydney
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
 * @author <First> <Last> (tmachet)
 * @date <Day> <Month> 2010
 *
 * Changelog:
 * - 19/01/2010 - tmachet - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.action.access.tests;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.edu.uts.eng.remotelabs.rigclient.action.access.ExecAccessAction;
import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;

/**
 * @author tmachet
 *
 */
public class ExecAccessActionTester extends TestCase
{
    /** Object of class under test. */
    private MockAccessAction action;
    
    /** Mock configuration class. */
    private IConfig mockConfig;


    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception
    {
        
        this.mockConfig = createMock(IConfig.class);
        expect(this.mockConfig.getProperty("Logger_Type"))
                .andReturn("SystemErr");
        expect(this.mockConfig.getProperty("Log_Level"))
                .andReturn("DEBUG");
        expect(this.mockConfig.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"))
                .andReturn("[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
        expect(this.mockConfig.getProperty("FATAL_Log_Format")).andReturn(null);
        expect(this.mockConfig.getProperty("PRIORITY_Log_Format")).andReturn(null);
        expect(this.mockConfig.getProperty("ERROR_Log_Format")).andReturn(null);
        expect(this.mockConfig.getProperty("WARN_Log_Format")).andReturn(null);
        expect(this.mockConfig.getProperty("INFO_Log_Format")).andReturn(null);
        expect(this.mockConfig.getProperty("DEBUG_Log_Format")).andReturn(null);
        replay(this.mockConfig);
        
        Field configField = ConfigFactory.class.getDeclaredField("instance");
        configField.setAccessible(true);
        configField.set(null, this.mockConfig);

        LoggerFactory.getLoggerInstance(); 
        this.action = new MockAccessAction();
        
       
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception
    {
        // runs after test case - cleanup if resources used.

    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.action.access.ExecAccessAction#executeAccessAction()}.
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @Test
    public void testExecuteActionNoCommand() throws Exception
    {
        reset(this.mockConfig);
        replay(this.mockConfig);

        Field f = ExecAccessAction.class.getDeclaredField("command");
        f.setAccessible(true);
        f.set(this.action, null);

        Method testMe = ExecAccessAction.class.getDeclaredMethod("executeAccessAction");
        testMe.setAccessible(true);
        boolean result = (Boolean)testMe.invoke(this.action);
        
        assertFalse(result);
        
    }

    @Test
    public void testExecuteActionRun() throws Exception
    {
        reset(this.mockConfig);
        replay(this.mockConfig);

        final String os = System.getProperty("os.name");

        if (os.startsWith("Windows"))
        {
            // Sets command to echo out a string
            Field f = ExecAccessAction.class.getDeclaredField("command");
            f.setAccessible(true);
            f.set(this.action, "net" );

            final List<String> args = new ArrayList<String>();
            args.add("user");
            f = ExecAccessAction.class.getDeclaredField("commandArguments");
            f.setAccessible(true);
            f.set(this.action, args);
            
            f = ExecAccessAction.class.getDeclaredField("workingDirectory");
            f.setAccessible(true);
            f.set(this.action, null);
    
            Method testMe = ExecAccessAction.class.getDeclaredMethod("executeAccessAction");
            testMe.setAccessible(true);
            boolean result = (Boolean)testMe.invoke(this.action);
            
            assertTrue(result);
        }    
    }

    @Test
    public void testExecuteActionGetOutput() throws Exception
    {
        reset(this.mockConfig);
        replay(this.mockConfig);

        // Sets command to echo out a string
        Field f = ExecAccessAction.class.getDeclaredField("command");
        f.setAccessible(true);
        f.set(this.action, "net" );

        final List<String> args = new ArrayList<String>();
        args.add("user");
        args.add("tmachet");
        f = ExecAccessAction.class.getDeclaredField("commandArguments");
        f.setAccessible(true);
        f.set(this.action, args);
        
        f = ExecAccessAction.class.getDeclaredField("workingDirectory");
        f.setAccessible(true);
        f.set(this.action, null);

        Method testMe = ExecAccessAction.class.getDeclaredMethod("executeAccessAction");
        testMe.setAccessible(true);
        boolean result = (Boolean)testMe.invoke(this.action);

        Method testOut = ExecAccessAction.class.getDeclaredMethod("getAccessOutputString");
        testOut.setAccessible(true);
        String outString = (String) testOut.invoke(this.action);
        
        assertTrue(result);
        assertNotNull(outString);
        
    }

}
