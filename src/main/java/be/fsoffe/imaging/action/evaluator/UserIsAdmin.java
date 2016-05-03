package be.fsoffe.imaging.action.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;

/**
 * Evaluator checking if user is administrator.
 * 
 * @author jbourlet
 *
 */
public class UserIsAdmin extends RegisteredActionEvaluator {

	private transient AuthorityService authService;
	
	
	public void setAuthService(AuthorityService authService) {
		this.authService = authService;
	}


	@Override
	public boolean evaluate(NodeRef node) {
		return authService.hasAdminAuthority();
	}

}
