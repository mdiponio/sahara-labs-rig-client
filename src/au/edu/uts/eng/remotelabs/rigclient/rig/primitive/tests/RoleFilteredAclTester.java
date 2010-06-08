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
 * @author Michael Diponio (mdiponio)
 * @date 7th June 2010
 */
package au.edu.uts.eng.remotelabs.rigclient.rig.primitive.tests;

import java.lang.reflect.Field;
import java.util.List;

import junit.framework.TestCase;
import au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session;
import au.edu.uts.eng.remotelabs.rigclient.rig.primitive.IPrimitiveAcl;
import au.edu.uts.eng.remotelabs.rigclient.rig.primitive.RoleFilteredAcl;

/**
 * Tests the {@link RoleFilteredAcl} class.
 */
public class RoleFilteredAclTester extends TestCase
{
    /* Object of class under test. */
    private IPrimitiveAcl acl;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.acl = new RoleFilteredAcl();
    }

    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.primitive.RoleFilteredAcl#allowRole(au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session, java.lang.String)}.
     */
    public void testAllowRole()
    {
        assertTrue(this.acl.allowRole(Session.MASTER, "foo"));
        assertFalse(this.acl.allowRole(Session.SLAVE_ACTIVE, "foo"));
        assertFalse(this.acl.allowRole(Session.SLAVE_PASSIVE, "foo"));
        assertFalse(this.acl.allowRole(Session.NOT_IN, "foo"));
    }
    
    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.primitive.RoleFilteredAcl#allowRole(au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session, java.lang.String)}.
     */
    @SuppressWarnings("unchecked")
    public void testAllowRoleSlaveNotAllowed() throws Exception
    {
        Field f = RoleFilteredAcl.class.getDeclaredField("slaveActiveActions");
        f.setAccessible(true);
        List<String> sActive= (List<String>) f.get(this.acl);
        sActive.add("bar");
        
        f =  RoleFilteredAcl.class.getDeclaredField("slavePassiveActions");
        f.setAccessible(true);
        List<String> sPassive = (List<String>) f.get(this.acl);
        sPassive.add("baz");
        
        assertTrue(this.acl.allowRole(Session.MASTER, "foo"));
        assertFalse(this.acl.allowRole(Session.SLAVE_ACTIVE, "foo"));
        assertFalse(this.acl.allowRole(Session.SLAVE_PASSIVE, "foo"));
        assertFalse(this.acl.allowRole(Session.NOT_IN, "foo"));
    }
    
    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.primitive.RoleFilteredAcl#allowRole(au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session, java.lang.String)}.
     */
    @SuppressWarnings("unchecked")
    public void testAllowRoleSlaveActive() throws Exception
    {
        Field f = RoleFilteredAcl.class.getDeclaredField("slaveActiveActions");
        f.setAccessible(true);
        List<String> sActive= (List<String>) f.get(this.acl);
        sActive.add("foo");

        assertTrue(this.acl.allowRole(Session.MASTER, "foo"));
        assertTrue(this.acl.allowRole(Session.SLAVE_ACTIVE, "foo"));
        assertFalse(this.acl.allowRole(Session.SLAVE_PASSIVE, "foo"));
        assertFalse(this.acl.allowRole(Session.NOT_IN, "foo"));
    }
    
    /**
     * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.primitive.RoleFilteredAcl#allowRole(au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session, java.lang.String)}.
     */
    @SuppressWarnings("unchecked")
    public void testAllowRoleSlavePassive() throws Exception
    {
        Field f =  RoleFilteredAcl.class.getDeclaredField("slavePassiveActions");
        f.setAccessible(true);
        List<String> sPassive = (List<String>) f.get(this.acl);
        sPassive.add("foo");
        
        assertTrue(this.acl.allowRole(Session.MASTER, "foo"));
        assertTrue(this.acl.allowRole(Session.SLAVE_ACTIVE, "foo"));
        assertTrue(this.acl.allowRole(Session.SLAVE_PASSIVE, "foo"));
        assertFalse(this.acl.allowRole(Session.NOT_IN, "foo"));
    }
}
