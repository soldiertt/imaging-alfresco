package be.fsoffe.imaging.webscript;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class SudoScript extends BaseScopableProcessorExtension {

	// the list of allowed scripts from bean definition
	private List<String> allowedScripts;
	private static Log logger = LogFactory.getLog(SudoScript.class);
	private String list = "";

	public void setAllowedScripts(List<String> allowedScripts) {
		this.allowedScripts = allowedScripts;
	}

	public void sudo(final String path) throws Exception {
		logger.debug("Number of scripts allowed: " + allowedScripts.size());

		if (!allowedScripts.contains(path)) {
			Iterator<String> scripts = allowedScripts.iterator();
			while (scripts.hasNext()) {
				list += scripts.next() + "\n";
			}
			throw new Exception("The script " + path + " is a not allowed script to sudo... Allowed scripts: \n" + list);
		}

		// get the current context, needed to execute the script
		final Context cx = Context.getCurrentContext();

		// get current scope of the script that calls this class
		final Scriptable scope = getScope();

		// get the script from the classpath
		ClassLoader cl = this.getClass().getClassLoader();
		InputStreamReader is = new InputStreamReader(cl.getResourceAsStream(path));
		BufferedReader reader = new BufferedReader(is);
		final Script script = cx.compileReader(reader, path, 0, null);

		logger.debug("Got compiled script " + path);

		RunAsWork<Object> raw = new RunAsWork<Object>() {
			public Object doWork() throws Exception {
				logger.debug("Just before exec " + path);
				script.exec(cx, scope);
				return null;
			}
		};

		// actually run the script as the SystemUser
		AuthenticationUtil.runAs(raw, AuthenticationUtil.getSystemUserName());
	}
}
