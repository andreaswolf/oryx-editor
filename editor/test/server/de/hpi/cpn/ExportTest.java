package de.hpi.cpn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.junit.Test;

import de.hpi.cpn.converter.CPNConverter;


public class ExportTest 
{
	@Test
	public void testMapping() throws JSONException, IOException
	{
		String json = getJSONNamed("JSON3.json");	
		
		CPNConverter trans = new CPNConverter();
		
		String xml = trans.convertToCPNFile(json);
		
		System.out.println(xml);		
	}
	
	private static String getJSONNamed(String filename) throws IOException
	{
		try
		{
			File f = new File(filename);

			FileReader fReader = new FileReader(f);
			BufferedReader bReader = new BufferedReader(fReader);
			String json = "";
			while (bReader.ready())
			{
				json += bReader.readLine();
			}			
						
			return json;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new IOException();
		}
		
		
	}
}
