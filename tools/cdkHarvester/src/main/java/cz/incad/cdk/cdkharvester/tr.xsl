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
    <xsl:param name="solr_url" select="'http://localhost:8080/solr/select'" />
	<!--  specified in order to testable -->
    <xsl:param name="_for_tests" select="false()" />

    <xsl:template match="/">
        <add>
            <xsl:for-each select="/response/result/doc">
            <doc>
                <!--
                <xsl:variable name="search_url">
                    <xsl:value-of select="$solr_url"/>?q=PID:<xsl:call-template name="url-encode">
                    <xsl:with-param name="str">"<xsl:value-of select="./str[@name='PID']"/>"</xsl:with-param></xsl:call-template></xsl:variable>
                -->    
                <xsl:variable name="query">"<xsl:value-of select="./str[@name='PID']"/>"</xsl:variable>    
				<!-- 
                <xsl:variable name="search_url"><xsl:value-of select="$solr_url"/>?q=PID:<xsl:value-of select="exts:encode($xslfunctions, $query)"/></xsl:variable>
                 -->
               	<xsl:variable name="search_url">
				    <xsl:choose>
			        	<xsl:when test="_for_tests">
            				<xsl:value-of select="concat($solr_url,'?q=PID:',exts:encode($xslfunctions, $query))" />
			        	</xsl:when>
          				<xsl:otherwise>
			                <xsl:variable name="_pid"><xsl:value-of select="./str[@name='PID']"/></xsl:variable>    
	          				<!-- 
	          				<xsl:value-of select="concat($solr_url,'?q=PID:',exts:encode($xslfunctions, $_pid))" />
	          				 -->
				        	<xsl:value-of select="concat($solr_url,'/',translate($_pid, ':', '_'))"/>
			        	</xsl:otherwise>
			   		</xsl:choose>
				</xsl:variable>
                    
                    
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
                    <field><xsl:attribute name="name"><xsl:value-of  select="../@name"/></xsl:attribute><xsl:value-of select="." /></field>
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
                
                <xsl:call-template name="collection">
                    <xsl:with-param name="search_url" select="$search_url" />
                </xsl:call-template>
                <!--
                <field name="collection" update="add"><xsl:value-of select="$collectionPid" /></field>
                -->
                <field name="compositeId" >
                   <xsl:value-of select="./str[@name='root_pid']"/>_<xsl:value-of select="./str[@name='PID']"/>
                </field>
            </doc>
            </xsl:for-each>
        </add>
    </xsl:template>
    
    <xsl:template name="collection">
        <xsl:param name="search_url"/>
        <xsl:variable name="orig" select="document($search_url)" />
        <xsl:for-each select="$orig/response/result/doc/arr[@name='collection']/str[.!=$collectionPid]">
                <xsl:if test="not(./text()=normalize-space($collectionPid))">
                    <field name="collection" ><xsl:value-of select="." /></field>
                </xsl:if>
        </xsl:for-each>
        <field name="collection"><xsl:value-of select="$collectionPid" /></field>
    </xsl:template>
    
    
<!--
 ISO-8859-1 based URL-encoding demo
       Written by Mike J. Brown, mike@skew.org.
       Updated 2002-05-20.

       No license; use freely, but credit me if reproducing in print.

       Also see http://skew.org/xml/misc/URI-i18n/ for a discussion of
       non-ASCII characters in URIs.
  
    -->
    <!--
     The string to URL-encode.
           Note: By "iso-string" we mean a Unicode string where all
           the characters happen to fall in the ASCII and ISO-8859-1
           ranges (32-126 and 160-255) 
    -->

    <xsl:template name="url-encode">
        <xsl:param name="str"/>
    <!--
     Characters we'll support.
           We could add control chars 0-31 and 127-159, but we won't. 
    -->
        <xsl:variable name="ascii"> !"K$%K'()*+,-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
        <xsl:variable name="latin1">¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ</xsl:variable>
        <!--  Characters that usually don't need to be escaped  -->
        <xsl:variable name="safe">!'()*-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>
        <xsl:variable name="hex">0123456789ABCDEF</xsl:variable>
        <xsl:if test="$str">
            <xsl:variable name="first-char" select="substring($str,1,1)"/>
            <xsl:choose>
                <xsl:when test="contains($safe,$first-char)">
                    <xsl:value-of select="$first-char"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="codepoint">
                        <xsl:choose>
                            <xsl:when test="contains($ascii,$first-char)">
                                <xsl:value-of select="string-length(substring-before($ascii,$first-char)) + 32"/>
                            </xsl:when>
                            <xsl:when test="contains($latin1,$first-char)">
                                <xsl:value-of select="string-length(substring-before($latin1,$first-char)) + 160"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:message terminate="no">
                                    Warning: string contains a character that is out of range! Substituting "?".
</xsl:message>
                                <xsl:text>63</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="hex-digit1" select="substring($hex,floor($codepoint div 16) + 1,1)"/>
                    <xsl:variable name="hex-digit2" select="substring($hex,$codepoint mod 16 + 1,1)"/>
                    <xsl:value-of select="concat('%',$hex-digit1,$hex-digit2)"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="string-length($str) > 1">
                <xsl:call-template name="url-encode">
                    <xsl:with-param name="str" select="substring($str,2)"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
    </xsl:template>



</xsl:stylesheet>
