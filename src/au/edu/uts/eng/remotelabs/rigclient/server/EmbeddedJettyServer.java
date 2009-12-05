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
 * @date 5th December 2009
 *
 * Changelog:
 * - 05/12/2009 - mdiponio - Initial file creation.
 */
package au.edu.uts.eng.remotelabs.rigclient.server;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;

import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;

/**
 * Embedded Jetty Server set up to handle SOAP requests using the Apache Axis
 * 2 
 */
public class EmbeddedJettyServer implements IServer
{
    /** Jetty server. */
    private Server server;
    
    /** Connector which receives and handles requests.. */
    private Connector connector;
    
    /** Configuration. */
    private final IConfig config;
    
    /** Logger. */
    private final ILogger logger;
    
    public EmbeddedJettyServer()
    {
        this.logger = LoggerFactory.getLoggerInstance();
        this.logger.debug("Creating a new embedded Jetty server.");
                        
        this.config = ConfigFactory.getInstance();        
        
    }
    
    
    /**
     * Initialise the server.
     * 
     * @throws ServerException error setting up server
     */
    private void init() throws ServerException
    {
        /* --------------------------------------------------------------------
         * ---- 1. Create the server. -----------------------------------------
         * ----------------------------------------------------------------- */
        /* The server is the main class for the Jetty HTTP server. It uses a 
         * connector to receive requests, a context to handle requests and 
         * a thread pool to manage concurrent requests.
         */
        this.server = new Server();
        
        /* --------------------------------------------------------------------
         * ---- 2. Create and configure the connector. ------------------------
         * ----------------------------------------------------------------- */
        /*
         * 
         */
        try
        {
            final int portNumber = Integer.parseInt(this.config.getProperty("Listening_Port"));
            if (portNumber < 1)
            {
                this.logger.fatal("Invalid listening server port number loaded from configuration. Check the " +
                		"configuration property 'Listening_Port' has a valid port number set.");
                throw new ServerException("Invalid port number configuration.");
            }
            
            // DODGY The maxIdleTimeout may need to set on the connector in case long
            // requests are truncated.
            this.connector = new SelectChannelConnector();
            this.connector.setPort(portNumber);
        }
        catch (NumberFormatException e)
        {
            this.logger.fatal("Failed to load the port number to listen for requests on. Check the configuration" +
            		" property 'Listening_Port' has a valid port number set.");
            throw new ServerException("Unable to load server port number.");
        }
        // TODO Add a HTTPS connector (SslSelectSocketConnector)
        this.server.setConnectors(new Connector[]{this.connector});
        
        
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.server.IServer#shutdownServer()
     */
    @Override
    public boolean shutdownServer()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* 
     * @see au.edu.uts.eng.remotelabs.rigclient.server.IServer#startListening()
     */
    @Override
    public boolean startListening()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /*
     * @see au.edu.uts.eng.remotelabs.rigclient.server.IServer#getAddress() 
     */
    @Override
    public String getAddress()
    {
        // TODO listening address
        return null;
    }

}
