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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The default page.
 */
public class IndexPage extends AbstractPage
{
    private Map<String, String> links;
    
    public IndexPage()
    {
        this.links = new LinkedHashMap<String, String>();
        this.links.put("Status", "/status");
        this.links.put("Maintenance", "/mainten");
        this.links.put("Configuration", "/config");
        this.links.put("Logs", "/logs");
        this.links.put("Runtime Information", "/info");
        this.links.put("Documentation", "/doc");
    }
    
    @Override
    public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {   
        this.println("<div id='linklist'>");
        this.println("   <ul id='ullinklist'>");

        int i = 0;
        for (Entry<String, String> e : this.links.entrySet())
        {
            String classes = "linkbut plaina";
            if (i == 0) classes += " ui-corner-top";
            else if (i == this.links.size() - 1) classes += " ui-corner-bottom";

            this.println("       <li><a class='" + classes + "' href='" + e.getValue() + "'>");
            this.println("           <div class='linkbutcont'>");
            this.println(                "<span class='linkicon ui-icon ui-icon-arrowreturnthick-1-e' style='float:left; margin:0 10px 0'></span>" 
                    + e.getKey());
            this.println("           </div>");
            this.println("      </a></li>");

            i++;
        }
       
       this.println("   </ul>");
       this.println("</div>");
    }

    @Override
    protected String getPageHeader()
    {
        return "Welcome to " + this.config.getProperty("Rig_Name");
    }
    
    @Override
    protected String getPageType()
    {
        return "Main";
    }
}
