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
 * @date 7th December 2009
 *
 * Changelog:
 * - 07/12/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.protocol;

import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AbortBatchControl;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AbortBatchControlResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.Allocate;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AllocateResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.ErrorType;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetAttribute;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetAttributeResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetBatchControlStatus;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetBatchControlStatusResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetStatus;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetStatusResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.Notify;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.NotifyResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.OperationResponseType;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformBatchControl;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformBatchControlResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformPrimitiveControl;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformPrimitiveControlResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.Release;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.ReleaseResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetMaintenance;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetMaintenanceResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetTestInterval;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetTestIntervalResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveAllocate;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveAllocateResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveRelease;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveReleaseResponse;
import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveUserType;
import au.edu.uts.eng.remotelabs.rigclient.rig.IRig;
import au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session;
import au.edu.uts.eng.remotelabs.rigclient.type.RigFactory;
import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;

/**
 * Rig client SOAP interface implementation.
 */
public class RigClientService implements RigClientServiceSkeletonInterface
{
    /** Rig type class. */
    private final IRig rig;
    
    /** Logger. */
    private final ILogger logger;
    
    /**
     * Constructor.
     */
    public RigClientService()
    {
        this.logger = LoggerFactory.getLoggerInstance();
        this.rig = RigFactory.getRigInstance();
    }
    
    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#allocate(Allocate)
     */
    @Override
    public AllocateResponse allocate(final Allocate allocRequest)
    {   
        /* Request parameters. */
        final String user = allocRequest.getAllocate().getUser();
        this.logger.debug("Received allocate request with parameter user=" + user + ".");
        
        /* Response parameters. */
        final OperationResponseType operation = new OperationResponseType();
        final ErrorType error = new ErrorType();
        operation.setError(error);
        error.setOperation("Allocate rig to user " + user + ".");
        error.setReason("");
        final AllocateResponse response = new AllocateResponse();
        response.setAllocateResponse(operation);

        if (!this.isSourceAuthenticated())
        {
            this.logger.warn("Failed allocating user " + user + " because of invalid permission.");
            operation.setSuccess(false);
            error.setCode(3);
            error.setReason("Not authorised to allocate a user.");

        }
        /* Try to allocate rig. */
        if (this.rig.isSessionActive()) // Cannot allocate a rig who is already in-use
        {
            this.logger.warn("Failed allocating user " + user + " because there is an existing session.");
            operation.setSuccess(false);
            error.setCode(4);
            error.setReason("A session is already active.");
        }
        else if (!this.rig.isMonitorStatusGood()) // Cannot allocate a rig who is in bad state
        {
            this.logger.warn("Failed allocating user " + user + " because the rig is not in a operable state.");
            operation.setSuccess(false);
            error.setCode(7);
            error.setReason("Rig not operable.");
        }
        else if (this.rig.assign(user)) // Actual allocation 
        {
            this.logger.info("Successfully allocated user " + user + " to rig.");
            operation.setSuccess(true);
        }
        else // Something fraked up
        {   
            this.logger.warn("Failed allocating user " + user + " because of some unknown reason " +
            "(this may be a bug).");
            operation.setSuccess(false);
            error.setCode(16);
            error.setReason("Unknown.");
        }

        
        
        return response;
    }
    
    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#release(Release)
     */
    @Override
    public ReleaseResponse release(final Release relRequest)
    {
        /* Request parameters. */
        final String user = relRequest.getRelease().getUser();
        this.logger.debug("Received release request with parameter: user=" + user + ".");
        
        /* Response parameters. */
        final OperationResponseType operation = new OperationResponseType();
        final ErrorType error = new ErrorType();
        operation.setError(error);
        error.setCode(0);
        error.setReason("");
        error.setOperation("Release " + user + ".");
        final ReleaseResponse response = new ReleaseResponse();
        response.setReleaseResponse(operation);
        
        if (!this.isSourceAuthenticated())
        {
            this.logger.warn("Failed releasing user " + user + " because of invalid permission.");
            operation.setSuccess(false);
            error.setCode(3);
            error.setReason("Not authorised to release a user.");

        }
        else if (!this.rig.isSessionActive())
        {
            this.logger.warn("Failed to release " + user + " as no session is currently running.");
            operation.setSuccess(false);
            error.setCode(6);
            error.setReason("Session not running.");
        }
        else if (!this.rig.hasPermission(user, Session.MASTER)) // If the user is indeed a master session user
        {
            this.logger.warn("Failed to release " + user + " as they are not the session master.");
            operation.setSuccess(false);
            error.setCode(5);
            error.setReason("User is not a master user.");
        }
        else if (this.rig.revoke()) // Actual revocation
        {
            this.logger.info("Released user " + user + ".");
            operation.setSuccess(true);
        }
        else  // Something fraked up
        {
            this.logger.warn("Failed to release " + user + " for some unknown reason (this may be a bug).");
            operation.setSuccess(false);
            error.setCode(16);
            error.setReason("Unknown.");
        }
        
        return response;
    }
    
    
    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#slaveAllocate(SlaveAllocate)
     */
    @Override
    public SlaveAllocateResponse slaveAllocate(final SlaveAllocate slaveRequest)
    {
        /* Request parameters. */
        final SlaveUserType slave = slaveRequest.getSlaveAllocate();
        final String user = slaveRequest.getSlaveAllocate().getUser();
        final String typeStr = slaveRequest.getSlaveAllocate().getType().getValue();
        Session type = Session.NOT_IN;
        if (typeStr.equalsIgnoreCase("Active"))
        {
            type = Session.SLAVE_ACTIVE;
        }
        else if (typeStr.equalsIgnoreCase("Passive"))
        {
            type = Session.SLAVE_PASSIVE; 
        }
        this.logger.debug("Received slave allocate request with parameters: user=" + user + ", slave type=" + 
                typeStr + ".");
        
        /* Response parameters. */
        final OperationResponseType operation = new OperationResponseType();
        final ErrorType error = new ErrorType();
        error.setCode(0);
        error.setOperation("Slave allocate of type " + typeStr + " to " + user + ".");
        error.setReason("");
        operation.setError(error);
        final SlaveAllocateResponse response = new SlaveAllocateResponse();
        response.setSlaveAllocateResponse(operation);
        
        
        // TODO Master user slave user assignment
        if (!this.isSourceAuthenticated())
        {
            this.logger.warn("Failed allocating slave user " + user + " because of invalid permission.");
            operation.setSuccess(false);
            error.setCode(3);
            error.setReason("Not authorised to allocate a slave user.");
        }
        else if (!(type == Session.SLAVE_ACTIVE || type == Session.SLAVE_PASSIVE)) // Only slave types
        {
            this.logger.warn("Failed allocating slave user " + user + " because of an invalid slave type " + 
                    typeStr + ".");
            operation.setSuccess(false);
            error.setCode(2);
            error.setReason("Invalid slave type parameter.");
        }
        else if (!this.rig.isSessionActive()) // Cannot slave assign when no session is running
        {
            this.logger.warn("Failed allocating slave user " + user + " because a session is not running.");
            operation.setSuccess(false);
            error.setCode(6);
            error.setReason("No session is currently running.");
        }
        else if (this.rig.hasPermission(slave.getUser(), Session.MASTER))
        {
            this.logger.warn("Trying to slave allocate a master user.");
            operation.setSuccess(false);
            error.setCode(2);
            error.setReason("User " + user + " is already a master user.");
        }
        else if (this.rig.addSlave(user, type == Session.SLAVE_ACTIVE ? false : true))
        {
            this.logger.info("Successfully added user " + user + " as slave user of type " + typeStr + ".");
            operation.setSuccess(true);
        }
        else
        {
            this.logger.warn("Failed allocating slave user " + user + ", the user may already have the requested " +
            		"slave permission.");
            operation.setSuccess(false);
            error.setCode(16);
            error.setReason("Unknown, user may already be a slave user.");
        }

        return response;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#slaveRelease(au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveRelease)
     */
    @Override
    public SlaveReleaseResponse slaveRelease(final SlaveRelease slaveRequest)
    {
        /* Request parameters. */
        final String slave = slaveRequest.getSlaveRelease().getUser();
        this.logger.debug("Received slave release request with parameters: user=" + slave + ".");
        
        /* Response parameters. */
        final SlaveReleaseResponse response = new SlaveReleaseResponse();
        final OperationResponseType operation = new OperationResponseType();
        response.setSlaveReleaseResponse(operation);
        final ErrorType error = new ErrorType();
        operation.setError(error);
        error.setCode(0);
        error.setOperation("Release slave " + slave + ".");
        error.setReason("");
        
        // TODO Master user slave release
        if (!this.isSourceAuthenticated())
        {
            this.logger.warn("Failed releasing slave user " + slave + " because of invalid permission.");
            operation.setSuccess(false);
            error.setCode(3);
            error.setReason("Not authorised to release slave user.");
        }
        else if (!(this.rig.isInSession(slave) == Session.SLAVE_ACTIVE || 
                   this.rig.isInSession(slave) == Session.SLAVE_PASSIVE))
        { 
            this.logger.warn("Failed releasing slave user " + slave + " because they are not a slave user.");
            operation.setSuccess(false);
            error.setCode(18);
            error.setReason("User " + slave + " not a slave user.");
        }
        else if (this.rig.revokeSlave(slave))
        {
            this.logger.info("Released slave user " + slave + ".");
            operation.setSuccess(true);
        }
        else // Something mother fraked up
        {
            this.logger.warn("Failed releasing slave user " + slave + " because of some unknown reason " +
            		"(possibly a bug).");
            error.setCode(16);
            error.setReason("Unknown reason.");
        }
        
        return response;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#abortBatchControl(au.edu.uts.eng.remotelabs.rigclient.protocol.types.AbortBatchControl)
     */
    @Override
    public AbortBatchControlResponse abortBatchControl(AbortBatchControl abortRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }

    

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#getAttribute(au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetAttribute)
     */
    @Override
    public GetAttributeResponse getAttribute(GetAttribute attrRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#getBatchControlStatus(au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetBatchControlStatus)
     */
    @Override
    public GetBatchControlStatusResponse getBatchControlStatus(GetBatchControlStatus statusRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#getStatus(au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetStatus)
     */
    @Override
    public GetStatusResponse getStatus(GetStatus statusRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#notify(au.edu.uts.eng.remotelabs.rigclient.protocol.types.Notify)
     */
    @Override
    public NotifyResponse notify(Notify notify)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#performBatchControl(au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformBatchControl)
     */
    @Override
    public PerformBatchControlResponse performBatchControl(PerformBatchControl batchRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#performPrimitiveControl(au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformPrimitiveControl)
     */
    @Override
    public PerformPrimitiveControlResponse performPrimitiveControl(PerformPrimitiveControl orimRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }

    

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#setMaintenance(au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetMaintenance)
     */
    @Override
    public SetMaintenanceResponse setMaintenance(SetMaintenance maintenRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.protocol.RigClientServiceSkeletonInterface#setTestInterval(au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetTestInterval)
     */
    @Override
    public SetTestIntervalResponse setTestInterval(SetTestInterval interRequest)
    {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * If the source is the requested scheduling server.
     * 
     * @param source
     * @return
     */
    private boolean isSourceAuthenticated()
    {
        return true;
    }

}
