package ru.andreymarkelov.atlas.plugins.prombambooexporter.action.admin;

import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.bamboo.security.BambooPermissionManager;
import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.struts.Preparable;
import com.atlassian.user.User;
import ru.andreymarkelov.atlas.plugins.prombambooexporter.manager.SecureTokenManager;

import static com.opensymphony.xwork2.ActionContext.getContext;

public class SecureTokenConfigAction extends GlobalAdminAction implements Preparable {
    private final SecureTokenManager secureTokenManager;
    private final BambooAuthenticationContext bambooAuthenticationContext;
    private final BambooPermissionManager bambooPermissionManager;

    private boolean saved;
    private String token;

    public SecureTokenConfigAction(
            SecureTokenManager secureTokenManager,
            BambooAuthenticationContext bambooAuthenticationContext,
            BambooPermissionManager bambooPermissionManager) {
        this.secureTokenManager = secureTokenManager;
        this.bambooAuthenticationContext = bambooAuthenticationContext;
        this.bambooPermissionManager = bambooPermissionManager;
    }

    @Override
    public String execute() {
        User user = bambooAuthenticationContext.getUser();
        if (user == null || !bambooPermissionManager.isAdmin(user.getName())) {
            return ERROR;
        }
        secureTokenManager.setToken(token);
        saved = true;
        return SUCCESS;
    }

    @Override
    public String input() {
        token = secureTokenManager.getToken();
        saved = false;
        return INPUT;
    }

    @Override
    public void prepare() {
        getContext().put("baseurl", getBambooUrl().rootContext());
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
