<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"
    xmlns:exts="java://cz.incad.xsl.XSLFunctions"
    xmlns:java="http://xml.apache.org/xslt/java">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:variable name="xslfunctions" select="exts:new()" />
    <xsl:param name="collectionPid" select="collectionPid" />
    <xsl:template match="/">
        <add>
            <xsl:for-each select="/response/result/doc">
            <doc>
                <xsl:for-each select="str">
                    <field>
                        <xsl:attribute name="name">
                            <xsl:value-of  select="@name"/>
                        </xsl:attribute>
                        <xsl:value-of select="." />
                    </field>
                </xsl:for-each>
                <xsl:for-each select="int">
                    <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                <xsl:for-each select="date">
                    <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                <xsl:for-each select="bool">
                    <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                <xsl:for-each select="arr/str">
                    <xsl:choose>
                        <xsl:when test="../@name = 'collection'">
                            <field><xsl:attribute name="name"><xsl:value-of  select="../@name"/></xsl:attribute><xsl:value-of select="." /></field>
                        </xsl:when>
                        <xsl:otherwise>
                            <field><xsl:attribute name="name"><xsl:value-of  select="../@name"/></xsl:attribute><xsl:value-of select="." /></field>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                </xsl:for-each>
                <xsl:for-each select="arr/int">
                    <field><xsl:attribute name="name"><xsl:value-of  select="../@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                
                <xsl:variable name="title"><xsl:value-of select="str[@name='dc.title']" /></xsl:variable>
                
                <xsl:for-each select="arr[@name='dc.creator']">
                    <field name="browse_autor" ><xsl:value-of select="exts:prepareCzech($xslfunctions, ./str)"/>##<xsl:value-of select="./str" /></field>
                </xsl:for-each>
                
                <field name="browse_title" >
                    <xsl:value-of select="exts:prepareCzech($xslfunctions, $title)"/>##<xsl:value-of select="$title"/>
                </field>
                <field name="collection"><xsl:value-of select="$collectionPid" /></field>
            </doc>
            </xsl:for-each>
        </add>
    </xsl:template>

</xsl:stylesheet>