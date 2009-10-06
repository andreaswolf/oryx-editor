<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xhtml="http://www.w3.org/1999/xhtml" 
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:events="http://www.w3.org/2001/xml-events"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" 
    xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
    xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/"
    xmlns:chiba="http://chiba.sourceforge.net/xforms"
    xmlns:xtee="http://x-tee.riik.ee/xsd/xtee.xsd">
<xsl:import href="wsdl2xforms.xsl"/>
<xsl:output method="xml" version="1.0" encoding="US-ASCII" indent="yes" /> 

<!-- Default name of the institution performing the query. -->
<xsl:param name="institution"></xsl:param>
<!-- Default ID code of the person performing the query. -->
<xsl:param name="idcode"></xsl:param>
<!-- Default post of the person performing the query. -->
<xsl:param name="post"></xsl:param>
<!-- Default query ID. -->
<xsl:param name="id"></xsl:param>
<!-- Default document number. -->
<xsl:param name="document"></xsl:param>

<!-- Web root of xteeportal. -->
<xsl:param name="webroot">{$contextroot}/forms/xteeportal/</xsl:param>
<!-- Prefix for classifiers. -->
<xsl:param name="classifier-prefix" select="concat($webroot, 'classifiers/')"/>
<!-- Suffix for classifiers. -->
<xsl:param name="classifier-suffix">.xml</xsl:param>
<!-- URL for submit, by default Chiba debugging JSP. -->
<xsl:param name="url">{$contextroot}/resources/jsp/debug-instance.jsp</xsl:param>

<!-- Debug flag. -->
<xsl:param name="debug" select="false()"/>
<xsl:param name="operation"/>

<xsl:template match="/">
  <xsl:apply-imports>
    <xsl:with-param name="operation" select="$operation" tunnel="yes"/>
  </xsl:apply-imports>
</xsl:template>

<xsl:template match="wsdl:port">
  <xsl:apply-imports>
    <xsl:with-param name="producer" select="xtee:address/@producer" tunnel="yes"/>
  </xsl:apply-imports>
</xsl:template>

<!-- Generate form for every operation in bindings section. -->
<!-- Start with bindings, because it contains version information. -->
<xsl:template match="wsdl:binding/wsdl:operation">
  <xsl:param name="producer" tunnel="yes"/>
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:if test="($operation = '' or $operation = @name) and @name != 'listMethods' and @name != 'loadClassificators' and @name != 'getCharge'">
    <xsl:variable name="operation" select="@name"/>
    <xsl:variable name="version" select="normalize-space(xtee:version)"/>
    <xsl:variable name="fullname" select="concat($producer, '.', $operation, '.', $version)"/>
    <xsl:variable name="formname" select="$fullname"/>
    <xsl:variable name="filename" select="concat($producer, '/', $operation, '.', $version, '.xhtml')"/>
    <xsl:apply-imports>
      <xsl:with-param name="filename" select="$filename" tunnel="yes" />
      <xsl:with-param name="operation" select="$operation" tunnel="yes" />
      <xsl:with-param name="version" select="$version" tunnel="yes" />
      <xsl:with-param name="fullname" select="$fullname" tunnel="yes" />
      <xsl:with-param name="formname" select="$formname" tunnel="yes" />
      <xsl:with-param name="requirecontent" select="xtee:requirecontent" tunnel="yes" />
      <xsl:with-param name="nocontent" select="xtee:nocontent" tunnel="yes" />
    </xsl:apply-imports>
  </xsl:if>
</xsl:template>

<!-- Just to make xtee namespace appear in root element. -->
<xsl:template match="wsdl:portType/wsdl:operation">
  <xsl:param name="producer" tunnel="yes"/>
  <xhtml:html>
    <xsl:namespace name="{$producer}" select="/wsdl:definitions/@targetNamespace" />
    <xsl:apply-templates select="." mode="html">
      <xsl:with-param name="tnsprefix" select="$producer" tunnel="yes"/>
    </xsl:apply-templates>
  </xhtml:html>
</xsl:template>

<!-- Titles and headings. -->

<xsl:template match="wsdl:operation/wsdl:documentation/xtee:title" mode="title">
  <xsl:if test="$debug"><xsl:message select="concat('title(xtee): ', name())"/></xsl:if>
  <xhtml:title><xsl:value-of select="normalize-space(.)"/></xhtml:title>
</xsl:template>

<xsl:template match="wsdl:operation/wsdl:documentation/xtee:title" mode="heading">
  <xsl:if test="$debug"><xsl:message select="concat('heading(xtee): ', name())"/></xsl:if>
  <xhtml:h1><xsl:value-of select="normalize-space(.)"/></xhtml:h1>
</xsl:template>

<xsl:template match="wsdl:operation/wsdl:documentation/xtee:notes" mode="heading">
  <xsl:if test="$debug"><xsl:message select="concat('heading(xtee): ', name())"/></xsl:if>
  <!--xhtml:p><xsl:value-of select="." disable-output-escaping="yes"/></xhtml:p-->
  <xhtml:p><xsl:value-of select="normalize-space(.)"/></xhtml:p>
</xsl:template>

<!-- Instance generation. -->

<xsl:template match="/" mode="instance">
  <xsl:param name="name"/>
  <xsl:param name="what"/>
  <xsl:param name="context"/>
  <xsl:if test="$debug"><xsl:message select="concat('instance(xtee): ', $name, ', ', $what, ', ', $context/name())"/></xsl:if>

  <!-- Resolve type name. -->
  <xsl:variable name="qname" select="resolve-QName($name, $context)"/>
  <xsl:variable name="localname" select="local-name-from-QName($qname)"/>
  <xsl:variable name="namespace" select="namespace-uri-from-QName($qname)"/>

  <xsl:choose>
    <!-- If type is ehak, then add two additional attributes. -->
    <xsl:when test="ends-with($what, 'Type') and $localname = 'ehak' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
      <xsl:attribute name="maakond" />
      <xsl:attribute name="vald" />
    </xsl:when>
    <!-- If type is ArrayOfStrings, then create empty array item. -->
    <xsl:when test="ends-with($what, 'Type') and $localname = 'ArrayOfString' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
      <!-- Chiba supports by default only XML Schema native types. -->
      <!--xsl:attribute name="type" select="'SOAP-ENC:Array'" namespace="http://www.w3.org/2001/XMLSchema-instance" />
      <xsl:attribute name="arrayType" select="'xsd:string[]'" namespace="http://www.w3.org/2001/XMLSchema-instance" /-->
      <!-- Add initial item to be used for repeat. -->
      <item xsi:type="xsd:string"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Resolve type/element/attribute. -->
      <xsl:apply-imports>
        <xsl:with-param name="name" select="$name"/>
        <xsl:with-param name="what" select="$what"/>
        <xsl:with-param name="context" select="$context"/>
      </xsl:apply-imports>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Do not attach type to elements with enumeration restriction. -->
<xsl:template match="xsd:restriction" mode="instance">
  <xsl:choose>
    <xsl:when test="xsd:enumeration"/>
    <xsl:otherwise>
      <xsl:apply-imports/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="createTextsInstance">
  <xforms:instance id="texts">
    <texts>
      <boolean value="true">jah</boolean>
      <boolean value="false">ei</boolean>
      <no_data>Andmeid ei tulnud.</no_data>
    </texts>
  </xforms:instance>
</xsl:template>

<xsl:template match="wsdl:operation" mode="instance">
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:apply-imports />
  <xsl:call-template name="createTextsInstance"/>
</xsl:template>

<!-- SOAP header. -->
<xsl:template match="wsdl:input" mode="instance-soap-header">
  <xsl:param name="producer" tunnel="yes"/>
  <xsl:param name="fullname" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('instance-soap-header(xtee): ', name(), ', ', @message)"/></xsl:if>
  <xtee:asutus><xsl:value-of select="$institution"/></xtee:asutus>
  <xtee:andmekogu><xsl:value-of select="$producer"/></xtee:andmekogu>
  <xtee:isikukood>EE<xsl:value-of select="$idcode"/></xtee:isikukood>
  <xtee:id><xsl:value-of select="$id"/></xtee:id>
  <xtee:nimi><xsl:value-of select="$fullname"/></xtee:nimi>
  <xtee:amet><xsl:value-of select="$post"/></xtee:amet>
  <xtee:toimik><xsl:value-of select="$document"/></xtee:toimik>
  <xtee:ametnik><xsl:value-of select="$idcode"/></xtee:ametnik>
  <xsl:apply-imports />
</xsl:template>

<xsl:template match="wsdl:input" mode="instance schema bind form">
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('#all(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:apply-imports>
    <xsl:with-param name="message" select="$operation" tunnel="yes"/>
  </xsl:apply-imports>
</xsl:template>

<xsl:template match="wsdl:output" mode="instance schema bind form">
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('#all(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:apply-imports>
    <xsl:with-param name="message" select="concat($operation, 'Response')" tunnel="yes"/>
  </xsl:apply-imports>
</xsl:template>

<!-- Only generate "keha" parts. -->
<xsl:template match="wsdl:part" mode="#all">
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('#all(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:if test="@name = 'keha'">
    <xsl:apply-imports/>
  </xsl:if>
</xsl:template>

<!-- Lookup instance generation. -->

<xsl:template match="/" mode="lookup">
  <xsl:param name="name"/>
  <xsl:param name="what"/>
  <xsl:param name="context"/>
  <xsl:if test="$debug"><xsl:message select="concat('lookup: ', $name, ', ', $what)"/></xsl:if>

  <!-- Resolve type name. -->
  <xsl:variable name="qname" select="resolve-QName($name, $context)"/>
  <xsl:variable name="localname" select="local-name-from-QName($qname)"/>
  <xsl:variable name="namespace" select="namespace-uri-from-QName($qname)"/>

  <!-- Generate lookup instance for EHAK components. -->
  <xsl:if test="$namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
    <xsl:if test="$localname = ('maakond', 'vald', 'asula', 'ehak')">
      <xforms:instance id="ehak" src="{$classifier-prefix}ehak{$classifier-suffix}"/>
    </xsl:if>
  </xsl:if>

  <xsl:apply-imports>
    <xsl:with-param name="name" select="$name"/>
    <xsl:with-param name="what" select="$what"/>
    <xsl:with-param name="context" select="$context"/>
  </xsl:apply-imports>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo/xtee:lookup" mode="lookup">
  <xforms:instance id="{.}" src="{$classifier-prefix}{.}{$classifier-suffix}"/>
</xsl:template>

<!-- Lookup labels. -->

<xsl:template match="xsd:enumeration" mode="lookup-label">
  <xsl:if test="$debug"><xsl:message select="concat('lookup-label(xtee): ', name())"/></xsl:if>
  <xsl:choose>
    <xsl:when test="xsd:annotation/xsd:appinfo/xtee:title">
      <xsl:value-of select="xsd:annotation/xsd:appinfo/xtee:title" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-imports />
    </xsl:otherwise>
  </xsl:choose>  
</xsl:template>

<!-- Bindings -->

<xsl:template match="/" mode="bind">
  <xsl:param name="name"/>
  <xsl:param name="what"/>
  <xsl:param name="context"/>
  <xsl:if test="$debug"><xsl:message select="concat('bind(xtee): ', $name, ', ', $what)"/></xsl:if>

  <!-- Resolve type name. -->
  <xsl:variable name="qname" select="resolve-QName($name, $context)"/>
  <xsl:variable name="localname" select="local-name-from-QName($qname)"/>
  <xsl:variable name="namespace" select="namespace-uri-from-QName($qname)"/>

  <xsl:choose>
    <!-- If type is xtee:ehak, then make additional attributes non-relevant. -->
    <xsl:when test="ends-with($what, 'Type') and $localname = 'ehak' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
      <xforms:bind nodeset="@maakond" relevant="boolean-from-string(instance('temp')/relevant)" />
      <xforms:bind nodeset="@vald" relevant="boolean-from-string(instance('temp')/relevant)" />
    </xsl:when>
    <!-- If type is xtee:url, then add type xsd:anyURI, to make it link in output. -->
    <xsl:when test="ends-with($what, 'Type') and $localname = 'url' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
      <xsl:attribute name="type" select="'xsd:anyURI'"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Resolve type/element/attribute. -->
      <xsl:apply-imports>
        <xsl:with-param name="name" select="$name"/>
        <xsl:with-param name="what" select="$what"/>
        <xsl:with-param name="context" select="$context"/>
      </xsl:apply-imports>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="xsd:element | xsd:attribute" mode="required">
  <xsl:param name="requirecontent" tunnel="yes"/>
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('required(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:choose>
    <!-- Use schema MIPS on input forms. -->
    <xsl:when test="$formtype = 'input'">
      <xsl:if test="$requirecontent = 'true' and (exists(@minOccurs) = false() or @minOccurs &gt; 0 or @use='required')">true()</xsl:if>
      <!--xsl:apply-imports /-->
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-imports />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="xsd:element | xsd:attribute | xsd:choice" mode="relevant">
  <xsl:param name="nocontent" tunnel="yes"/>
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('relevant(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:choose>
    <xsl:when test="$formtype = 'input'">
      <xsl:apply-imports/>
    </xsl:when>
    <xsl:when test="$formtype = 'output'">
      <!-- Suppress empty fields if xtee:nocontent = null. -->
      <xsl:if test="$nocontent = 'null'">. != ''</xsl:if>
      <!-- Suppress normal relevance on output forms. -->
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="xsd:element | xsd:attribute" mode="constraint">
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('constraint(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:choose>
    <xsl:when test="$formtype = 'input'">
      <xsl:apply-imports/>
    </xsl:when>
    <xsl:when test="$formtype = 'output'">
      <!-- Suppress normal constraints on output forms. -->
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="xsd:element" mode="nillable">
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('nillable(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:choose>
    <!-- Hide xsi:nil when submitting input forms and not nillable. -->
    <xsl:when test="$formtype = 'input'">
      <xsl:apply-imports/>
    </xsl:when>
    <!-- Nillable binding is not needed on output forms. -->
    <xsl:when test="$formtype = 'output'"/>
  </xsl:choose>
</xsl:template>

<!-- Submission -->

<xsl:template match="wsdl:operation" mode="submission">
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('submission: ', name(), ', ', @name)"/></xsl:if>
  <xsl:apply-imports>
    <!-- Use global parameter as $url, instead of one from port. -->
    <xsl:with-param name="url" select="$url" tunnel="yes"/>
  </xsl:apply-imports>
</xsl:template>

<!-- Set id to random number. Doesn't work yet. Will be done on server side. -->
<!--xsl:template match="wsdl:input" mode="submission-actions-submit">
  <xsl:with-param name="formname" tunnel="yes"/>
  <xsl:apply-imports/>
  <xforms:setvalue ref="instance('{$formname}.input')/SOAP-ENV:Header/xtee:id" value="digest(random(), 'MD5', 'hex')"/>
</xsl:template-->

<!-- Submit error message. -->
<xsl:template match="wsdl:operation" mode="submission-actions-submit-error-message-label">Veendu, et kõik väljad on korrektselt täidetud!</xsl:template>

<!-- Forms -->

<xsl:template match="xsd:simpleType | xsd:complexType" mode="form">
  <xsl:param name="node" tunnel="yes"/>
  <!-- If element doesn't have label, then take label from type. -->
  <xsl:apply-imports>
    <xsl:with-param name="node" select="if ($node/xsd:annotation/xsd:appinfo) then $node else ." tunnel="yes"/>
  </xsl:apply-imports>
</xsl:template>

<xsl:template match="/" mode="form">
  <xsl:param name="name"/>
  <xsl:param name="what"/>
  <xsl:param name="context"/>
  <xsl:param name="node" tunnel="yes"/>
  <xsl:param name="ref" tunnel="yes"/>
  <xsl:param name="parentref" tunnel="yes"/>
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('form(xtee): ', $name, ', ', $what)"/></xsl:if>

  <xsl:variable name="qname" select="resolve-QName($name, $context)"/>
  <xsl:variable name="localname" select="local-name-from-QName($qname)"/>
  <xsl:variable name="namespace" select="namespace-uri-from-QName($qname)"/>

  <xsl:choose>
    <xsl:when test="ends-with($what, 'Type')">
      <xsl:choose>
        <xsl:when test="$formtype = 'input'">
          <xsl:choose>
            <!-- Ignore ArrayOfString in input. -->
            <xsl:when test="$namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd' and $localname = 'ArrayOfString'">
            </xsl:when>
            <xsl:when test="$localname = 'maakond' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
              <xforms:select1 ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
                <xforms:itemset nodeset="instance('ehak')/maakond">
                  <xforms:label ref="@nimi"/>
                  <xforms:value ref="@kood"/>
                </xforms:itemset>
              </xforms:select1>
            </xsl:when>
            <xsl:when test="$localname = 'vald' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
              <xforms:select1 ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
                <xsl:variable name="dependref" select="$node/xsd:annotation/xsd:appinfo/xtee:ref"/>
                <xforms:itemset nodeset="instance('ehak')/maakond{if ($dependref) then concat('[@kood=', $parentref, '/', $dependref, ']') else ''}/vald">
                  <xforms:label ref="@nimi"/>
                  <xforms:value ref="@kood"/>
                </xforms:itemset>
              </xforms:select1>
            </xsl:when>
            <xsl:when test="$localname = 'asula' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
              <xforms:select1 ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
                <xsl:variable name="dependref" select="$node/xsd:annotation/xsd:appinfo/xtee:ref"/>
                <xforms:itemset nodeset="instance('ehak')/maakond/vald{if ($dependref) then concat('[@kood=', $parentref, '/', $dependref, ']') else ''}/asula">
                  <xforms:label ref="@nimi"/>
                  <xforms:value ref="@kood"/>
                </xforms:itemset>
              </xforms:select1>
            </xsl:when>
            <xsl:when test="$localname = 'ehak' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
              <xforms:group>
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
                <!-- In case of EHAK add additional attributes maakond and vald will be added to the element. -->
                <!-- Those attributes are suppressed by non-relevant binding. ref must be an element. -->
                <xforms:select1 ref="{$ref}/@maakond">
                  <xforms:label>Maakond</xforms:label>
                  <xforms:itemset nodeset="instance('ehak')/maakond">
                    <xforms:label ref="@nimi"/>
                    <xforms:value ref="@kood"/>
                  </xforms:itemset>
                </xforms:select1>
                <xforms:select1 ref="{$ref}/@vald">
                  <xforms:label>Vald</xforms:label>
                  <xforms:itemset nodeset="instance('ehak')/maakond[@kood={$parentref}/{$ref}/@maakond]/vald">
                    <xforms:label ref="@nimi"/>
                    <xforms:value ref="@kood"/>
                  </xforms:itemset>
                </xforms:select1>
                <xforms:select1 ref="{$ref}">
                  <xforms:label>Asula</xforms:label>
                  <xforms:itemset nodeset="instance('ehak')/maakond[@kood={$parentref}/{$ref}/@maakond]/vald[@kood={$parentref}/{$ref}/@vald]/asula">
                    <xforms:label ref="@nimi"/>
                    <xforms:value ref="@kood"/>
                  </xforms:itemset>
                </xforms:select1>
              </xforms:group>
            </xsl:when>
            <!-- If there is reference to lookup values, then select1 control. -->
            <xsl:when test="$node/xsd:annotation/xsd:appinfo/xtee:lookup">
              <xforms:select1 ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
                <xsl:variable name="lookup" select="$node/xsd:annotation/xsd:appinfo/xtee:lookup"/>
                <xsl:variable name="nodeset" select="$node/xsd:annotation/xsd:appinfo/xtee:lookup/@nodeset"/>
                <xsl:variable name="labelref" select="$node/xsd:annotation/xsd:appinfo/xtee:lookup/@labelref"/>
                <xsl:variable name="valueref" select="$node/xsd:annotation/xsd:appinfo/xtee:lookup/@valueref"/>
                <xforms:itemset nodeset="instance('{$lookup}')/{if ($nodeset) then $nodeset else 'item'}">
                  <xforms:label ref="{if ($labelref) then $labelref else 'label'}"/>
                  <xforms:value ref="{if ($valueref) then $valueref else 'value'}"/>
                </xforms:itemset>
              </xforms:select1>
            </xsl:when>
            <!-- If xtee:fieldtype = 'textarea' then generate textarea control. -->
            <xsl:when test="$node/xsd:annotation/xsd:appinfo/xtee:fieldtype = 'textarea'">
              <xforms:textarea ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:textarea>
            </xsl:when>
            <!-- If xtee:fieldtype = 'comment' then generate output control. -->
            <xsl:when test="$node/xsd:annotation/xsd:appinfo/xtee:fieldtype = 'comment'">
              <xforms:output ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:output>
            </xsl:when>
            <!-- If is image type, then generate upload control with preview. -->
            <xsl:when test="$namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd' and ($localname = ('jpg', 'gif', 'png'))">
              <xforms:upload ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:upload>
              <!-- Preview image. -->
              <xforms:output ref="{$ref}" mediatype="image/*"/>
            </xsl:when>
            <!-- If file type, then generate upload control. -->
            <xsl:when test="$namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd' and ($localname = ('xml', 'txt', 'csv'))">
              <xforms:upload ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:upload>
            </xsl:when>
            <!-- If url type, then generate input control. -->
            <xsl:when test="$namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd' and $localname = 'url'">
              <xforms:input ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:input>
            </xsl:when>
            <!-- Otherwise let schema2xforms handle it. -->
            <xsl:otherwise>
              <xsl:apply-imports>
                <xsl:with-param name="name" select="$name"/>
                <xsl:with-param name="what" select="$what"/>
                <xsl:with-param name="context" select="$context"/>
              </xsl:apply-imports>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="$formtype = 'output'">
          <xsl:choose>
            <!-- Do not show ID fields on output forms. -->
            <xsl:when test="$namespace = 'http://www.w3.org/2001/XMLSchema' and $localname = 'ID'"/>
            <!-- If is boolean type, then display jah/ei, instead of Chiba's default - checkbox. -->
            <xsl:when test="$namespace = 'http://www.w3.org/2001/XMLSchema' and $localname = 'boolean'">
              <xforms:output value="instance('texts')/boolean[@value = current()/{$ref}]">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:output>
            </xsl:when>
            <xsl:when test="$localname = 'maakond' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
              <xforms:output value="instance('ehak')/maakond[@kood = current()/{$ref}]/@nimi">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:output>
            </xsl:when>
            <xsl:when test="$localname = 'vald' and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
              <xsl:variable name="dependref" select="$node/xsd:annotation/xsd:appinfo/xtee:ref"/>
              <!-- Filter on maakond first, this makes a lot faster. -->
              <xforms:output value="instance('ehak')/maakond{if ($dependref) then concat('[@kood=', $parentref, '/', $dependref, ']') else ''}/vald[@kood = current()/{$ref}]/@nimi">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:output>
            </xsl:when>
            <xsl:when test="$localname = ('asula', 'ehak') and $namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd'">
              <xsl:variable name="dependref" select="$node/xsd:annotation/xsd:appinfo/xtee:ref"/>
              <!-- Filter on vald first, this makes a lot faster. -->
              <xforms:output value="instance('ehak')/maakond/vald{if ($dependref) then concat('[@kood=', $parentref, '/', $dependref, ']') else ''}/asula[@kood = current()/{$ref}]/@nimi">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:output>
            </xsl:when>
            <!-- If is image type, then generate image output. -->
            <xsl:when test="$namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd' and ($localname = ('jpg', 'gif', 'png'))">
              <xforms:output ref="{$ref}" mediatype="image/*">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
              </xforms:output>
            </xsl:when>
            <!-- If is file or URL type, then generate link. -->
            <xsl:when test="$namespace = 'http://x-tee.riik.ee/xsd/xtee.xsd' and ($localname = ('xml', 'txt', 'csv', 'url'))">
              <xforms:trigger ref="{$ref}">
                <!-- Generate labels and hints. -->
                <xsl:apply-templates select="$node" mode="label"/>
                <xforms:load events:event="DOMActivate" ref="." show="new"/>
              </xforms:trigger>
            </xsl:when>
            <!-- Otherwise let schema2xforms handle it. -->
            <xsl:otherwise>
              <xsl:apply-imports>
                <xsl:with-param name="name" select="$name"/>
                <xsl:with-param name="what" select="$what"/>
                <xsl:with-param name="context" select="$context"/>
              </xsl:apply-imports>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
    </xsl:when>
    <!-- Resolve element/attribute/type. -->
    <xsl:otherwise>
      <xsl:apply-imports>
        <xsl:with-param name="name" select="$name"/>
        <xsl:with-param name="what" select="$what"/>
        <xsl:with-param name="context" select="$context"/>
      </xsl:apply-imports>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="xsd:element" mode="form">
  <xsl:if test="$debug"><xsl:message select="concat('form(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:choose>
    <!-- Ignore nonvisible controls on both input and output forms. -->
    <xsl:when test="xsd:annotation/xsd:appinfo/xtee:visibility = '0'">
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-imports/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="wsdl:message" mode="form">
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('form(xtee): ', name(), ', ', @name)"/></xsl:if>
  <xsl:choose>
    <xsl:when test="$formtype = 'input'">
      <xsl:variable name="qname" select="resolve-QName(wsdl:part[@name = 'keha']/@type, .)"/>
      <xsl:variable name="localname" select="local-name-from-QName($qname)"/>
      <xsl:variable name="namespace" select="namespace-uri-from-QName($qname)"/>
      <xsl:choose>
        <!-- Query will be submitted immediately, if keha type is string or legacy query. -->
        <xsl:when test="$formtype = 'input' and (($namespace = 'http://www.w3.org/2001/XMLSchema' and $localname = 'string') or starts-with($operation, 'legacy'))">
          <xforms:send submission="{$formname}.submission" events:event="xforms-select"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-imports/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="$formtype = 'output'">
      <!-- Show query id from the header on output form. -->
      <xforms:group ref="instance('{$formname}.output')/SOAP-ENV:Header">
        <xforms:output ref="xtee:id" class="serviceid">
          <xforms:label>Päringu id</xforms:label>
        </xforms:output>
      </xforms:group>
      <xsl:apply-imports/>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<!-- Submit label. -->

<xsl:template match="wsdl:input" mode="form-submit">
  <xsl:if test="$debug"><xsl:message select="concat('form-submit(xtee): ', name(), ', ', @message)"/></xsl:if>
  <xforms:label>
    <xsl:choose>
      <xsl:when test="../wsdl:documentation/xtee:actiontitle != ''">
        <xsl:value-of select="../wsdl:documentation/xtee:actiontitle"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'Esita päring'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xforms:label>
</xsl:template>

<!-- Back button label. -->
<xsl:template match="wsdl:output" mode="form-again">
  <xsl:if test="$debug"><xsl:message select="concat('form-again(xtee): ', name(), ', ', @message)"/></xsl:if>
  <xforms:label>Uuesti</xforms:label>
</xsl:template>

<!-- If no data returned, show message. -->
<xsl:template match="wsdl:output" mode="form-fault">
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:param name="tnsprefix" tunnel="yes"/>
  <xforms:group ref="instance('{$formname}.output')/SOAP-ENV:Body/{$tnsprefix}:{$operation}Response[keha = '']" class="info">
    <xforms:output ref="instance('texts')/no_data"/>
  </xforms:group>
  <xforms:group ref="instance('{$formname}.output')/SOAP-ENV:Body/{$tnsprefix}:{$operation}Response/keha/faultString" class="info">
    <xforms:output ref="."/>
  </xforms:group>
  <xsl:apply-imports />
</xsl:template>

<!-- Labels and hints. -->

<xsl:template match="xsd:element | xsd:attribute | xsd:group | xsd:complexType | xsd:simpleType | xsd:enumeration" mode="label title heading label-only">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name(), ', ', @name, ', ', @value)"/></xsl:if>
  <xsl:apply-templates select="xsd:annotation" mode="appearance"/>
  <!-- Don't use annotation/documentation for label, only anything that is below annotation/appinfo. -->
  <xsl:apply-templates select="xsd:annotation/xsd:appinfo" mode="#current"/>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo/xtee:title" mode="label label-only">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name())"/></xsl:if>
  <xforms:label><xsl:value-of select="." /></xforms:label>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo/xtee:notes" mode="label">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name())"/></xsl:if>
  <xforms:help><xsl:value-of select="." /></xforms:help>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo/xtee:wildcard" mode="label">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name())"/></xsl:if>
  <xforms:hint>Saab kasutada metamärke: <xsl:value-of select="." /></xforms:hint>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo/xtee:appearance" mode="label">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name())"/></xsl:if>
  <xsl:attribute name="appearance"><xsl:value-of select="." /></xsl:attribute>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo/xtee:inputmode" mode="label">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name())"/></xsl:if>
  <xsl:attribute name="inputmode"><xsl:value-of select="." /></xsl:attribute>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo/xtee:selection" mode="label">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name())"/></xsl:if>
  <xsl:attribute name="selection"><xsl:value-of select="." /></xsl:attribute>
</xsl:template>

<xsl:template match="xsd:annotation/xsd:appinfo" mode="label">
  <xsl:if test="$debug"><xsl:message select="concat('label(xtee): ', name())"/></xsl:if>
  <xsl:variable name="style">
    <xsl:apply-templates mode="style"/>
  </xsl:variable>
  <xsl:if test="$style != ''">
    <xsl:attribute name="style" select="$style"/>
  </xsl:if>
  <xsl:apply-imports/>
</xsl:template>

<xsl:template match="xtee:fieldsize" mode="style">width: <xsl:value-of select="if (. &gt; 10) then . div 2 else ."/>em;</xsl:template>
<xsl:template match="xtee:fieldcols" mode="style">width: <xsl:value-of select="if (. &gt; 10) then . div 2 else ."/>em;</xsl:template>
<xsl:template match="xtee:fieldrows" mode="style">height: <xsl:value-of select="."/>em;</xsl:template>

</xsl:stylesheet>
