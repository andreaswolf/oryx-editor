package org.b3mn.poem.sketching;

import java.util.*;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.StyleHandler;
import org.w3c.dom.*;

public class CSSStyleHandler implements StyleHandler {

    // The CDATA section that holds the CSS stylesheet.
    private CDATASection styleSheet;
    private SVGGeneratorContext ctx;

    // Build the handler with a reference to the stylesheet section.
    public CSSStyleHandler(CDATASection styleSheet, SVGGeneratorContext ctx) {
        this.styleSheet = styleSheet;
        this.ctx		= ctx;
    }
    
    // invokes other setStyle Method with required parameter SVGGeneratorContext
    public void setStyle(Element element, Map styleMap){
    	this.setStyle(element, styleMap, this.ctx);
    }

    // parameter context is necessary to comply with the interface StyleHandler
    public void setStyle(Element element, Map styleMap, SVGGeneratorContext context) {
        Iterator iter = styleMap.keySet().iterator();

        // Create a new class in the style sheet.
        String id = context.getIDGenerator().generateID("C");
        styleSheet.appendData("."+ id +" {");

        // Append each key/value pair.
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = (String) styleMap.get(key);
            styleSheet.appendData(key + ":" + value + ";");
        }

        styleSheet.appendData("}\n");

        // Reference the stylesheet class on the element to be styled.
        element.setAttributeNS(null, "class", id);
    }
}