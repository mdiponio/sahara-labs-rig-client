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
 * @date 7th October 2009
 */
package au.edu.uts.eng.remotelabs.rigclient.rig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import au.edu.uts.eng.remotelabs.rigclient.rig.internal.AttributeMacroSubstituter;
import au.edu.uts.eng.remotelabs.rigclient.rig.internal.DataTransferWatcher;
import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;

/**
 * Abstract rig type class that contains lists of the following actions:
 * 
 * <ul>
 *    <li>IAcessAction</li>
 *    <li>ISlaveAccessAction</li>
 *    <li>IResetAction</li>
 *    <li>INotifyAction</li>
 *    <li>ITestActionM</li>
 *    <li>IActivityDetectorAction</li>
 * </ul>
 *   
 * When a method is called, the corresponding action list is iterated through
 * and the action initiating method is called. For example, if the
 * <code>startTests</code> method is called, the <code>ITestAction</code> list 
 * is iterated through and the <code>startTest</code> method is called.
 */
public abstract class AbstractRig implements IRig
{
    /** 
     * Action types that may be registered with <code>registerAction</code>.
     */
    public enum ActionType 
    {
        /** Master access and revocation. */
        ACCESS,
        /** Slave access and revocation. */
        SLAVE_ACCESS,
        /** User notification. */
        NOTIFY,
        /** Rig reset. */
        RESET,
        /** Monitor test. */
        TEST,
        /** Activity detection. */
        DETECT,
        /** Session file detection. */
        FILES
    }
    
    /** Session users in the form key => user name, value => user type */
    protected Map<String, Session> sessionUsers;
    
    /** Access action list. */
    private final List<IAccessAction> accessActions;
    
    /** Slave access action list. */
    private final List<ISlaveAccessAction> slaveActions;
    
    /** Notify action list. */
    private final List<INotifyAction> notifyActions;
    
    /** Reset action list. */
    private final List<IResetAction> resetActions;
    
    /** Test action list. */
    private final List<ITestAction> testActions;
    
    /** Activity detector action list. */
    private final List<IActivityDetectorAction> detectionActions;
    
    /** File detector action list. */
    private final List<IFilesDetectorAction> filesActions;
    
    /** Test actions thread group. */
    private final ThreadGroup testThreads;
    
    /** Maintenance flag. */
    private boolean inMaintenance;
    
    /** Maintenance reason. */
    private String maintenanceReason;
    
    /** Threshold for number of action failures before maintenance mode
     *  is set. */
    private int failureThreshold;
    
    /** Count for action failures which is incremented after a each action
     *  failure. <strong>NOTE:</strong> the failure count is only ever 
     *  cleared when maintenance is cleared, not after a subsequent
     *  successful action. */
    private final Map<IAction, Integer> actionFailures;
    
    /** Macro substituter for the <code>getRigAttribute</code> method. */ 
    protected final AttributeMacroSubstituter macroSubstituter;
    
    /** Thread used to transfer files to the Scheduling Server. */
    private final DataTransferWatcher dataTransfer;
    
    /** Rig client configuration. */
    protected IConfig configuration;
    
    /** Logger. */
    protected ILogger logger;    
    
    /**
     * Constructor, initialises the action lists.
     */
    public AbstractRig()
    {
        this.logger = LoggerFactory.getLoggerInstance();
        this.configuration = ConfigFactory.getInstance();
        
        this.sessionUsers = Collections.synchronizedMap(new HashMap<String, Session>());
        
        this.accessActions = new ArrayList<IAccessAction>();
        this.slaveActions = new ArrayList<ISlaveAccessAction>();
        this.notifyActions = new ArrayList<INotifyAction>();
        this.resetActions = new ArrayList<IResetAction>();
        this.testActions = new ArrayList<ITestAction>();
        this.detectionActions = new ArrayList<IActivityDetectorAction>();
        this.filesActions = new ArrayList<IFilesDetectorAction>();
        this.testThreads = new ThreadGroup("Test Threads");
        
        this.macroSubstituter = new AttributeMacroSubstituter();
        
        this.inMaintenance = false;

        try
        {
            this.failureThreshold = Integer.parseInt(this.configuration.getProperty("Action_Failure_Threshold"));
            this.logger.debug("Loaded action fail threshold as " + this.failureThreshold);
        }
        catch (NumberFormatException nfe)
        {
            this.failureThreshold = 3;
            this.logger.debug("Failed to load the action failure threshold configuration item, so using " + 
                    this.failureThreshold +  " as the default.");
        }
        this.actionFailures = new HashMap<IAction, Integer>();
        
        /* Call initialisation. */
        this.logger.debug("Calling derived class implementing init.");
        this.init();
        
        /* Start data transfer service. */
        this.dataTransfer = new DataTransferWatcher(this);
        this.dataTransfer.start();
    }
    
    /**
     * Initialise method which derived classes should implement to register rig type
     * specific actions.
     * 
     * This method is called by the <code>AbstractRig</code> constructor to 
     * complete the initialisation of the rig type class.
     */
    protected abstract void init();

    /**
     * Registers an action to be performed when its corresponding method(s) is
     * called.
     *  
     * @param action instance of IAction to be registered, a <code>null</code>
     *              action will throw a <code>NullPointerException</code> as a 
     *              null action may not be registered
     * @param type action type, the type must corresponded to its action type 
     *              interface (e.g. ActionType.Test corresponds to
     *              ITest action).
     * @return true if the action was successfully registered
     */
    protected boolean registerAction(final IAction action, final ActionType type)
    {
        switch (type)
        {
            case ACCESS: /* Adding an access action. */
                this.logger.debug("Requested to register an access action type.");
                if (!(action instanceof IAccessAction))
                {
                    this.logger.error("Provided action type instance with class name " + 
                            action.getClass().getCanonicalName() + "is not an access action type (must be derived " +
                            "from au.edu.uts.edu.remotelabs.rigclient.rig.IAccessAction. Action type registration" +
                            "has failed.");
                    return false;
                }
                
                synchronized (this.accessActions)
                {
                    for (IAccessAction a : this.accessActions)
                    {
                        /* Only the instance is checked not the type. This is to 
                         * support multiple uses of the same type. It is assumed
                         * using the same instance is a bug. */
                        if (a == action)
                        {
                            this.logger.error("Cannot register the same action instance twice.");
                            return false;
                        }
                    }
                    /* Looks good so actually register the action. */
                    final IAccessAction access = (IAccessAction)action;
                    this.logger.info("Registering an access action with provided type of " + access.getActionType()
                            + ".");
                    return this.accessActions.add(access);
                }
                
            case SLAVE_ACCESS: /* Adding a slave access action. */
                this.logger.debug("Requested to register an slave access action type.");
                if (!(action instanceof ISlaveAccessAction))
                {
                    this.logger.error("Provided action type instance with class name " + 
                            action.getClass().getCanonicalName() + "is not a slave access action type (must be " +
                            "derived from au.edu.uts.edu.remotelabs.rigclient.rig.ISlaveAccessAction. " +
                            "Action type registration has failed.");
                    return false;
                }
                
                synchronized(this.slaveActions)
                {
                    for (ISlaveAccessAction a : this.slaveActions)
                    {
                        /* Only the instance is checked not the type. This is to 
                         * support multiple uses of the same type. It is assumed
                         * using the same instance is a bug. */
                        if (a == action)
                        {
                            this.logger.error("Cannot register the same action instance twice.");
                            return false;
                        }
                    }
                    /* Looks good so actually register the action. */
                    final ISlaveAccessAction slave = (ISlaveAccessAction)action;
                    this.logger.info("Registering a slave access action with provided type of " + 
                            slave.getActionType() + ".");
                    return this.slaveActions.add(slave);
                }
                
            case NOTIFY: /* Adding a notify action. */
                this.logger.debug("Requested to register an notify access action type.");
                if (!(action instanceof INotifyAction))
                {
                    this.logger.error("Provided action type instance with class name " + 
                            action.getClass().getCanonicalName() + "is not a notify action type (must be " +
                            "derived from au.edu.uts.edu.remotelabs.rigclient.rig.INotifyAction. " +
                            "Action type registration has failed.");
                    return false;
                }
                
                synchronized (this.notifyActions)
                {
                    for (INotifyAction a : this.notifyActions)
                    {
                        /* Only the instance is checked not the type. This is to 
                         * support multiple uses of the same type. It is assumed
                         * using the same instance is a bug. */
                        if (a == action)
                        {
                            this.logger.error("Cannot register the same action instance twice.");
                            return false;
                        }
                    }
                    /* Looks good so actually register the action. */
                    final INotifyAction notify = (INotifyAction)action;
                    this.logger.info("Registering a notify access action with provided type of " + 
                            notify.getActionType() + ".");
                    return this.notifyActions.add(notify);
                }
                
            case RESET: /* Adding a reset action. */
                this.logger.debug("Requested to register an reset access action type.");
                if (!(action instanceof IResetAction))
                {
                    this.logger.error("Provided action type instance with class name " + 
                            action.getClass().getCanonicalName() + "is not a reset action type (must be " +
                            "derived from au.edu.uts.edu.remotelabs.rigclient.rig.IResetAction. " +
                            "Action type registration has failed.");
                    return false;
                }
                
                synchronized (this.resetActions)
                {
                    for (IResetAction a : this.resetActions)
                    {
                        /* Only the instance is checked not the type. This is to 
                         * support multiple uses of the same type. It is assumed
                         * using the same instance is a bug. */
                        if (a == action)
                        {
                            this.logger.error("Cannot register the same action instance twice.");
                            return false;
                        }
                    }
                    /* Looks good so actually register the action. */
                    final IResetAction reset = (IResetAction)action;
                    this.logger.info("Registering a reset access action with provided type of " +
                            reset.getActionType() + ".");
                    return this.resetActions.add(reset);
                }
                
            case TEST: /* Adding a test action. */
                this.logger.debug("Requested to register a test access action type.");
                if (!(action instanceof ITestAction))
                {
                    this.logger.error("Provided action type instance with class name " + 
                            action.getClass().getCanonicalName() + "is not a test action type (must be " +
                            "derived from au.edu.uts.edu.remotelabs.rigclient.rig.ITestAction. " +
                            "Action type registration has failed.");
                    return false;
                }
                
                synchronized (this.testActions)
                {
                    for (ITestAction a : this.testActions)
                    {
                        /* Only the instance is checked not the type. This is to 
                         * support multiple uses of the same type. It is assumed
                         * using the same instance is a bug. */
                        if (a == action)
                        {
                            this.logger.error("Cannot register the same action instance twice.");
                            return false;
                        }
                    }
                    /* Looks good so actually register the action. */
                    final ITestAction test = (ITestAction)action;
                    this.logger.info("Registering a test access action with provided type of " + 
                            test.getActionType() + ".");
                    
                    /* Test actions run it their own thread, so create one and start it. */
                    final Thread thr = new Thread(this.testThreads, test);
                    thr.setName(test.getActionType());
                    thr.setDaemon(true); /* To not keep the JVM chugging on shutdown. */
                    thr.start();
                    return this.testActions.add(test);
                }
                
            case DETECT: /* Adding an activity detector type. */
                this.logger.debug("Requested to register an activity detector type.");
                if (!(action instanceof IActivityDetectorAction))
                {
                    this.logger.error("Provided action type instance with class name " + 
                            action.getClass().getCanonicalName() + "is not an activity detector action type (must be " +
                            "derived from au.edu.uts.edu.remotelabs.rigclient.rig.IActivityDetectorAction. " +
                            "Action type registration has failed.");
                    return false;
                }
                
                synchronized (this.detectionActions)
                {
                    for (IActivityDetectorAction a : this.detectionActions)
                    {
                        if (a == action)
                        {
                            this.logger.error("Cannot register the same action instance twice.");
                            return false;
                        }
                    }
                    
                    final IActivityDetectorAction detector = (IActivityDetectorAction)action;
                    this.logger.info("Registering an activity detector action with provided type of " + 
                            detector.getActionType() + ".");
                    return this.detectionActions.add(detector);
                }
                
            case FILES: /* Adding a session file detector type. */
                this.logger.debug("Requested to  register a file detector type.");
                if (!(action instanceof IFilesDetectorAction))
                {
                    this.logger.error("Provided action type instance with class name " + 
                            action.getClass().getCanonicalName() + " is not a file detector type (must be " +
                            "derived from " + IFilesDetectorAction.class.getCanonicalName() + ". Action type " +
                            "registration has failed.");
                    return false;
                }
                
                synchronized (this.filesActions)
                {
                    for (IFilesDetectorAction a : this.filesActions)
                    {
                        if (a == action)
                        {
                            this.logger.error("Cannot register the same action instance twice.");
                            return false;
                        }
                        
                        final IFilesDetectorAction detector = (IFilesDetectorAction)action;
                        this.logger.info("Registering an session file detector action with provided type of " +
                                detector.getActionType() + '.');
                        return this.filesActions.add(detector);
                    }
                }
                
            default:
                /* DODGY This is an impossible situation that _should_ never be hit. 
                 * Java type safety ahoy! */
                this.logger.error("Look out the window, the pigs are flying!");
                throw new IllegalStateException("Look out the window, the pigs are flying!");
        }
    }

    @Override
    public Map<String, String> getAllRigAttributes()
    {
        final Map<String, String> allProps = this.configuration.getAllProperties();
        final Map<String, String> substituedProps = new HashMap<String, String>(allProps.size());
        
        for (Entry<String, String> prop : allProps.entrySet())
        {
            try
            {
                substituedProps.put(prop.getKey(), this.macroSubstituter.substitueMacros(prop.getValue()));
            }
            catch (Exception e)
            {
                this.logger.warn("Failed to substitute macros of rig attribute with key " + prop.getKey() + " and with" +
                        " configured value as " + prop.getValue() + ". Using unsubstituted value.");
                substituedProps.put(prop.getKey(), prop.getValue());
            }
        }
        
        return substituedProps;
    }

    @Override
    public String[] getCapabilities()
    {
        /* DODGY Keeping the old misspelled capabilities configuration property
         * for backwards compatibility. */
        String cap = this.configuration.getProperty("Rig_Capabilites");
        if (cap == null || "".equals(cap)) cap = this.configuration.getProperty("Rig_Capabilities");
        
        this.logger.debug("Loaded Rig_Capabilities configuration item as " + cap + ".");
        
        if (cap == null)
        {
            this.logger.error("Rig client capabilities configuration item not found. Please check configuration" +
            		"at " + this.configuration.getConfigurationInfomation() + " and ensure the field " +
            		"'Rig_Capabilities' is present and populated with a comma seperated list of rig client " +
            		"capability tokens.");
            return null;
        }
        
        /* Extract tokens from configuration string. */
        String tokens[] = cap.split(",");
        final StringBuilder buf = new StringBuilder(40);
        buf.append("Rig client capabilites are ");
        for (int i = 0; i < tokens.length; i++)
        {
            tokens[i] = tokens[i].trim();
            buf.append(i);
            buf.append(": ");
            buf.append(tokens[i]);
            if (i != tokens.length - 1) buf.append(", ");
        }
        
        return tokens;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.rig.IRig#getName()
     */
    @Override
    public String getName()
    {
        final String name = this.configuration.getProperty("Rig_Name");
        this.logger.debug("Loaded Rig_Name configuration item as " + name);
        if (name == null)
        {
            this.logger.error("Rig client name configuration item not found. Please check configuration" +
                    "at " + this.configuration.getConfigurationInfomation() + " and ensure the field " +
                    "'Rig_Name' is present and populated with a rig name string.");
        }

        return name;
    }

    @Override
    public String getRigAttribute(final String key)
    {
        this.logger.debug("Requested to provide the " + key + " rig attribute.");
        if (key == null)
        {
            this.logger.info("Received a null rig attribute request, provided a null response.");
            return null;
        }
        
        final String value = this.configuration.getProperty(key);
        if (value == null)
        {
            this.logger.debug("Rig attribute value for " + key + " not found.");
            return null;
        }
        else
        {
            this.logger.debug("Found rig attribute value, value of " + key + " is " + value + ".");
        }
        try
        {
            return this.macroSubstituter.substitueMacros(value);
        }
        catch (Exception e)
        {
            this.logger.warn("Failed to substitute macros of rig attribute with key " + key + " and with" +
                    " configured value as " + value + ". Using unsubstituted value.");
            return value;
        }
    }

    @Override
    public String getType()
    {
        final String type = this.configuration.getProperty("Rig_Type");
        this.logger.debug("Loaded Rig_Type configuration item as " + type);
        if (type == null)
        {
            this.logger.error("Rig client type configuration item not found. Please check configuration" +
                    "at " + this.configuration.getConfigurationInfomation() + " and ensure the field " +
                    "'Rig_Type' is present and populated with a rig type string.");
        }

        return type;
    }

    @Override
    public String getMaintenanceReason()
    {
        return this.maintenanceReason;
    }

    @Override
    public String getMonitorReason()
    {
        final StringBuilder buf = new StringBuilder();
        for (ITestAction test : this.testActions)
        {
            if (test == null) continue;
            if (!test.getStatus())
            {
                buf.append(test.getActionType());
                buf.append(": ");
                buf.append(test.getReason());
                buf.append(' ');
            }
        }
        
        if (this.maintenanceReason != null)
        {
            buf.append(this.maintenanceReason);
        }
        
        if (buf.length() == 0)
        {
            return null;
        }
        
        this.logger.debug("Monitor bad reason:" + buf.toString() + ".");
        return buf.toString();
    }

    @Override
    public boolean isMonitorStatusGood()
    {
        if (this.inMaintenance) return false;
        for (ITestAction test : this.testActions)
        {
            if (test == null) continue;
            if (!test.getStatus()) return false;
        }
        return true;
    }

    @Override
    public boolean isNotInMaintenance()
    {
        return !this.inMaintenance;
    }

    @Override
    public boolean setInterval(final int interval)
    {
        this.logger.info("Setting test interval to " + (interval) + " minutes.");
        
        synchronized (this.testActions)
        {
            for (ITestAction test : this.testActions)
            {
                if (test == null) continue;
                test.setInterval(interval);                
            }
        }
        
        /* DODGY In hindsight, the interface of setInterval shouldn't return
         * a value. */
        return true;
    }

    @Override
    public boolean setMaintenance(final boolean offline, final String reason, final boolean runTests)
    {
        if (offline)
        {
            if (this.isSessionActive())
            {
                this.logger.warn("Terminating session and revoking users because maintenance" +
                		"mode is being set.");
                this.revoke();
            }
            this.logger.warn("Putting the rig into maintenance mode. Provided reason is " + reason + ".");
            this.inMaintenance = true;
            this.maintenanceReason = reason;
            
            if (runTests)
            {
                this.logger.debug("Monitor tests will be run in maintenance mode.");
                this.startTests();
            }
            else
            {
                this.logger.debug("Monitor tests will not be run in maintenance mode.");
                this.stopTests();
            }
        }
        else
        {
            this.logger.debug("Taking the rig out of maintenance mode.");
            this.inMaintenance = false;
            this.maintenanceReason = null;
            
            /* Clear the action failure counts. */
            synchronized (this.actionFailures)
            {
                for (int c : this.actionFailures.values())
                {
                    if (c > this.failureThreshold)
                    {
                        this.logger.info("Clearing maintenance state caused by the action failure threshold being " +
                        		"exceeded.");
                    }
                }
                this.actionFailures.clear();
            }
            
            this.logger.debug("Monitor tests are going to be started.");
            this.startTests();
        }
        
        return true;
    }

    @Override
    public void startTests()
    {
        this.logger.debug("Starting exerciser tests.");
        
        synchronized (this.testActions)
        {
            for (ITestAction test : this.testActions)
            {
                if (test == null) continue;
                test.startTest();
            }
        }
    }

    @Override
    public void stopTests()
    {
        this.logger.debug("Stopping exerciser tests.");
        
        synchronized (this.testActions)
        {
            for (ITestAction test : this.testActions)
            {
                if (test == null) continue;
                test.stopTest();
            }
        }
    }
    
    /**
     * Gets the list of tests. The list is unmodifiable and will throw an 
     * {@link UnsupportedOperationException} if modified.
     * 
     * @return list of tests
     */
    public List<ITestAction> getTests()
    {
        return Collections.unmodifiableList(this.testActions);
    }
  
    @Override
    public Map<String, Session> getSessionUsers()
    {
        return this.getSessionUsersClone();
    }

    @Override
    public boolean addSlave(final String name, final boolean passive)
    {
        this.logger.debug("Adding slave user " + name + " with " + (passive ? "passive" : "active") + " access.");
        
        /* Slaves can't be assigned if there is no session. */
        synchronized (this.sessionUsers)
        {
            if (!this.isSessionActive()) return false;

            if (this.sessionUsers.containsKey(name))
            {
                final Session currentPerm = this.sessionUsers.get(name);
                if (passive && currentPerm == Session.SLAVE_PASSIVE) // Requested passive, already a passive user
                {
                    this.logger.warn("User " + name + " is already a passive slave user, nothing to do.");
                    return false;
                }
                else if (!passive && currentPerm == Session.SLAVE_ACTIVE) // Requested active, already an active user
                {
                    this.logger.warn("User " + name + " is already an active slave user, nothing to do.");
                    return false;
                }
                else if (currentPerm == Session.SLAVE_ACTIVE || currentPerm == Session.SLAVE_PASSIVE)
                {
                    this.logger.info("Revoking slave user (" + name + ") to change their permission.");
                    if (!this.revokeSlave(name))
                    {
                        this.logger.warn("Failed changing slave permission because revocation of previous slave " +
                        "permission failed revocation.");
                        return false;
                    }    
                }
            }
        }
        
        synchronized (this.slaveActions)
        {
            for (int i = 0; i < this.slaveActions.size(); i++)
            {
                ISlaveAccessAction action = this.slaveActions.get(i);
                if (action == null) continue;
                if (!action.assign(name, passive))
                {
                    this.setMaintenanceFromActionFailure(action, ActionType.SLAVE_ACCESS);
                    return false;
                }
            }
        }
        
        synchronized (this.sessionUsers)
        {
            this.sessionUsers.put(name, passive ? Session.SLAVE_PASSIVE : Session.SLAVE_ACTIVE);
        }
        
        return true;
    }

    @Override
    public boolean assign(final String name)
    {
        this.logger.debug("Assigning master access to " + name);
        
        synchronized (this.sessionUsers)
        {
            /* Check there isn't an existing session (master user). */
            if (this.sessionUsers.containsValue(Session.MASTER))
            {
                this.logger.warn("Failed allocation, the rig is already in session with another user.");
                return false;
            }

            /* Check the rig is in a state to be assigned. */
            if (!(this.isMonitorStatusGood() && this.isNotInMaintenance()))
            {
                final StringBuilder builder = new StringBuilder(50);
                builder.append("Failed allocation, the rig is not in an operable state.");
                if (this.getMaintenanceReason() != null)
                {
                    builder.append(" The rig is in maintenance with reason " + this.getMaintenanceReason() + "."); 
                }
                if (this.getMonitorReason() != null)
                {
                    builder.append(" The rig is detected to be bad with reason " + this.getMonitorReason() + ".");
                }
                this.logger.warn(builder.toString());
                return false;
            }

            /* Stop tests. */
            this.stopTests();

            for (int i = 0; i < this.accessActions.size(); i++)
            {
                IAccessAction action = this.accessActions.get(i);
                if (action == null) continue;            
                if (!action.assign(name))
                {
                    this.setMaintenanceFromActionFailure(action, ActionType.ACCESS);
                    this.startTests();
                    return false;
                }
            }

            this.sessionUsers.put(name, Session.MASTER);
        }
        
        /* Notify the data transfer service a session has started. */
       this.dataTransfer.sessionStarted(name);
        
        return true;
    }

    @Override
    public boolean hasPermission(final String name, final Session ses)
    {
        Session userSes = this.sessionUsers.get(name);

        if (userSes == null)
        {
            return ses == Session.NOT_IN;
        }

        switch (userSes)
        {
            case MASTER:
                return ses == Session.MASTER || ses == Session.SLAVE_ACTIVE || ses == Session.SLAVE_PASSIVE ||
                ses == Session.NOT_IN;
            case SLAVE_ACTIVE:
                return ses == Session.SLAVE_ACTIVE || ses == Session.SLAVE_PASSIVE || ses == Session.NOT_IN;
            case SLAVE_PASSIVE:
                return ses == Session.SLAVE_PASSIVE || ses == Session.NOT_IN;
        }

        this.logger.error("All permission states unaccounted for, someone has been been naughty" +
        		"and put a Session.NOT_IN user as a session user. Please file bug report.");
        throw new IllegalStateException("Error in AbstractRig->hasPermission");
    }

    @Override
    public Session isInSession(final String name)
    {
        synchronized (this.sessionUsers)
        {
            if (!this.sessionUsers.containsKey(name))
            {
                return Session.NOT_IN;
            }

            return this.sessionUsers.get(name);
        }
    }

    @Override
    public boolean notify(final String message)
    {
        if (!this.isSessionActive()) return false;
        
        boolean ret = true;
        final String typeSafe[] = new String[0];
        this.logger.debug("Running notification for all users with message " + message);
        
        synchronized (this.notifyActions)
        {
            for (INotifyAction action : this.notifyActions)
            {
                if (action == null) continue;
                if (!action.notify(message, this.sessionUsers.keySet().toArray(typeSafe)))
                {
                    this.setMaintenanceFromActionFailure(action, ActionType.NOTIFY);
                    ret = false;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean revoke()
    {
        this.logger.debug("Terminating a session and revoking access from master user.");
        boolean ret = true;
        
        /* First check there is a master user. */
        if (!this.isSessionActive())
        {
            this.logger.warn("Unable to terminate a session as there is no currently running session.");
            return false;
        }
        
        /* Notify the data transfer service that the session has completed. */
        this.dataTransfer.sessionComplete();
        
        /* Revoke any running batch control operations. */
        if (this instanceof IRigControl)
        {
            IRigControl controlRig = (IRigControl)this;
            
            /* Abort any running batch control invocations. */
            if (controlRig.isBatchRunning())
            {
                this.logger.info("Aborting batch control because of session termination.");
                controlRig.abortBatch();
                
            }
            
            /* Clear batch state so it does not persist across sessions. */
            controlRig.clearBatchState();
            
            /* Clear the primitive control cache. */
            controlRig.expungePrimitiveControllerCache();
        }

        String user = null;
        Session perm;
        synchronized (this.sessionUsers)
        {
            for (Entry<String, Session> entry : this.getSessionUsersClone().entrySet())
            {
                user = entry.getKey();
                perm = entry.getValue();

                if (perm == Session.MASTER) // If master, run revoke actions  
                {
                    this.logger.info("Terminating and revoking master session user: " + user + "."); 
                    for (int i = 1; i <= this.accessActions.size(); i++)
                    {
                        IAccessAction action = this.accessActions.get(this.accessActions.size() - i);
                        if (action == null) continue;
                        if (!action.revoke(user))
                        {
                            this.setMaintenanceFromActionFailure(action, ActionType.ACCESS);
                            ret = false;
                        }
                    }
                }
                else if ((perm == Session.SLAVE_ACTIVE || perm == Session.SLAVE_PASSIVE) && !this.revokeSlave(user))
                {
                    ret = false;
                }
            }

            /* Remove the session users. */
            this.sessionUsers.clear();
        }
        
        /* Reset the rig. */
        synchronized (this.resetActions)
        {
            this.logger.debug("Running the rig reset actions.");
            for (int i = 0; i < this.resetActions.size(); i++)
            {
                IResetAction action = this.resetActions.get(i);
                if (action == null) continue;
                if (!action.reset())
                {
                    this.setMaintenanceFromActionFailure(action, ActionType.RESET);
                    ret = false;
                }
            }
        }
        
        /* Start the tests. */
        this.startTests();
        return ret;
    }

    @Override
    public boolean isSessionActive()
    {
        return this.sessionUsers.values().contains(Session.MASTER);
    }
   
    @Override
    public boolean isActivityDetected()
    {
        /* If not session, there is obviously not activity. */
        if (!this.isSessionActive())
        {
            return false;
        }
        
        /* The default answer should be too report activity if activity cannot
         * be determined. */
        if (this.detectionActions.size() == 0)
        {
            return true;
        }
        
        for (IActivityDetectorAction detector : this.detectionActions)
        {
            if (detector.detectActivity()) return true;
        }
        return false;
    }

    @Override
    public boolean revokeSlave(final String name)
    {
        boolean ret = true;
        this.logger.info("Revoking slave access from: " + name + '.');
        
        Session perm = Session.NOT_IN;
        synchronized (this.sessionUsers)
        {
            /* First check the user is actually a slave user. */
            perm = this.isInSession(name);
            if (perm != Session.SLAVE_ACTIVE && perm != Session.SLAVE_PASSIVE)
            {
                this.logger.warn("Failed revoking slave access, provided user " + name + " is not a slave user.");
                return false;
            }
            
            /* Remove user from being a slave user. */
            this.sessionUsers.remove(name);
        }
        
        /* Run the slave revocation actions. */
        synchronized (this.slaveActions)
        {
            for (int i = 1; i <= this.slaveActions.size(); i++)
            {
                ISlaveAccessAction action = this.slaveActions.get(this.slaveActions.size() - i);
                if (action == null) continue;
                if (!action.revoke(name, perm == Session.SLAVE_PASSIVE ? true : false))
                {
                    this.setMaintenanceFromActionFailure(action, ActionType.SLAVE_ACCESS);
                    ret = false;
                }
            }
        }
        
        return ret;
    }
    
    @Override
    public List<File> detectSessionFiles()
    {
        // TODO 
        return Collections.emptyList();
    }
    
    /**
     * Returns the failure reason of an action. May return <tt>null</tt> if
     * the previous invocation of the action method did not fail.
     * 
     * @param type action type
     * @return action failure reason
     */
    public String getActionFailureReason(ActionType type)
    {
        StringBuilder buf = new StringBuilder();
        List<? extends IAction> actions = new ArrayList<IAction>(0);
        String tmp;
        
        switch (type)
        {
            case ACCESS:
                actions = this.accessActions;
                break;
            case DETECT:
                actions = this.detectionActions;
                break;
            case NOTIFY:
                actions = this.notifyActions;
                break;
            case RESET:
                actions = this.resetActions;
                break;
            case SLAVE_ACCESS:
                actions = this.slaveActions;
                break;
            case TEST:
                actions = this.testActions;                
                break;
            case FILES:
                actions = this.filesActions;
        }
        
        for (IAction a : actions)
        {
            if ((tmp = a.getFailureReason()) != null)
            {
                if (buf.length() > 0) buf.append(' ');
                buf.append(a.getActionType());
                buf.append(':');
                buf.append(tmp);
            }
        }
        
        return buf.toString();
    }
    
    /**
     * Cleans up this class. This should be called before the rig client
     * is shutdown. 
     */
    public void cleanUp()
    {
        this.testThreads.interrupt();
        this.dataTransfer.shutdown();
    }
    
    /**
     * Puts the rig into maintenance mode because of a failure reported by
     * an action.
     * 
     * @param action action which failed
     */
    protected void setMaintenanceFromActionFailure(final IAction action, final ActionType type)
    {
        String typeStr = null;
        switch (type)
        {
            case ACCESS:
                typeStr = "Session access";
                break;
            case SLAVE_ACCESS:
                typeStr = "Slave access";
                break;
            case NOTIFY:
                typeStr = "Notification";
                break;
            case RESET:
                typeStr = "Device reset";
                break;
            case TEST:
                typeStr = "Exerciser test";
                break;
            case DETECT:
                typeStr = "Activity detector";
                break;
            case FILES:
                typeStr = "Files detector";
                break;
            default:
                throw new IllegalStateException("Unknown action type, this is a bug.");
        }
        this.logger.error(typeStr + " action of type " + action.getActionType() + " failed with reason " + 
                action.getFailureReason() + ".");

        synchronized (this.actionFailures)
        {
            if (this.actionFailures.containsKey(action))
            {
                this.actionFailures.put(action, (this.actionFailures.get(action)) + 1);
                this.logger.info("Incrementing action failure for " + action.getActionType() + ". Current value is " +
                        + this.actionFailures.get(action) + ". Threshold for action failues is " + 
                        this.failureThreshold + ".");
            }
            else
            {
                this.actionFailures.put(action, 1);
                this.logger.info("First action failure for " + action.getActionType() + "Threshold for action failures" +
                        " is " + this.failureThreshold + ".");
            }

            if (this.actionFailures.get(action) > this.failureThreshold)
            {
                this.logger.error("Rig has been put into maintenance mode because an action failure " + 
                        action.getActionType() + " count has reached the failure threshold.");
                this.inMaintenance = true;
                this.maintenanceReason = typeStr + " action failed with reason " + action.getFailureReason(); 
            }
        }
    }
    
    /**
     * Returns a deep clone of the session users hash map.
     * 
     * @return session users map clone
     */
    private Map<String, Session> getSessionUsersClone()
    {
        synchronized (this.sessionUsers)
        {
            final Map<String, Session> sessions = new HashMap<String, Session>(this.sessionUsers.size());
            for (Entry<String, Session> entry : this.sessionUsers.entrySet())
            {
                sessions.put(entry.getKey(), entry.getValue());
            }
            return sessions;
        }
    }

}
