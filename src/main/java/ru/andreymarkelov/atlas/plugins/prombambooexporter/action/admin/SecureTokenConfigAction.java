package ru.andreymarkelov.atlas.plugins.prombambooexporter.action.admin;

import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.struts.Preparable;
import ru.andreymarkelov.atlas.plugins.prombambooexporter.manager.SecureTokenManager;

import static com.atlassian.bamboo.util.ActionParamsUtils.getParameter;
import static com.opensymphony.xwork2.ActionContext.getContext;

public class SecureTokenConfigAction extends GlobalAdminAction implements Preparable {
    private final SecureTokenManager secureTokenManager;

    public SecureTokenConfigAction(SecureTokenManager secureTokenManager) {
        this.secureTokenManager = secureTokenManager;
    }

    @Override
    public String execute() throws Exception {
        secureTokenManager.setToken(getParameter("prometheus.settings.token"));
        return super.execute();
    }

    @Override
    public void prepare() {
        getContext().put("prometheus.settings.token", secureTokenManager.getToken());
    }
}
