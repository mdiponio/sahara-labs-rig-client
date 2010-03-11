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
 * @date 12th December 2009
 *
 * Changelog:
 * - 29/12/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import au.edu.uts.eng.remotelabs.rigclient.main.RigClientDefines;

/**
 * Appends messages to a log file. The log file is rolled to backup files 
 * when it exceeds a maximum file size. After a maximum backup file number
 * is exceeded, the oldest backup files are deleted.
 */
public class RolledFileLogger extends AbstractLog4JLogger
{
    @Override
    protected void setAppeneder()
    {
       /* File logger. */
        final IConfig conf = ConfigFactory.getInstance();
        final String fileName = conf.getProperty("Log_File_Name");
        
        if (fileName == null)
        {
            System.err.println("FATAL - Failed to load logging file name from configuration.");
            System.err.println("Check the " + conf.getConfigurationInfomation() + " is present, readable by the rig client");
            System.err.println("and the 'Log_File_Name' field is present.");
            System.err.println();
            System.err.println("Shutting down...");
            throw new RuntimeException("Unable to load file logger file name.");
        }
        
        final int fileSize = this.getConfigInt(conf.getProperty("Log_File_Max_Size", "10"), 10);
        final int numBackups = this.getConfigInt(conf.getProperty("Log_File_Backups", "5"), 5);
        
        /** Frustratingly this is different on Windows and Linux. */
        if (System.getProperty("os.name").startsWith("Windows")) this.stackLevel = 4;
        
        try
        {
            RollingFileAppender appender = new RollingFileAppender(
                    new PatternLayout(AbstractLog4JLogger.PATTERN_LAYOUT), fileName);
            appender.setMaximumFileSize(fileSize * 1024 * 1024);
            appender.setMaxBackupIndex(numBackups);
            
            this.logger.addAppender(appender);
        }
        catch (IOException ioe)
        {
            /* Check the file. */
            final File file = new File(fileName);
            if (file.isFile())
            {
                System.err.println("FATAL - Failed to add a Log4J file appender. (FileLogger->addAppender)");
                RigClientDefines.reportBug("log4j File Logger", ioe);
                System.err.println("Shutting down, sorry...");
                throw new RuntimeException("Failed adding file appender.");
            }
            else
            {
                System.err.println("FATAL - The loaded configuration file name (" + fileName + ") is not a valid file.");
                System.err.println("Change the configuration file property 'Log_File_Name' in");
                System.err.println(conf.getConfigurationInfomation() + " to a valid file.");
                throw new RuntimeException("Log file does not exist.");
            }
        }
    }
}
