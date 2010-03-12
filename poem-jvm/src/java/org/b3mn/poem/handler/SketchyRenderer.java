package org.b3mn.poem.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.b3mn.poem.Representation;
import org.b3mn.poem.util.ExportHandler;

import org.b3mn.poem.sketching.*;

@ExportHandler(uri="/sketch", formatName="Sketch", iconUrl="/backend/images/silk/pencil_go.png")
public class SketchyRenderer extends PdfRenderer {
	
	@Override
    protected void transcode(String in_s, OutputStream out, Representation representation) throws TranscoderException, IOException {
    	InputStream in = new ByteArrayInputStream(in_s.getBytes("UTF-8"));
	  	SketchyTransformer transformer = new SketchyTransformer(in, out, true);
	  	transformer.transform();
    }

}
