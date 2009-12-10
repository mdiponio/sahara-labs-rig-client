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
 * @date 10th December 2009
 *
 * Changelog:
 * - 10/12/2009 - mdiponio - Initial file creation.
 */

/**
 * ErrorType.java This file was auto-generated from WSDL by the Apache Axis2
 * version: 1.4.1 Built on : Aug 19, 2008 (10:13:44 LKT)
 */

package au.edu.uts.eng.remotelabs.rigclient.protocol.types;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMConstants;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.ADBDataSource;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;

/**
 * ErrorType bean class.
 */
public class ErrorType implements ADBBean
{
    /*
     * This type was generated from the piece of schema that had name =
     * ErrorType Namespace URI =
     * http://remotelabs.eng.uts.edu.au/rigclient/protocol Namespace Prefix =
     * ns1
     */

    private static final long serialVersionUID = 8082628791916144814L;
    
    protected int code;

    protected String operation;

    protected String reason;

    protected boolean traceTracker = false;
    protected String trace;


    private static String generatePrefix(String namespace)
    {
        if (namespace.equals("http://remotelabs.eng.uts.edu.au/rigclient/protocol")) return "ns1";
        return BeanUtil.getUniquePrefix();
    }

    public static boolean isReaderMTOMAware(XMLStreamReader reader)
    {
        boolean isReaderMTOMAware = false;

        try
        {
            isReaderMTOMAware = Boolean.TRUE.equals(reader.getProperty(OMConstants.IS_DATA_HANDLERS_AWARE));
        }
        catch (IllegalArgumentException e)
        {
            isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
    }

    public OMElement getOMElement(final QName parentQName, final OMFactory factory) throws ADBException
    {
        OMDataSource dataSource = new ADBDataSource(this, parentQName)
        {
            @Override
            public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException
            {
                ErrorType.this.serialize(this.parentQName, factory, xmlWriter);
            }
        };
        return new OMSourcedElementImpl(parentQName, factory, dataSource);
    }

    public XMLStreamReader getPullParser(QName qName) throws ADBException
    {

        ArrayList<Serializable> elementList = new ArrayList<Serializable>();
        ArrayList<QName> attribList = new ArrayList<QName>();

        elementList.add(new QName("", "code"));
        elementList.add(ConverterUtil.convertToString(this.code));
        
        elementList.add(new QName("", "operation"));
        if (this.operation != null)
        {
            elementList.add(ConverterUtil.convertToString(this.operation));
        }
        else
        {
            throw new ADBException("operation cannot be null!!");
        }

        elementList.add(new QName("", "reason"));
        if (this.reason != null)
        {
            elementList.add(ConverterUtil.convertToString(this.reason));
        }
        else
        {
            throw new ADBException("reason cannot be null!!");
        }
        
        if (this.traceTracker)
        {
            elementList.add(new QName("", "trace"));
            if (this.trace != null)
            {
                elementList.add(ConverterUtil.convertToString(this.trace));
            }
            else
            {
                throw new ADBException("trace cannot be null!!");
            }
        }

        return new ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
    }

    private String registerPrefix(XMLStreamWriter xmlWriter, String namespace) throws XMLStreamException
    {
        String prefix = xmlWriter.getPrefix(namespace);
        if (prefix == null)
        {
            prefix = ErrorType.generatePrefix(namespace);
            while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null)
            {
                prefix = BeanUtil.getUniquePrefix();
            }

            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }

        return prefix;
    }

    public void serialize(final QName parentQName, final OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter)
            throws XMLStreamException, ADBException
    {
        this.serialize(parentQName, factory, xmlWriter, false);
    }

    public void serialize(final QName parentQName, final OMFactory factory, MTOMAwareXMLStreamWriter xmlWriter,
            boolean serializeType) throws XMLStreamException, ADBException
    {

        String prefix = parentQName.getPrefix();
        String namespace = parentQName.getNamespaceURI();

        if (namespace != null && namespace.trim().length() > 0)
        {
            String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null)
            {
                xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
            }
            else
            {
                if (prefix == null)
                {
                    prefix = ErrorType.generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        else
        {
            xmlWriter.writeStartElement(parentQName.getLocalPart());
        }

        if (serializeType)
        {

            String namespacePrefix = this.registerPrefix(xmlWriter,
                    "http://remotelabs.eng.uts.edu.au/rigclient/protocol");
            if (namespacePrefix != null && namespacePrefix.trim().length() > 0)
            {
                this.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix
                        + ":ErrorType", xmlWriter);
            }
            else
            {
                this.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "ErrorType", xmlWriter);
            }
        }

        namespace = "";
        if (!namespace.equals(""))
        {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null)
            {
                prefix = ErrorType.generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix, "code", namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);

            }
            else
            {
                xmlWriter.writeStartElement(namespace, "code");
            }
        }
        else
        {
            xmlWriter.writeStartElement("code");
        }

        if (this.code == Integer.MIN_VALUE)
            throw new ADBException("code cannot be null!!");
        else
        {
            xmlWriter.writeCharacters(ConverterUtil.convertToString(this.code));
        }

        xmlWriter.writeEndElement();

        namespace = "";
        if (!namespace.equals(""))
        {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null)
            {
                prefix = ErrorType.generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix, "operation", namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);

            }
            else
            {
                xmlWriter.writeStartElement(namespace, "operation");
            }
        }
        else
        {
            xmlWriter.writeStartElement("operation");
        }

        if (this.operation == null)
        {
            throw new ADBException("operation cannot be null!!");
        }
        else
        {
            xmlWriter.writeCharacters(this.operation);

        }
        xmlWriter.writeEndElement();

        namespace = "";
        if (!namespace.equals(""))
        {
            prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null)
            {
                prefix = ErrorType.generatePrefix(namespace);
                xmlWriter.writeStartElement(prefix, "reason", namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            else
            {
                xmlWriter.writeStartElement(namespace, "reason");
            }
        }
        else
        {
            xmlWriter.writeStartElement("reason");
        }

        if (this.reason == null)
        {
            throw new ADBException("reason cannot be null!!");
        }
        else
        {
            xmlWriter.writeCharacters(this.reason);
        }

        xmlWriter.writeEndElement();
        if (this.traceTracker)
        {
            namespace = "";
            if (!namespace.equals(""))
            {
                prefix = xmlWriter.getPrefix(namespace);
                if (prefix == null)
                {
                    prefix = ErrorType.generatePrefix(namespace);
                    xmlWriter.writeStartElement(prefix, "trace", namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
                else
                {
                    xmlWriter.writeStartElement(namespace, "trace");
                }
            }
            else
            {
                xmlWriter.writeStartElement("trace");
            }

            if (this.trace == null)
            {
                throw new ADBException("trace cannot be null!!");
            }
            else
            {
                xmlWriter.writeCharacters(this.trace);
            }
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();

    }
    
    public int getCode()
    {
        return this.code;
    }
    
    public String getReason()
    {
        return this.reason;
    }
    
    public String getOperation()
    {
        return this.operation;
    }
    
    public String getTrace()
    {
        return this.trace;
    }

    public void setCode(int param)
    {
        this.code = param;
    }

    public void setOperation(String param)
    {
        this.operation = param;
    }

    public void setReason(String param)
    {
        this.reason = param;
    }

    public void setTrace(String param)
    {
        if (param != null)
        {
            this.traceTracker = true;
        }
        else
        {
            this.traceTracker = false;
        }
        this.trace = param;
    }

    private void writeAttribute(String prefix, String namespace, String attName, String attValue,
            XMLStreamWriter xmlWriter) throws XMLStreamException
    {
        if (xmlWriter.getPrefix(namespace) == null)
        {
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }
        xmlWriter.writeAttribute(namespace, attName, attValue);

    }

    public static class Factory
    {
        public static ErrorType parse(XMLStreamReader reader) throws Exception
        {
            ErrorType object = new ErrorType();
            try
            {
                while (!reader.isStartElement() && !reader.isEndElement())
                {
                    reader.next();
                }

                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null)
                {
                    String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type");
                    if (fullTypeName != null)
                    {
                        String nsPrefix = null;
                        if (fullTypeName.indexOf(":") > -1)
                        {
                            nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                        }
                        nsPrefix = nsPrefix == null ? "" : nsPrefix;

                        String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                        if (!"ErrorType".equals(type))
                        {
                            // find namespace for the prefix
                            String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                            return (ErrorType) ExtensionMapper.getTypeObject(nsUri, type, reader);
                        }
                    }
                }

                reader.next();
                while (!reader.isStartElement() && !reader.isEndElement())
                {
                    reader.next();
                }
                if (reader.isStartElement() && new QName("", "code").equals(reader.getName()))
                {
                    String content = reader.getElementText();
                    object.setCode(ConverterUtil.convertToInt(content));
                    reader.next();
                }
                else
                {
                    throw new ADBException("Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                {
                    reader.next();
                }
                if (reader.isStartElement() && new QName("", "operation").equals(reader.getName()))
                {
                    String content = reader.getElementText();
                    object.setOperation(ConverterUtil.convertToString(content));
                    reader.next();
                }
                else
                {
                    throw new ADBException("Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                {
                    reader.next();
                }
                if (reader.isStartElement() && new QName("", "reason").equals(reader.getName()))
                {
                    String content = reader.getElementText();
                    object.setReason(ConverterUtil.convertToString(content));
                    reader.next();
                }
                else
                {
                    throw new ADBException("Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                {
                    reader.next();
                }
                if (reader.isStartElement() && new QName("", "trace").equals(reader.getName()))
                {
                    String content = reader.getElementText();
                    object.setTrace(ConverterUtil.convertToString(content));
                    reader.next();
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                {
                    reader.next();
                }
                if (reader.isStartElement())
                {
                    throw new ADBException("Unexpected subelement " + reader.getLocalName());
                }
            }
            catch (XMLStreamException e)
            {
                throw new Exception(e);
            }

            return object;
        }
    }
}
