/**
 * SlaveRelease.java This file was auto-generated from WSDL by the Apache Axis2
 * version: 1.4.1 Built on : Aug 19, 2008 (10:13:44 LKT)
 */

package au.edu.uts.eng.remotelabs.rigclient.protocol.types;

/**
 * SlaveRelease bean class
 */

public class SlaveRelease implements org.apache.axis2.databinding.ADBBean
{

    /**
     * Factory class that keeps the parse method
     */
    public static class Factory
    {

        /**
         * static method to create the object Precondition: If this object is an
         * element, the current or next start element starts this object and any
         * intervening reader events are ignorable If this object is not an
         * element, it is a complex type and the reader is at the event just
         * after the outer start element Postcondition: If this object is an
         * element, the reader is positioned at its end element If this object
         * is a complex type, the reader is positioned at the end element of its
         * outer element
         */
        public static SlaveRelease parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception
        {
            SlaveRelease object = new SlaveRelease();

            try
            {

                while (!reader.isStartElement() && !reader.isEndElement())
                {
                    reader.next();
                }

                new java.util.Vector();

                while (!reader.isEndElement())
                {
                    if (reader.isStartElement())
                    {

                        if (reader.isStartElement()
                                && new javax.xml.namespace.QName("http://remotelabs.eng.uts.edu.au/rigclient/protocol",
                                        "slaveRelease").equals(reader.getName()))
                        {

                            object.setSlaveRelease(au.edu.uts.eng.remotelabs.rigclient.protocol.types.UserType.Factory
                                    .parse(reader));

                        } // End of if for expected property start element
                        else
                            // A start element we are not expecting indicates an
                            // invalid parameter was passed
                            throw new org.apache.axis2.databinding.ADBException("Unexpected subelement "
                                    + reader.getLocalName());

                    }
                    else
                    {
                        reader.next();
                    }
                } // end of while loop

            }
            catch (javax.xml.stream.XMLStreamException e)
            {
                throw new java.lang.Exception(e);
            }

            return object;
        }

    }// end of factory class

    /**
     * 
     */
    private static final long serialVersionUID = -5290540338648712576L;

    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
            "http://remotelabs.eng.uts.edu.au/rigclient/protocol", "slaveRelease", "ns2");

    private static java.lang.String generatePrefix(java.lang.String namespace)
    {
        if (namespace.equals("http://remotelabs.eng.uts.edu.au/rigclient/protocol")) return "ns2";
        return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * isReaderMTOMAware
     * 
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader)
    {
        boolean isReaderMTOMAware = false;

        try
        {
            isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader
                    .getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }
        catch (java.lang.IllegalArgumentException e)
        {
            isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
    }

    /**
     * field for SlaveRelease
     */

    protected au.edu.uts.eng.remotelabs.rigclient.protocol.types.UserType localSlaveRelease;

    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException
    {

        org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                SlaveRelease.MY_QNAME)
        {

            @Override
            public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                    throws javax.xml.stream.XMLStreamException
            {
                SlaveRelease.this.serialize(SlaveRelease.MY_QNAME, factory, xmlWriter);
            }
        };
        return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(SlaveRelease.MY_QNAME, factory, dataSource);

    }

    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException
    {

        // We can safely assume an element has only one type associated with it
        return this.localSlaveRelease.getPullParser(SlaveRelease.MY_QNAME);

    }

    /**
     * Auto generated getter method
     * 
     * @return au.edu.uts.eng.remotelabs.rigclient.protocol.types.UserType
     */
    public au.edu.uts.eng.remotelabs.rigclient.protocol.types.UserType getSlaveRelease()
    {
        return this.localSlaveRelease;
    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException
    {
        java.lang.String prefix = xmlWriter.getPrefix(namespace);

        if (prefix == null)
        {
            prefix = SlaveRelease.generatePrefix(namespace);

            while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null)
            {
                prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
            }

            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }

        return prefix;
    }

    public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException
    {
        this.serialize(parentQName, factory, xmlWriter, false);
    }

    public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException
    {

        // We can safely assume an element has only one type associated with it

        if (this.localSlaveRelease == null)
            throw new org.apache.axis2.databinding.ADBException("Property cannot be null!");
        this.localSlaveRelease.serialize(SlaveRelease.MY_QNAME, factory, xmlWriter);

    }

    /**
     * Auto generated setter method
     * 
     * @param param SlaveRelease
     */
    public void setSlaveRelease(au.edu.uts.eng.remotelabs.rigclient.protocol.types.UserType param)
    {

        this.localSlaveRelease = param;

    }

}