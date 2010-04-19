/***************************************
 * Copyright (c) 2008
 * Helen Kaltegaertner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ****************************************/

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