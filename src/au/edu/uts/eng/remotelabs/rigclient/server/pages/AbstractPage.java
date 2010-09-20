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
 * @date 20th September 2010
 */
package au.edu.uts.eng.remotelabs.rigclient.server.pages;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;

/**
 * Interface for the a embedded server page.
 */
public abstract class AbstractPage
{
    /** The output buffer. */
    protected StringBuilder buf;
    
    /** Servlet writer. */
    private PrintWriter out;
    
    /** Logger. */
    protected ILogger logger;
    
    public AbstractPage()
    {
        this.logger = LoggerFactory.getLoggerInstance();
        this.buf = new StringBuilder();
    }
    
    /**
     * Services the request.
     * 
     * @param req request
     * @param resq response
     * @throws IOException 
     */
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        this.out = resp.getWriter();
        
        resp.setStatus(HttpServletResponse.SC_OK);
        this.println("<!DOCTYPE html>");
        this.println("<html>");
        this.addHead();
        this.println("<body onload='initPage()' onresize='resizeFooter()'>");
        this.println("<div id='wrapper'>");
        this.addHeader();
        this.addNavbar("Index");
        
        /* Actual page contents. */
        this.contents(req, resp);
        
        this.addFooter();
        this.println("</div>");
        this.println("</body>");
        this.println("</html>");
        
        resp.getWriter().print(this.buf);
    }
    
    /**
     * Adds the page specific contents.
     * 
     * @param req request 
     * @param resp response
     * @throws IOException 
     */
    public abstract void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    
    /**
     * Adds a line to the output buffer.
     * 
     * @param line output line
     */
    public void println(String line)
    {
        this.buf.append(line);
        this.buf.append('\n');
    }
    
    /**
     * Flushes the output buffer.
     */
    public void flushOut()
    {
        this.out.print(this.buf);
        this.buf = new StringBuilder();
    }
    
    /**
     * Adds the head section to the page.
     * 
     * @param out output writer
     */
    protected void addHead()
    {
        this.println("<head>");
        this.println("  <title>Rig Client</title>");
        this.println("  <meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
        this.println("  <link href='/css/rigclient.css' media='screen' rel='stylesheet' type='text/css' />");
        this.println("  <link href='/css/smoothness/jquery-ui.custom.css' rel='stylesheet' type='text/css' />");
        this.println("  <script type='text/javascript' src='/js/jquery.js'> </script>");
        this.println("  <script type='text/javascript' src='/js/jquery-ui.js'> </script>");
        this.println("  <script type='text/javascript' src='/js/rigclient.js'> </script>");
        this.println("</head>");
    }
   
    /**
     * Adds the header to the page.
     * 
     * @param out output writer
     */
    protected void addHeader()
    {
        this.println("<div id='header'>");
        this.println("    <div class='headerimg' >");
        this.println("        <a href='http://sourceforge.net/projects/labshare-sahara/'>" +
        		"<img src='/img/logo.png' alt='Sourceforge Project' /></a>");
        this.println("   </div>");
        this.println("   <div class='headerimg' >");
        this.println("        <img src='/img/sahara.png' alt='Sahara Labs' />");
        this.println("    </div>");
        this.println("    <div id='labshareimg'>");
        this.println("        <a href='http://www.labshare.edu.au/'><img src='/img/labshare.png' alt='LabShare' /></a>");
        this.println("    </div>");
        this.println("</div>");   
    }
    
    /**
     * Adds the header to the page.
     * 
     * @param out output writer
     */
    protected void addNavbar(String page)
    {
        this.println("<div class='menu ui-corner-bottom'>");
        this.println("   <ol id='menu'>");
        
        /* Navigation bar contents. */
        this.innerNavBar("Main", "/");
        this.innerNavBar("Status", "/status");
        this.innerNavBar("Maintenance", "/mainten");
        this.innerNavBar("Configuration", "/config");
        this.innerNavBar("Documentation", "/doc");
        this.innerNavBar("Logs", "/logs");
        this.innerNavBar("Runtime Information", "/info");
        
        this.println("   </ol>");
        this.println("   <div id='slide'> </div>");
        this.println("</div>");
    }
    
    /**
     * Adds a nav bar item to the navigation bar.
     * 
     * @param out output writer
     * @param name name to display
     * @param path path to page
     */
    private void innerNavBar(String name, String path)
    {
        if (this.getPageType().equals(name))
        {
            this.println("    <li value='1'>");
        }
        else
        {
            this.println("    <li>");
        }
        this.println("            <a class='plaina apad' href='" + path + "'>" + name + "</a>");
        this.println("        </li>");
    }
    
    /**
     * Adds the footer to the page.
     * 
     * @param out output writer
     */
    protected void addFooter()
    {
        this.println("<div id='footer' class='ui-corner-top'>");
        this.println("   <a class='plaina alrpad' href='http://www.labshare.edu.au'>Labshare Australia</a>");
        this.println("   | <a class='plaina alrpad' href='http://www.uts.edu.au'>&copy; University of Technology, Sydney 2009 - 2010</a>");
        this.println("</div>");
    }
    
    /**
     * Gets the page type.
     * 
     * @return page type
     */
    protected abstract String getPageType();
}
