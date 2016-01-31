// This file was generated by Mendix Modeler 6.0.
//
// WARNING: Code you write here will be lost the next time you deploy the project.

package test.proxies.microflows;

import java.util.HashMap;
import java.util.Map;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;

public class Microflows
{
	// These are the microflows for the Test module

	public static void mF_Gmail(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.MF_Gmail", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}

	public static void testCsvTemplateMultipleObjects(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.TestCsvTemplateMultipleObjects", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}

	public static void testEmailMarkdownTemplateMultipleObjects(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.TestEmailMarkdownTemplateMultipleObjects", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}

	public static void testJsonTemplateMultipleObjects(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.TestJsonTemplateMultipleObjects", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}

	public static void testMarkdownPdfTemplateMultipleObjects(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.TestMarkdownPdfTemplateMultipleObjects", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}

	public static void testMarkdownTemplateMultipleObjects(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.TestMarkdownTemplateMultipleObjects", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}

	public static void testTemplateSingleObject(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.TestTemplateSingleObject", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}

	public static void testXmlTemplateMultipleObjects(IContext context)
	{
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			Core.execute(context, "Test.TestXmlTemplateMultipleObjects", params);
		}
		catch (CoreException e)
		{
			throw new MendixRuntimeException(e);
		}
	}
}