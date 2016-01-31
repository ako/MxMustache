// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package test.actions;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import org.subethamail.wiser.Wiser;

/**
 * 
 */
public class StartSmtpMock extends CustomJavaAction<Boolean>
{
	public StartSmtpMock(IContext context)
	{
		super(context);
	}

	@Override
	public Boolean executeAction() throws Exception
	{
		// BEGIN USER CODE
		//throw new com.mendix.systemwideinterfaces.MendixRuntimeException("Java action was not implemented");
		Wiser wiser = new Wiser();
		wiser.setPort(2500); // Default is 25
		wiser.start();
		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public String toString()
	{
		return "StartSmtpMock";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
