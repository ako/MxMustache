// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package mendixsso.actions;

import mendixsso.implementation.UserMapper;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;

public class CreateUserWithUserProfile extends CustomJavaAction<IMendixObject>
{
	private IMendixObject __userProfile;
	private mendixsso.proxies.UserProfile userProfile;

	public CreateUserWithUserProfile(IContext context, IMendixObject userProfile)
	{
		super(context);
		this.__userProfile = userProfile;
	}

	@java.lang.Override
	public IMendixObject executeAction() throws Exception
	{
		this.userProfile = __userProfile == null ? null : mendixsso.proxies.UserProfile.initialize(getContext(), __userProfile);

		// BEGIN USER CODE
        return UserMapper.getInstance().createUser(getContext(), userProfile);
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "CreateUserWithUserProfile";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}