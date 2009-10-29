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
 * @date 23rd October 2009
 *
 * Changelog:
 * - 23/10/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.rig.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;

/**
 * Abstract class for batch control. Batch control in Sahara is run by 
 * invoking a system executable which does the batch control and
 * (presumably) generates a results file. The following methods
 * should be overridden for rig specific batch control.
 * <ul>
 *     <li><code>checkFile()</code> - Should provide a sanity check
 *     on the provided batch instruction file.</li>
 *     <li>init() - Should setup the command, arguments, working directory and 
 *     execution environment.</li>
 *     <li>sync() - This is run after the completion of batch
 *     control and should do synchronisation of the generated results 
 *     files to a persistent store.</li>
 * </ul>
 */
public abstract class AbstractBatchRunner implements Runnable
{
    /** Batch process. */
    private Process batchProc;
    
    /** Standand out of the batch process. */
    private BufferedReader batchStdOut;
    
    /** Standard err of the batch process. */
    private BufferedReader batchStdErr;
    
    /** Command line argument to invoke. */
    protected String command;
    
    /** Command line argument to invoke. */ 
    protected List<String> commandArgs;
    
    /** File name of instruction file. */
    protected String fileName;
    
    /** Batch process working directory base (base has a new folder added to
     *  be set as the batch processes CWD. */
    protected String workingDirBase;
    
    /** Batch process working directory. */
    protected String workingDir;
    
    /** Enviroment variables for the batch process. */
    protected Map<String, String> envMap;
    
    /** Flag to specify if batch execution has started. */
    protected boolean started;
    
    /** Flag to specify if batch execution is running. */
    protected boolean running;
    
    /** Flag to specify if batch execution failed. */
    protected boolean failed;
    
    /** Batch process exit code. */
    protected int exitCode;
    
    /** Results files. */
    protected List<String> resultsFiles;
    
    /** Logger. */
    protected ILogger logger;
    
    /**
     * Constructor.
     * 
     * @param file uploaded instruction file
     */
    public AbstractBatchRunner(final String file)
    {
        this.logger = LoggerFactory.getLoggerInstance();
        this.logger.debug("Creating a Batch Runner.");
        
        this.fileName = file;
        this.commandArgs = new ArrayList<String>();
        this.envMap = new HashMap<String, String>();
    }
    
    /**
     * Processes the batch control invocation performing the following steps, 
     * calling the listed methods:
     * <ol>
     *      <li><strong><code>init()</code></strong> - Command, argument, 
     *      working directory and enviroment setip.</li>
     *      <li><strong><code>checkFile()</code></strong> - Instruction file
     *      test.</li>
     *      <li><strong><code>invoke()</code></strong> - Process creation.
     *      </li>
     *      <li><strong><code>sync()</code></strong> - Generated file 
     *      syncronisation.<li>
     *      <li><strong><code>clean()</code></strong> - Invocation 
     *      cleanup.</li>
     * </ol>
     * If a preceding method fails (returns <code>false</code>), successive 
     * methods in the stack are not called, except <code>cleanup()</code>
     * which is always called.
     */
    @Override
    public void run()
    {
        try
        {
            if (!this.init()) // Batch initialisation
            {
                this.logger.warn("Batch process initialisation has failed.");
                this.failed = true;
                return;
            }
            else if (!this.checkFile()) // Check batch file
            {
                this.logger.warn("Uploaded batch file has failed the instruction file sanity test.");
                this.failed = true;
            }          
            else if (!this.invoke()) // Invoke command and create process
            {
                this.logger.warn("Batch control invocation has failed.");
                this.failed = true;
                this.running = false;
                return;
            }
            
            this.batchStdOut = new BufferedReader(new InputStreamReader(this.batchProc.getInputStream()));
            this.batchStdErr = new BufferedReader(new InputStreamReader(this.batchProc.getErrorStream()));
            
            this.logger.info("Invoked batch command at " + this.getTimeStamp('/', ' ', ':'));
            this.exitCode = this.batchProc.waitFor(); // Blocks up process completion.
            this.logger.info("The batch control process terminated with error code " + this.exitCode + " at " +
                    this.getTimeStamp('/', ' ', ':') + ".");
            
            this.running = false;
            this.failed = false;
            
            if (!this.failed)
            {            
                /* Find out the list of results files. */            
                this.resultsFiles = Arrays.asList(new File(this.workingDir).list());
                
                /* Syncronise. */
                if (!this.sync())
                {
                    this.logger.warn("File syncronisation has failed.");
                }
            }
        }
        catch (Exception ex)
        {
            this.logger.warn("Batch control failed with exception of type " + ex.getClass().getName() + " and with " +
            		"message " + ex.getMessage());
        }
        finally
        {
            /* Cleanup. */
            this.cleanup();
        }
    }
    
    /**
     * Sets the required batch control process variables. The variables that
     * should be set in this method are:
     * 
     * <ul>
     *     <li><strong><code>command</code></strong> - The batch command name
     *     executable name.</li>
     *     <li><strong><code>commandArgs</code></strong> - The command line 
     *     arguments to provide for the command. The arguments are provided
     *     in the same order as they are entered into the
     *     <code>commandArgs</code> list. <strong>NOTE:</strong> You should
     *     add the <code>fileName</code> field as a parameter for the command
     *     since this contains an absolute reference to the uploaded 
     *     instruction file.</li>
     *     <li><strong><code>workingDirBase</code></strong> - The working
     *     directory base for the batch command process. If not set, the default
     *     base is the system temporary directory. The base is appended with
     *     a new folder to contain the results files from the batch executable
     *     (<em>assuming</em> it puts the files in its current working 
     *     directory.)</li>
     *     <li><strong><code>envMap</code></strong> - Environment variable map
     *     for the batch process enviroment. Additions to this will be appended
     *     to the default enviroment variables.</li>
     * </ul>
     * 
     * @return true if successful, false otherwise
     */
    protected abstract boolean init();
    
    /**
     * Provides a sanity check on the provided batch instruction file.
     * For example, it should check the file extension, file size and/or
     * file magic number.
     * 
     * It may also check the file extension and apply a transform. For
     * example, if the extension is <code>.gz</code>, it may used 
     * <code>GZIPInputStream</code> to uncompress the file. 
     * 
     * @return true if the file passed the sanity check, false otherwise
     */
    protected abstract boolean checkFile();
    
    /**
     * Invokes the batch control executable providing the instruction files
     * as a command line parameter. Returns are invocation and does not
     * wait for completeion.
     * 
     * @return true if the executable was successfully invoked
     * @throws IOException exception caused by starting the batch command
     */
    protected boolean invoke() throws IOException
    {
        /* --------------------------------------------------------------------
         * ---- 1. Set up batch process builder. ------------------------------
         * ------------------------------------------------------------------*/
        if (this.command == null)
        {
            this.logger.warn("No command to invoke has been set for batch control, batch invocation has failed.");
            return false;
        }
        
        final List<String> invocation = new ArrayList<String>();
        invocation.add(this.command);
        invocation.addAll(this.commandArgs);
        this.logger.info("Batch command that will be invoked is " + this.command);
        this.logger.info("Batch command arguments are " + this.commandArgs.toString());
        
        final ProcessBuilder builder = new ProcessBuilder(invocation);
        
        /* --------------------------------------------------------------------
         * ---- 2. Set up working directory. ----------------------------------
         * --------------------------------------------------------------------*/
        if (this.workingDirBase == null) // NOPMD by mdiponio on 29/10/09 11:54 AM
        {
            /* Default is a directory in the system directory. */
            this.workingDirBase = System.getProperty("java.io.tmpdir");
            if (this.workingDirBase == null)
            {
                this.logger.warn("The temporary directory system property (java.io.tmpdir) did not find the system" +
                        "temporary directory. Going to use the current Rig Client working directory as base.");
                this.workingDirBase = System.getProperty("user.dir");
                this.logger.warn("Using Rig Client working directory " + this.workingDirBase + " as batch control " +
                        "working directory base.");
            }
        }
        else
        {
            this.logger.info("User set batch working directory base is " + this.workingDirBase);
        }
        /* Add a new folder with a timestamp as the name. */
        this.workingDir = this.workingDirBase + File.separatorChar + this.getTimeStamp('-', '-', '-');
        this.logger.info("Batch process working directory is " + this.workingDir);
        final File wDir = new File(this.workingDir);
        if (!wDir.mkdir())
        {
            final File wdBase = new File(this.workingDirBase);
            final StringBuffer buf = new StringBuffer(22);
            buf.append("Unable to create batch process working directory (" + this.workingDir + ").");
            if (!wdBase.exists()) buf.append(" Base " + this.workingDirBase + " does not exist.");
            if (!wdBase.isDirectory()) buf.append(" Base " + this.workingDirBase + " is not a directory");
            if (!wdBase.canWrite()) buf.append("Base " + this.workingDirBase +  " is not writeable");
            this.logger.warn(buf.toString());
            return false;
        }
        builder.directory(wDir);
        
        /* --------------------------------------------------------------------
         * ---- 3. Set up enviroment variables. -------------------------------
         * ------------------------------------------------------------------*/
        final Map<String, String> env = builder.environment();
        for (Entry<String, String> e : this.envMap.entrySet())
        {
            this.logger.info("Adding batch control env variable " + e.getKey() + " with value " + e.getValue());
            env.put(e.getKey(), e.getValue());
        }
        this.logger.info("Batch control environment variables: " + env.toString());
        
        /* --------------------------------------------------------------------
         * ---- 4. Test command. ----------------------------------------------
         * ------------------------------------------------------------------*/
        /* If the configured command is not absolute, test each path 
         * directory to try and find command. */
        File commFile = new File(this.command);
        if (!commFile.isAbsolute())
        {
            this.logger.info("Batch control command is not absolute. This isn't recommended. Will test path for" +
            		"command file.");
            final String path = env.get("path");
            if (path == null)
            {
                this.logger.error("Path enviroment variable (path) for batch control not found, batch failing.");
                return false;
            }
            /* Iterate through path elements and test if the command is one of the path directories. */
            boolean found = false;
            for (String p : path.split(File.pathSeparator))
            {
                this.logger.debug("Looking for batch command " + this.command + " in path directory " + p + ".");
                commFile = new File(p + File.separator + this.command);
                if (commFile.isFile())
                {
                    this.logger.debug("Found batch command " + this.command + " in path directory " + p + ".");
                    found = true;
                    break;
                }
            }
            
            if (!found)
            {
                this.logger.warn("Batch command " + this.command + " not found, batch control failing.");
                return false;
            }
            
            /* Update the process builder command with the absolute reference to the batch command. */
            final List<String> comm = builder.command();
            comm.set(0, commFile.getAbsolutePath());
        }
        
        if (!commFile.canExecute())
        {
            this.logger.warn("Do not have permission to execute batch command " + commFile.getAbsolutePath() + 
                    ". Batch control failing.");
            return false;
        }
        
        /* --------------------------------------------------------------------
         * ---- 5. Invoke command. --------------------------------------------
         * ------------------------------------------------------------------*/
        this.batchProc = builder.start();
        this.started = true;
        this.running = true;
        return true;
    }
    
    /**
     * This method is run after completion of the batch control executable 
     * and should do cleanup, if needed and file synchronisation to some 
     * persistent user accessible store, again if needed.
     * 
     * This is provided as a utility with a guarantee that it is run
     * <strong>only</strong> after the successful completion of the 
     * batch control executable.
     * 
     * @return true if successfully completed
     */
    protected abstract boolean sync();
    
    /**
     * Terminate the batch process.
     */
    public void terminate()
    {
        this.logger.info("Terminating batch process.");
        this.batchProc.destroy();
    }
    
    /**
     * Cleans up a batch control invocation.
     */
    protected void cleanup()
    {
        this.logger.debug("Cleaning a batch control invocation.");
        try
        {
            this.batchStdErr.close();
            this.batchStdOut.close();
            
            final IConfig conf = ConfigFactory.getInstance();
            if (Boolean.parseBoolean(conf.getProperty("Clean_Up_Batch")))
            {
                this.recusiveDelete(new File(this.workingDir));
            }
        }
        catch (IOException ex)
        {
            this.logger.warn("Failed to clean a batch control invocation because of error " + ex.getMessage());
        }
        
    }
    
    /**
     * Reads and returns the batch process standard output stream. 
     * <code>null</code> is returned if there is nothing to read.
     * 
     * @return batch process standard out 
     */
    public String getBatchStandardOut()
    {
        final StringBuffer buf = new StringBuffer();
        try
        {
            while (this.batchStdOut.ready())
            {
                buf.append(this.batchStdOut.readLine());
            }
        }
        catch (IOException e)
        {
            this.logger.warn("Reading batch process standard out failed with error " + e.getMessage() + ".");
            return null;
        }
        
        this.logger.debug("Read from batch process standard out: " + buf.toString());
        return buf.toString();
    }
    
    /**
     * Reads and returns the batch process standard error stream. 
     * <code>null</code> is returned if there is nothing to read.
     * 
     * @return batch process standard error 
     */
    public String getBatchStandardError()
    {
        final StringBuffer buf = new StringBuffer();
        try
        {
            while (this.batchStdErr.ready())
            {
                buf.append(this.batchStdErr.readLine());
            }
        }
        catch (IOException e)
        {
            this.logger.warn("Reading batch process standard error stream failed with error " + e.getMessage() + ".");
            return null;
        }
        
        this.logger.debug("Read from batch process standard err: " + buf.toString());
        return buf.toString();
    }
    
    /**
     * Returns <code>true</code> if a batch process has been started. Started
     * is defined as when the command is called to fork a new process. This
     * returns <code>true</code> irrespective if the batch process is
     * started and running or if the batch process is started and complete.
     * 
     * @return true if batch started, false otherwise
     */
    public boolean isStarted()
    {
        return this.started;
    }
    
    /**
     * Returns <code>true</code> if a batch process has been started and is
     * currently running. 
     * 
     * @return true if batch is running, false otherwise
     */
    public boolean isRunning()
    {
        return this.running;
    }
    
    /**
     * Returns <code>true</code> if the batch invocation attempt has failed.
     * 
     * @return true if batch failed
     */
    public boolean isFailed()
    {
       return this.failed; 
    }
    
    /**
     * Returns the decteced results files for a batch control invocation.
     * Detected results files are those that are generated in the set
     * batch process working directory.
     * 
     * @return
     */
    public List<String> getResultsFiles()
    {
        if (this.resultsFiles == null) return null;
        
        return new ArrayList<String>(Arrays.asList(this.resultsFiles.toArray(new String[0])));
    }
    
    /**
     * Gets a formatted time stamp with day, month, year, hour, minute
     * and second components seperated with the <code>glue</code> parameter.
     * 
     * @param dateGlue character to append date components with
     * @param join charater to join date and time with
     * @paarm timeGlue charater to append time with
     * @return String formatted timestamp
     */
    protected String getTimeStamp(final char dateGlue, final char join, final char timeGlue)
    {
        final Calendar cal = Calendar.getInstance();
        final StringBuffer buf = new StringBuffer();
        buf.append(cal.get(Calendar.DATE));
        buf.append(dateGlue);
        buf.append(cal.get(Calendar.MONTH) + 1);
        buf.append(dateGlue);
        buf.append(cal.get(Calendar.YEAR));
        buf.append(dateGlue);
        buf.append(cal.get(Calendar.HOUR_OF_DAY));
        buf.append(dateGlue);
        buf.append(cal.get(Calendar.MINUTE));
        buf.append(dateGlue);
        buf.append(cal.get(Calendar.SECOND));
        return buf.toString();
    }
    
    /**
     * Recursively delete a directory. The directory and all its child files 
     * and elements will be deleted.
     *  
     * @param dir directory to delete
     * @throws IOException 
     */
    private void recusiveDelete(final File file) throws IOException
    {   
        this.logger.debug("Attempting to delete " + file.getName());
        
        if (!file.getCanonicalPath().startsWith(this.workingDirBase))
        {
            this.logger.warn("Not deleting directory " + file.getCanonicalPath() + " since it does not appear to be " +
            		"located in " + this.workingDirBase);
            return;
        }
        
        /* DODGY This apparently works according to Java Bug ID 4313887. */
        if (!file.getCanonicalPath().equals(file.getAbsolutePath()))
        {
            this.logger.warn("Not deleting " + file.getAbsolutePath() + " as it appears to a symbolic link.");
            return;
        }
        
        if (file.isDirectory())
        {
            /* Delete all the nested directories and files. */
            for (File f : file.listFiles())
            {
                if (f.isDirectory())
                {
                    this.recusiveDelete(f);
                }
                else
                {
                    if (!f.delete()) this.logger.warn("Unable to delete " + f.getCanonicalPath());
                }
            }
        }
        
        if (!file.delete()) this.logger.warn("Unable to delete " + file.getCanonicalPath());
        
    }
}
