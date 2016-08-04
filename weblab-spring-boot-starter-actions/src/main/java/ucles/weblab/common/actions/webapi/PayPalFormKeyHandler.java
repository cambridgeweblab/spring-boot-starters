package ucles.weblab.common.actions.webapi;

import ucles.weblab.common.webapi.TitledLink;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;

/**
 *
 */
public interface PayPalFormKeyHandler {

    ActionableResourceSupport.Action createAction(String businessKey, String taskId);
}
