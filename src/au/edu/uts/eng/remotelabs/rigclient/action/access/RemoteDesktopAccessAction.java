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
 * @date 21st December 2009
 *
 * Changelog:
 * - 21/12/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.action.access;

import java.util.ArrayList;
import java.util.List;

import au.edu.uts.eng.remotelabs.rigclient.action.access.ExecAccessAction;
import au.edu.uts.eng.remotelabs.rigclient.action.test.PingTestAction;
import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;

/**
 * Windows Terminal Services access action. Performs the access action by
 * adding and removing users from the 'Remote Desktop Users' group which
 * controls who may remote login to a Windows console using RDP. 
 * <p>
 * This action only works for Windows, on other platforms a 
 * {@link IllegalStateException} will be thrown on construction.
 * <p>
 * An optional configuration option is <strong>'Windows_Domain'</strong>
 * which specifies the Windows / Samba domain the user is part of 
 * (i.e their name is '\\&lt;Windows_Domain&gt;\&lt;name&gt;'.
 */
public class RemoteDesktopAccessAction extends ExecAccessAction
{
    /** Default user group for remote desktop access. */
    public static final String DEFAULT_GROUPNAME = "Remote Desktop Users";

    /** Default command for changing user groups for windows */
    public static final String DEFAULT_COMMAND = "net";

    /** Default command for changing user groups for windows */
    public static final String DEFAULT_LOCALGROUP = "localgroup";

    /** Domain name. */
    private final String domainName;
    
    /** group name for user group that has remote desktop access. */
    protected String groupName;

    /** user name to be assigned access. */
    protected String userName;
    
   
    /**
     * Constructor.
     */
    public RemoteDesktopAccessAction()
    {
        this.logger = LoggerFactory.getLoggerInstance();
        
        this.domainName = ConfigFactory.getInstance().getProperty("Windows_Domain");
        if (this.domainName == null)
        {
            this.logger.info("Windows domain name not found, so not using a domain name.");
        }
        
        this.groupName = ConfigFactory.getInstance().getProperty("Remote_Desktop_Groupname",RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
        this.logger.debug("Remote Desktop User group is " + this.groupName);
        
    }
    
    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction#assign(java.lang.String)
     */
    @Override
    public boolean assign(String name)
    {
        
        this.userName = name;
        this.setupAccessAction();
        this.commandArguments.add("/ADD");
        this.logger.debug("Remote Desktop Access assign - arguments are"  + this.commandArguments.toString());
        
        if(!this.executeAccessAction())
        {
            this.logger.error("Remote Desktop Access action failed, command unsuccessful");
            return false;
        }
            
        if(!this.verifyAccessAction())
        {
            this.logger.error("Remote Desktop Access revoke action failed, exit code is" + this.getExitCode());
            return false;
        }

        return true;
        
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction#revoke(java.lang.String)
     */
    @Override
    public boolean revoke(String name)
    {
        this.userName = name;
        this.setupAccessAction();
        this.commandArguments.add("/DELETE");
        this.logger.debug("Remote Desktop Access revoke - arguments are"  + this.commandArguments.toString());
        
        if(!this.executeAccessAction())
        {
            this.logger.error("Remote Desktop Access action failed, command unsuccessful");
            return false;
        }
        
        if(!this.verifyAccessAction())
        {
            this.logger.error("Remote Desktop Access revoke action failed, exit code is" + this.getExitCode());
            return false;
        }
            
        return true;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.rig.IAction#getFailureReason()
     */
    @Override
    public String getFailureReason()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.rig.IAction#getActionType()
     */
    @Override
    public String getActionType()
    {
        return "Windows Remote Desktop Access.";
    }
    
    /**
     * Sets up access action common parts.  
     * 
     * This supplies the:
     * <ul>
     *     <li><strong>Command</strong> - The common command for assigna nd revoke "net localgroup"
     *     <li><strong>Command arguments</strong> - The common group name, domain if specified and
     *     user name
     *     <li><strong>Working directory</strong> - The configurable working directory for access
     * 
     * @see au.edu.uts.eng.remotelabs.rigclient.action.access.ExecAccessAction#setupAccessAction()
     */
    @Override
    public void setupAccessAction()
    {
        this.command = RemoteDesktopAccessAction.DEFAULT_COMMAND;
        this.commandArguments.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
        this.commandArguments.add(this.groupName);
        
        if (this.domainName != null)
        {
            this.commandArguments.add(this.domainName + "\\" + this.userName);
        }
        else
        {
             this.commandArguments.add(this.userName);
        }
        
        //No need to set up environment variables or working dir - TM

    }
    
    @Override
    public boolean  verifyAccessAction()
    {
        if(this.getExitCode() == 1)
        {
            return false;
        }
        
        return true;
            
    }
    
}
