package mendixsso.implementation;

import mendixsso.proxies.UserProfile;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import java.util.HashMap;
import java.util.Map;

public class UserMapper {

    private static UserMapper instance = null;
    private String createUserMicroflowName;
    private String updateUserMicroflowName;
    private String userEntityName;


    private UserMapper() {
    }

    public static UserMapper getInstance() {
        if (instance == null) {
            instance = new UserMapper();
        }
        return instance;
    }

    public void initialize(String createUserMicroflowName, String updateUserMicroflowName) {

        this.userEntityName = validateMicroflows(createUserMicroflowName, updateUserMicroflowName);
        this.createUserMicroflowName = createUserMicroflowName;
        this.updateUserMicroflowName = updateUserMicroflowName;
    }


    public IMendixObject createUser(IContext context, UserProfile userProfile) throws CoreException {

        return Core.execute(context, this.createUserMicroflowName, new HashMap<String, Object>() {{
            put("UserProfile", userProfile.getMendixObject());
        }});
    }

    public IMendixObject updateUser(IContext context, UserProfile userProfile) throws CoreException {

        return Core.execute(context, this.updateUserMicroflowName, new HashMap<String, Object>() {{
            put("UserProfile", userProfile.getMendixObject());
        }});
    }

    public String getUserEntityName(){
        return this.userEntityName;
    }

    private String validateMicroflows(String createUserMicroflowName, String updateUserMicroflowName) {

        final String returnObjectType = validateMicroflow(createUserMicroflowName);
        if (!returnObjectType.equals(validateMicroflow(updateUserMicroflowName))){
            throw new MendixRuntimeException(String.format("Mismatch between return parameter type of '%s' microflow and " +
                    "'%s' microflow. ", createUserMicroflowName, updateUserMicroflowName));
        }

        return returnObjectType;
    }

    private String validateMicroflow(String microflowName) {
        // check input parameters
        final Map<String, IDataType> inputParameters = Core.getInputParameters(microflowName);
        if (!inputParameters.containsKey("UserProfile")) {
            throw new MendixRuntimeException(String.format("Missing input parameter in the '%s' microflow. " +
                    "Expected input parameter: '%s'.", microflowName, "MendixSSO.UserProfile"));
        }
        final IDataType dataType = inputParameters.get("UserProfile");
        if (!dataType.isMendixObject() || !dataType.getObjectType().equals("MendixSSO.UserProfile")) {
            throw new MendixRuntimeException(String.format("Invalid input parameter type in the '%s' microflow. " +
                    "Actual: '%s', expected: '%s'.", microflowName, dataType.getObjectType(), "MendixSSO.UserProfile"));
        }
        // check return type
        final IDataType returnType = Core.getReturnType(microflowName);
        if (returnType.isNothing() || !returnType.isMendixObject()) {
            throw new MendixRuntimeException(String.format("Invalid return type in the '%s' microflow. " +
                    "Return type should be generalized by: '%s'.", microflowName, "System.User"));
        }
        String returnObjectType = returnType.getObjectType();

        if (returnObjectType == null || !Core.isSubClassOf("System.User", returnObjectType)) {
            throw new MendixRuntimeException(String.format("Invalid return type in the '%s' microflow. " +
                    "Actual: '%s', expected return type should be generalized by: '%s'.", microflowName, returnType.getObjectType(), "System.User"));
        }

        return returnObjectType;
    }
}
