package de.hpi.cpn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import de.hpi.cpn.converter.CPNConverter;

public class ImportTest
{
	@Test
	public void Importglobbox() throws IOException
	{
		CPNConverter transformer = new CPNConverter();
		
		System.out.print(transformer.importFirstPage(getXMlNamed("cpn10.cpn")));
	}
	
	private static String getXMlNamed(String filename) throws IOException
	{
		try
		{
			File f = new File(filename);

			FileReader fReader = new FileReader(f);
			BufferedReader bReader = new BufferedReader(fReader);
			String xml = "";
			while (bReader.ready())
			{
				xml += bReader.readLine();
			}
			
			xml = xml.substring(xml.indexOf("<workspaceElements>"));			
						
			return xml;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new IOException();
		}
		
		
	}

}
