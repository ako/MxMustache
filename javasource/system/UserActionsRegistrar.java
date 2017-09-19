package system;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.mendix.core.actionmanagement.IActionRegistrator;

@Component(immediate = true)
public class UserActionsRegistrar
{
  @Reference
  public void registerActions(IActionRegistrator registrator)
  {
    registrator.bundleComponentLoaded();
    registrator.registerUserAction(appcloudservices.actions.GenerateRandomPassword.class);
    registrator.registerUserAction(appcloudservices.actions.LogOutUser.class);
    registrator.registerUserAction(appcloudservices.actions.StartSignOnServlet.class);
    registrator.registerUserAction(mxlists.actions.GenerateDateTimeSeries.class);
    registrator.registerUserAction(mxlists.actions.GenerateLongSeries.class);
    registrator.registerUserAction(mxmustache.actions.FillTemplate.class);
    registrator.registerUserAction(mxmustache.actions.FillTemplateFromJson.class);
    registrator.registerUserAction(mxmustache.actions.FillTemplateFromMappingToFileDocument.class);
    registrator.registerUserAction(mxmustache.actions.FillTemplateToFileDocument.class);
    registrator.registerUserAction(mxmustache.actions.SendEmail.class);
    registrator.registerUserAction(mxmustachetests.actions.StartSmtpMock.class);
    registrator.registerUserAction(objecthandling.actions.clone.class);
    registrator.registerUserAction(objecthandling.actions.commitInSeparateDatabaseTransaction.class);
    registrator.registerUserAction(objecthandling.actions.copyAttributes.class);
    registrator.registerUserAction(objecthandling.actions.createObjectListFromObject.class);
    registrator.registerUserAction(objecthandling.actions.deepClone.class);
    registrator.registerUserAction(objecthandling.actions.deleteAll.class);
    registrator.registerUserAction(objecthandling.actions.deleteInSeparateTransaction.class);
    registrator.registerUserAction(objecthandling.actions.deleteWithoutEvents.class);
    registrator.registerUserAction(objecthandling.actions.EndTransaction.class);
    registrator.registerUserAction(objecthandling.actions.getCreatedByUser.class);
    registrator.registerUserAction(objecthandling.actions.getGUID.class);
    registrator.registerUserAction(objecthandling.actions.getLastChangedByUser.class);
    registrator.registerUserAction(objecthandling.actions.getOriginalValueAsString.class);
    registrator.registerUserAction(objecthandling.actions.getTypeAsString.class);
    registrator.registerUserAction(objecthandling.actions.memberHasChanged.class);
    registrator.registerUserAction(objecthandling.actions.objectHasChanged.class);
    registrator.registerUserAction(objecthandling.actions.refreshClassByObject.class);
    registrator.registerUserAction(objecthandling.actions.StartTransaction.class);
    registrator.registerUserAction(system.actions.VerifyPassword.class);
    registrator.registerUserAction(unittesting.actions.FindAllUnitTests.class);
    registrator.registerUserAction(unittesting.actions.ReportStepJava.class);
    registrator.registerUserAction(unittesting.actions.RunAllUnitTestsWrapper.class);
    registrator.registerUserAction(unittesting.actions.RunUnitTest.class);
    registrator.registerUserAction(unittesting.actions.StartRemoteApiServlet.class);
    registrator.registerUserAction(unittesting.actions.StartRunAllSuites.class);
    registrator.registerUserAction(unittesting.actions.ThrowAssertionFailed.class);
  }
}
