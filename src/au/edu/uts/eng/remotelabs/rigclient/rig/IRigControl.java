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
 * @date 5th October 2009
 *
 * Changelog:
 * - 05/10/2009 - mdiponio - Initial file creation.
 * - 22/10/2009 - mdiponio - Fixed BatchState enum Javadoc.
 */
package au.edu.uts.eng.remotelabs.rigclient.rig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for rig client control. Rig client provide control takes
 * two forms:
 *   - Batch control - invocation of a background process with an unloaded
 *          instruction file. Batch control is singular and may be polled for 
 *          run status and aborted once started.  A previously started batch
 *          invocation will block subsequent batch requests until it is 
 *          finished.
 *   - Primitive control - synchronous control of rig devices. Primitive 
 *          control uses the <em>Front Controller</em> pattern to call a
 *          method (the request action) on class (the request controller).
 *          If the requested method is an instance method (instance based), the
 *          controller is <em>lazy loaded</em> on first request and cleaned up
 *          at the end of the master users session.  
 */
public interface IRigControl
{
    /**
     * Batch control states.
     */
    public enum BatchState
    {
        /** None started. */
        CLEAR,
        /** Invocation currently in progress. */
        IN_PROGRESS,
        /** Invocation successfully completed. */
        COMPLETE,
        /** Invocation failed. */
        FAILED,
        /** Rig type not supporting batch. */
        NOT_SUPPORTED
    }
    
    
    
    /**
     * Starts a batch control invocation with the provided file reference.
     * Prior to calling this, the <code>isBatchRunning</code> method should
     * be called to ensure no previous batch control is still in execution.
     * If so, this method fails.
     * 
     * Also note, results of a prior batch control invocation will be
     * cleared upon the calling of this method, provided the batch
     * invocation is complete.
     * 
     * @param fileName file name of instruction
     * @param userName user who requested batch control
     * @return true if successful, false otherwise
     */
    public boolean performBatch(String fileName, String userName);
    
    /**
     * Specifies if a batch control invocation is in progress. 
     * 
     * @return true if batch running
     */
    public boolean isBatchRunning();
    
    /**
     * Gets the current batch state.
     * 
     * @return enumeration of <code>BatchState</code> specifying current state
     */
    public BatchState getBatchState();
    
    /**
     * Gets the progress of the current batch control invocation. The result
     * may be:
     *         - 1 .. 99 - Percentage complete
     *         - 0       - None started
     *         - 100     - Complete
     *         - -1      - Rig type doesn't support getting progress 
     *
     * @return progress indication or -1 if not supported
     */
    public int getBatchProgress();
    
    /**
     * Aborts a batch control invocation. The response is true if
     * no batch control invocation is running.
     * 
     * @return true if no invocation is running
     */
    public boolean abortBatch();
    
    /**
     * Gets the batch results for either the in-progress or finished batch
     * control invocation. If no invocations have been started 
     * <code>null</code> is returned.
     * 
     * @return batch results for in-progress or finished invocation
     */
    public BatchResults getBatchResults();
    
    /**
     * Performs a primitive request. 
     * 
     * @param req primitive control request information
     * @return primitive response 
     */
    public PrimitiveResponse performPrimitive(PrimitiveRequest req);
    
    /**
     * Expunges the controller cache. 
     */
    public void expungePrimitiveControllerCache();
    
    /**
     * Batch control invocation results.
     */
    public class BatchResults
    {
        /** Batch state. */
        private BatchState state;
        
        /** Instruction file reference. */
        private String instructionFile;
        
        /** List of generated result files. */
        private List<String> resultsFiles;
        
        /** Standard out output of the batch process. */
        private String standardOut;
        
        /** Standard error output of the batch process. */
        private String standardErr;
        
        public BatchResults()
        {
            this.resultsFiles = new ArrayList<String>();
        }

        /**
         * @return the state
         */
        public BatchState getState()
        {
            return this.state;
        }

        /**
         * @param state the state to set
         */
        public void setState(BatchState state)
        {
            this.state = state;
        }

        /**
         * @return the instructionFile
         */
        public String getInstructionFile()
        {
            return this.instructionFile;
        }

        /**
         * @param instructionFile the instructionFile to set
         */
        public void setInstructionFile(String instructionFile)
        {
            this.instructionFile = instructionFile;
        }

        /**
         * @return the results files
         */
        public String[] getResultsFiles()
        {
            return this.resultsFiles.toArray(new String[1]);
        }

        /**
         * @param file name of file to add
         */
        public void addResultsFile(String file)
        {
            this.resultsFiles.add(file);
        }

        /**
         * @return the standardOut
         */
        public String getStandardOut()
        {
            return standardOut;
        }

        /**
         * @param standardOut the standardOut to set
         */
        public void setStandardOut(String standardOut)
        {
            this.standardOut = standardOut;
        }

        /**
         * @return the standardErr
         */
        public String getStandardErr()
        {
            return standardErr;
        }

        /**
         * @param standardErr the standardErr to set
         */
        public void setStandardErr(String standardErr)
        {
            this.standardErr = standardErr;
        }

        /**
         * @param resultsFiles the resultsFiles to set
         */
        public void setResultsFiles(List<String> resultsFiles)
        {
            this.resultsFiles = resultsFiles;
        }
    }
    
    /**
     * Primitive control request information.
     */
    public class PrimitiveRequest
    {
        /** Primitive controller. */
        private String controller;
        
        /** Action to call on controller. */
        private String action;
        
        /** Request parameters (name - value). */
        private Map<String, String> parameters;
        
        /**
         * Constructor.
         */
        public PrimitiveRequest () 
        { 
            /* Does nothing. */
        }
        
        /**
         * Constructor.
         *  
         * @param _controller primitive request controller
         * @param _action primitive request action 
         */
        public PrimitiveRequest(String _controller, String _action)
        {
            this.controller = _controller;
            this.action = _action;
            
            this.parameters = new HashMap<String, String>();
        }

        /**
         * @return the controller
         */
        public String getController()
        {
            return this.controller;
        }

        /**
         * @param _controller the controller to set
         */
        public void setController(String _controller)
        {
            this.controller = _controller;
        }

        /**
         * @return the action
         */
        public String getAction()
        {
            return this.action;
        }

        /**
         * @param _action the action to set
         */
        public void setAction(String _action)
        {
            this.action = _action;
        }

        /**
         * @return the parameters
         */
        public Map<String, String> getParameters()
        {
            return this.parameters;
        }

        /**
         * @param parameters the parameters to set
         */
        public void setParameters(Map<String, String> parameters)
        {
            this.parameters = parameters;
        }
        
        /**
         * @param param the parameter key to add
         * @param val the parameter value to add
         */
        public void addParameter(String param, String val)
        {
            this.parameters.put(param, val);
        }
    }
    
    /**
     * Primitive control response.
     */
    public class PrimitiveResponse
    {
        /** Was operation successful. */
        private boolean wasSuccessful;
        
        /** List of generated result files. */
        private Map<String, String> results;

        /** Error code if operation was not successful. */
        private int errorCode;
        
        /** Error reason if operation was not successful. */
        private String errorReason;
        
        public PrimitiveResponse()
        {
            this.results = new HashMap<String, String>();
        }

        /**
         * @return the wasSuccessful
         */
        public boolean isWasSuccessful()
        {
            return this.wasSuccessful;
        }

        /**
         * @param wasSuccessful the wasSuccessful to set
         */
        public void setWasSuccessful(boolean wasSuccessful)
        {
            this.wasSuccessful = wasSuccessful;
        }

        /**
         * @return the results
         */
        public Map<String, String> getResults()
        {
            return this.results;
        }

        /**
         * @param results the results to set
         */
        public void setResults(Map<String, String> results)
        {
            this.results = results;
        }
        
        /**
         * @param name the name of the result to add
         * @param value the value of the result to add
         */
        public void addResult(String name, String value)
        {
            this.results.put(name, value);
        }

        /**
         * @return the errorCode
         */
        public int getErrorCode()
        {
            return this.errorCode;
        }

        /**
         * @param errorCode the errorCode to set
         */
        public void setErrorCode(int errorCode)
        {
            this.errorCode = errorCode;
        }

        /**
         * @return the errorReason
         */
        public String getErrorReason()
        {
            return this.errorReason;
        }

        /**
         * @param errorReason the errorReason to set
         */
        public void setErrorReason(String errorReason)
        {
            this.errorReason = errorReason;
        }
    }
}
