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
    xmlns:xtee="http://x-tee.riik.ee/xsd/xtee.xsd"
    xmlns:tm="tambet.matiisen@gmail.com">
<xsl:import href="xtee2xforms.xsl"/>
<xsl:output method="xml" version="1.0" encoding="US-ASCII" indent="yes" /> 

<!-- Directory, where WSDL files are. -->
<xsl:param name="wsdl-prefix"></xsl:param>
<!-- Extension of WSDL files. -->
<xsl:param name="wsdl-suffix">.wsdl</xsl:param>

<xsl:template match="wsdl:binding/wsdl:operation">
  <xsl:apply-imports>
    <xsl:with-param name="complex" select="xtee:complex" tunnel="yes"/>
  </xsl:apply-imports>
</xsl:template>

<!-- Only process wsdl:operation under wsdl:portType when not complex. -->
<xsl:template match="wsdl:portType/wsdl:operation">
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:choose>
    <xsl:when test="$complex"/>
    <xsl:otherwise>
      <xsl:apply-imports/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Ignore wsdl:input and wsdl:output when there is no message reference. -->
<xsl:template match="wsdl:input[@message = ''] | wsdl:output[@message = '']" mode="#all"/>

<!-- Disable parsing of wsdl:input and wsdl:output below binding element. -->
<xsl:template match="wsdl:binding/wsdl:operation/wsdl:input | wsdl:binding/wsdl:operation/wsdl:output" mode="model form"/>

<!-- Disable generation of xforms-ready event for every subform. -->
<xsl:template match="wsdl:portType/wsdl:operation" mode="activate">
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:if test="not($complex)">
    <xsl:apply-imports/>
  </xsl:if>
</xsl:template>

<xsl:template match="xtee:complex">
  <xhtml:html>
    <xsl:apply-templates mode="namespace"/>
    <xhtml:head>
      <xsl:apply-templates select=".." mode="title"/>
      <xforms:model>
        <xsl:apply-templates mode="model"/>
        <xsl:call-template name="createTempInstance"/>
        <xsl:call-template name="createTextsInstance"/>
        <!-- Toggle subform 'this' to be active. It contains event that transfers to first actual form and sets substitutions. -->
        <xforms:toggle case="this.request" events:event="xforms-ready"/>
      </xforms:model>
    </xhtml:head>
    <xhtml:body>
      <xsl:apply-templates select=".." mode="heading"/>
      <xforms:switch>
        <xsl:apply-templates mode="form"/>
      </xforms:switch>
    </xhtml:body>
  </xhtml:html>
</xsl:template>

<!-- Do not create temp and texts instances, they will be generated once for model. -->
<xsl:template match="wsdl:portType/wsdl:operation" mode="instance form">
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:choose>
    <xsl:when test="$complex">
      <xsl:apply-templates mode="#current"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-imports/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="xtee:suboperation[@type='locator']" mode="namespace">
  <xsl:variable name="producer" select="substring-before(@href, '.')"/>
  <!-- TODO: Should use target namespace from WSDL, but this is easier and quicker. -->
  <xsl:namespace name="{$producer}" select="concat('http://producers.', $producer, '.xtee.riik.ee/producer/', $producer)"/>
</xsl:template>

<xsl:template match="xtee:suboperation[@type='locator']" mode="model form">
  <xsl:if test="$debug"><xsl:message select="concat('model/form: ', name(), ', ', @label)"/></xsl:if>
  <xsl:variable name="producer" select="substring-before(@href, '.')"/>
  <xsl:variable name="rest" select="substring-after(@href, '.')"/>
  <xsl:variable name="operation" select="substring-before($rest, '.')"/>
  <xsl:variable name="version" select="substring-after($rest, '.')"/>
  <!-- TODO: Use correct binding from suboperations' WSDL. -->
  <xsl:apply-templates select="document(concat($wsdl-prefix, $producer, $wsdl-suffix))/wsdl:definitions/wsdl:binding[1]/wsdl:operation[@name=$operation][1]" mode="#current">
    <xsl:with-param name="producer" select="$producer" tunnel="yes" />
    <xsl:with-param name="operation" select="$operation" tunnel="yes" />
    <xsl:with-param name="version" select="$version" tunnel="yes" />
    <xsl:with-param name="fullname" select="@href" tunnel="yes" />
    <xsl:with-param name="formname" select="@label" tunnel="yes" />
    <xsl:with-param name="actuate" select="@actuate" tunnel="yes" />
    <xsl:with-param name="tnsprefix" select="$producer" tunnel="yes" />
  </xsl:apply-templates>
</xsl:template>

<!-- Dummy case for 'this'. -->
<xsl:template match="xtee:suboperation[@label = 'this' and @type = 'resource']" mode="form">
  <xsl:param name="complex" tunnel="yes"/>
  <xforms:case id="{@label}.request">
    <!-- Generate automatic transition to first form, with substitutions when = 'this' and everything. -->
    <xsl:apply-templates select="$complex/xtee:arc[@from = current()/@label and @actuate = 'onLoad']" mode="link"/>
  </xforms:case>
</xsl:template>

<!-- HACK: To get requirecontent, nocontent and portType parameters tunneled. This should be in unified in xtee2xforms. -->
<xsl:template match="wsdl:binding/wsdl:operation" mode="model form">
  <xsl:variable name="qname" select="resolve-QName(../@type, ..)"/>
  <xsl:variable name="portType" select="local-name-from-QName($qname)"/>
  <xsl:apply-imports>
    <xsl:with-param name="requirecontent" select="xtee:requirecontent" tunnel="yes" />
    <xsl:with-param name="nocontent" select="xtee:nocontent" tunnel="yes" />
    <xsl:with-param name="portType" select="$portType" tunnel="yes" />
  </xsl:apply-imports>
</xsl:template>

<xsl:template match="wsdl:message" mode="form">
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:param name="actuate" tunnel="yes"/>
  <xsl:if test="$debug"><xsl:message select="concat('form(complex): ', name(), ', ', @name)"/></xsl:if>
  <xsl:choose>
    <xsl:when test="$formtype = 'input'">
      <xsl:choose>
        <xsl:when test="$actuate = 'onLoad'">
          <!-- Do submit when input form is activated. -->
          <xforms:send submission="{$formname}.submission" events:event="xforms-select"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-imports/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="$formtype = 'output'">
      <xsl:apply-imports/>
      <!-- There could be several links which need to be activated after submit, depending on XPath condition. -->
      <xsl:apply-templates select="$complex/xtee:arc[@from = $formname and @actuate = 'onLoad']" mode="link"/>
      <!-- Add buttons to navigate to other forms. -->
      <xsl:apply-templates select="$complex/xtee:arc[@from = $formname and @actuate = 'onRequest' and not(exists(@arcrole))]" mode="link"/>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:function name="tm:extract-label">
  <xsl:param name="link" />
  <xsl:if test="$debug"><xsl:message select="concat('extract-label: ', $link)"/></xsl:if>
  <xsl:value-of select="substring-before($link, '#xpointer(')"/>
</xsl:function>

<xsl:function name="tm:extract-producer">
  <xsl:param name="fullname" />
  <xsl:if test="$debug"><xsl:message select="concat('extract-producer: ', $fullname)"/></xsl:if>
  <xsl:value-of select="substring-before($fullname, '.')"/>
</xsl:function>

<xsl:function name="tm:extract-operation">
  <xsl:param name="fullname" />
  <xsl:if test="$debug"><xsl:message select="concat('extract-operation: ', $fullname)"/></xsl:if>
  <xsl:value-of select="substring-before(substring-after($fullname, '.'), '.')"/>
</xsl:function>

<!-- Extracts path from xlink reference. -->
<xsl:function name="tm:extract-path">
  <xsl:param name="link" />
  <xsl:if test="$debug"><xsl:message select="concat('extract-path: ', $link)"/></xsl:if>
  <xsl:value-of select="replace($link, '[^#]*#xpointer\(([^\[]*)(\[.*\].*)?\)', '$1')"/>
  <!--xsl:message select="concat('link: ', $link, ', path: ', replace($link, '[^#]*#xpointer\(([^\[]*)(\[.*\])?\)', '$1'))"/-->
</xsl:function>

<!-- Extracts condition from xlink reference. -->
<xsl:function name="tm:extract-condition">
  <xsl:param name="link" />
  <xsl:if test="$debug"><xsl:message select="concat('extract-condition: ', $link)"/></xsl:if>
  <xsl:value-of select="replace($link, '[^#]*#xpointer\(([^\[]*)(\[.*\].*)?\)', '$2')"/>
  <!--xsl:message select="concat('link: ', $link, ', condition: ', replace($link, '[^#]*#xpointer\(([^\[]*)(\[.*\])?\)', '$2'))"/-->
</xsl:function>

<xsl:template match="xsd:element | wsdl:part[@name = 'keha']" mode="form">
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:param name="parentref" tunnel="yes"/>
  <xsl:param name="ref" tunnel="yes"/>
  <xsl:param name="element" tunnel="yes"/>
  <xsl:param name="root" tunnel="yes"/>
  <xsl:param name="prefix" tunnel="yes"/>
  <xsl:param name="formtype" tunnel="yes"/>
  <xsl:param name="actuate" tunnel="yes"/>
  <xsl:param name="tnsprefix" tunnel="yes"/>
  <xsl:choose>
    <!-- Do not generate input form, when it will be submitted automatically. -->
    <xsl:when test="$formtype = 'input' and $actuate = 'onLoad'"/>
    <!-- When part of complex form, then check if need to include link. -->
    <xsl:when test="$complex != ''">
      <!-- TODO: Duplicated code from schema2xforms.xsl. -->
      <!-- Absolute reference to parent element. May be empty. -->
      <xsl:variable name="parentref" select="if ($ref = '.') then $parentref else if ($parentref = '') then $ref else concat($parentref, '/', $ref)"/>
      <!-- Relative reference to this element. May be absolute, when root is not empty. -->
      <xsl:variable name="ref" select="concat($root, $prefix, normalize-space(if ($element) then $element else @name))"/>
      <!-- Absolute reference to this element. -->
      <xsl:variable name="fullref" select="concat($parentref, '/', $ref)"/>
      <!-- Arcrole references start from this prefix. -->
      <xsl:variable name="refprefix" select="concat($tnsprefix, ':', $operation, if ($formtype = 'output') then 'Response' else '')"/>
      <!-- Everything after the prefix. -->
      <xsl:variable name="shortref" select="substring-after($fullref, $refprefix)"/>
      <!-- If arcrole starts with /paring, it refers to input form instead of output form. -->
      <xsl:variable name="currentref" select="if ($formtype = 'input') then replace($shortref, '^/keha', '/paring') else $shortref"/>
      <!--xsl:message select="concat('parentref: ', $parentref, ', ref: ', $ref)"/>
      <xsl:message select="concat('fullref: ', $fullref, ', refprefix: ', $refprefix)"/>
      <xsl:message select="concat('shortref: ', $shortref, ', currentref: ', $currentref)"/-->
      <!-- Apply templates to all arcs with link in arcrole equal to current path. -->
      <!-- Arcs with @title = '_DATA' first. -->
      <xsl:variable name="control">
        <xsl:apply-templates select="$complex/xtee:arc[@from = $formname and $currentref = tm:extract-path(@arcrole) and @title = '_DATA']" mode="link">
          <xsl:with-param name="ref" select="$ref" tunnel="yes"/>
          <xsl:with-param name="node" select="." tunnel="yes"/>
        </xsl:apply-templates>
      </xsl:variable>
      <!-- Arcs with @title = '_DATA' replace control itself. -->
      <xsl:choose>
        <xsl:when test="$control != ''">
          <xsl:copy-of select="$control"/>
          <xsl:apply-templates select="$complex/xtee:arc[@from = $formname and $currentref = tm:extract-path(@arcrole) and @title != '_DATA' and @actuate = 'onRequest']" mode="link">
            <xsl:with-param name="ref" select="$ref" tunnel="yes"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <!-- Generate control as usual. -->
          <xsl:variable name="control">
            <xsl:apply-imports/>
          </xsl:variable>
          <!-- If control is repeat or group, then put link inside. -->
          <xsl:choose>
            <xsl:when test="$control/xforms:repeat">
              <xforms:repeat>
                <xsl:copy-of select="$control/xforms:repeat/@*"/>
                <xsl:copy-of select="$control/xforms:repeat/xforms:label"/>
                <!-- Use . as ref. -->
                <xsl:apply-templates select="$complex/xtee:arc[@from = $formname and $currentref = tm:extract-path(@arcrole) and @title != '_DATA' and @actuate = 'onRequest']" mode="link">
                  <xsl:with-param name="ref" select="'.'" tunnel="yes"/>
                </xsl:apply-templates>
                <xsl:copy-of select="$control/xforms:repeat/* except $control/xforms:repeat/xforms:label"/>
              </xforms:repeat>
            </xsl:when>
            <xsl:when test="$control/xforms:group">
              <xforms:group>
                <xsl:copy-of select="$control/xforms:group/@*"/>
                <xsl:copy-of select="$control/xforms:group/xforms:label"/>
                <!-- Use . as ref. -->
                <xsl:apply-templates select="$complex/xtee:arc[@from = $formname and $currentref = tm:extract-path(@arcrole) and @title != '_DATA' and @actuate = 'onRequest']" mode="link">
                  <xsl:with-param name="ref" select="'.'" tunnel="yes"/>
                </xsl:apply-templates>
                <xsl:copy-of select="$control/xforms:group/* except $control/xforms:group/xforms:label"/>
              </xforms:group>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="$control"/>
              <xsl:apply-templates select="$complex/xtee:arc[@from = $formname and $currentref = tm:extract-path(@arcrole) and @title != '_DATA' and @actuate = 'onRequest']" mode="link">
                <xsl:with-param name="ref" select="$ref" tunnel="yes"/>
              </xsl:apply-templates>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-imports/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="xtee:arc[@actuate = 'onRequest']" mode="link">
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:param name="ref" tunnel="yes"/>
  <xsl:param name="node" tunnel="yes"/>
  <!--xsl:message select="concat('arcrole: ', @arcrole)"/-->
  <!-- Must use relative ref instead of full ref from arcrole, 
       because otherwise it wouldn't work with repeating elements. 
       Add condition from arcrole manually. -->
  <xforms:group>
    <!-- Make group visible only when element referenced by arcrole exists. -->
    <xsl:if test="@arcrole">
      <xsl:attribute name="ref" select="concat($ref, tm:extract-condition(@arcrole))"/>
    </xsl:if>
    <!-- In case of @title = '_DATA' add label to group, because link title is taken from data. -->
    <!-- When repeat with appearance=compact is used, this way the column gets normal heading. -->
    <xsl:if test="upper-case(@title) = '_DATA'">
      <xforms:label><xsl:value-of select="$node/xsd:annotation/xsd:appinfo/xtee:title"/></xforms:label>
    </xsl:if>
    <xsl:variable name="title" select="$complex/xtee:suboperation[@label = current()/@to]/@title"/>
    <xforms:trigger appearance="minimal">
      <xsl:choose>
        <!-- In case of @title = '_DATA' actual data is used as link title. -->
        <xsl:when test="upper-case(@title) = '_DATA'">
          <xforms:label ref="."/>
        </xsl:when>
        <!-- If referenced suboperation has title, then use it. -->
        <xsl:when test="$title != ''">
          <xforms:label><xsl:value-of select="$title"/></xforms:label>
        </xsl:when>
        <!-- If arc has title, then use it. -->
        <xsl:when test="@title != ''">
          <xforms:label><xsl:value-of select="@title"/></xforms:label>
        </xsl:when>
        <!-- Otherwise fixed title. -->
        <xsl:otherwise>
          <xforms:label>Edasi</xforms:label>
        </xsl:otherwise>
      </xsl:choose>
      <!-- Put arc title into group, because it must not be shown, when binding expression does not evaluate to node. -->
      <xsl:if test="$title != '' and @title != '' and upper-case(@title) != '_DATA'">
        <xforms:help><xsl:value-of select="@title"/></xforms:help>
      </xsl:if>
      <!-- To force updates to model first and toggle case later. -->
      <xforms:action events:event="DOMActivate">
        <xsl:apply-templates select="." mode="substitutions"/>
        <xforms:toggle case="{@to}.request"/>
      </xforms:action>
    </xforms:trigger>
  </xforms:group>
</xsl:template>

<xsl:template match="xtee:arc[@actuate = 'onLoad']" mode="link">
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:param name="operation" tunnel="yes"/>
  <xsl:param name="tnsprefix" tunnel="yes"/>
  <xsl:choose>
    <xsl:when test="exists(@arcrole)">
      <xforms:action events:event="xforms-select" if="boolean(instance('{$formname}.output')/SOAP-ENV:Body/{$tnsprefix}:{$operation}Response{concat(tm:extract-path(@arcrole), tm:extract-condition(@arcrole))})">
        <xsl:apply-templates select="." mode="substitutions"/>
        <xforms:toggle case="{@to}.request"/>
      </xforms:action>
    </xsl:when>
    <xsl:otherwise>
      <xforms:action events:event="xforms-select">
        <xsl:apply-templates select="." mode="substitutions"/>
        <xforms:toggle case="{@to}.request"/>
      </xforms:action>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="xtee:arc" mode="substitutions">
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:apply-templates select="$complex/xtee:substitution[tm:extract-label(@to) = current()/@to and (not(exists(@when)) or @when = current()/@from)]" mode="#current"/>
</xsl:template>

<xsl:template match="xtee:substitution" mode="substitutions">
  <xsl:param name="complex" tunnel="yes"/>
  <xsl:param name="formname" tunnel="yes"/>
  <xsl:variable name="from-form" select="tm:extract-label(@from)"/>
  <xsl:variable name="from-fullname" select="$complex/xtee:suboperation[@label = $from-form]/@href"/>
  <xsl:variable name="from-producer" select="tm:extract-producer($from-fullname)"/>
  <xsl:variable name="from-operation" select="tm:extract-operation($from-fullname)"/>
  <xsl:variable name="from-path" select="concat(tm:extract-path(@from), tm:extract-condition(@from))"/>
  <!--xsl:message select="concat('from-operation: ', $from-operation)"/>
  <xsl:message select="concat('from-form: ', $from-form)"/>
  <xsl:message select="concat('from-path: ', $from-path)"/-->
  <xsl:variable name="to-form" select="tm:extract-label(@to)"/>
  <xsl:variable name="to-fullname" select="$complex/xtee:suboperation[@label = $to-form]/@href"/>
  <xsl:variable name="to-producer" select="tm:extract-producer($to-fullname)"/>
  <xsl:variable name="to-operation" select="tm:extract-operation($to-fullname)"/>
  <xsl:variable name="to-path" select="replace(replace(
      tm:extract-path(@to), 
      '^/paring', '/keha'), 
      '^/url', '/keha/url')"/>
  <xsl:choose>
    <!-- If from value is constant. -->
    <xsl:when test="$from-form = ''">
      <xforms:setvalue ref="instance('{$to-form}.input')/SOAP-ENV:Body/{$to-producer}:{$to-operation}{$to-path}" value="'{@from}'"/>
    </xsl:when>
    <!-- If can use relative path. -->
    <xsl:when test="$from-form = $formname and not(starts-with($from-path, '/'))">
      <xforms:setvalue ref="instance('{$to-form}.input')/SOAP-ENV:Body/{$to-producer}:{$to-operation}{$to-path}" value="{$from-path}"/>
    </xsl:when>
    <!-- Must use absolute path. -->
    <xsl:otherwise>
      <xforms:setvalue ref="instance('{$to-form}.input')/SOAP-ENV:Body/{$to-producer}:{$to-operation}{$to-path}" value="instance('{$from-form}.output')/SOAP-ENV:Body/{$from-producer}:{$from-operation}Response{$from-path}"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
