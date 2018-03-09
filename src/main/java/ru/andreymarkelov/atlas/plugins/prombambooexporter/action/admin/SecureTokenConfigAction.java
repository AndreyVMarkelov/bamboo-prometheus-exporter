package ru.andreymarkelov.atlas.plugins.prombambooexporter.action.admin;

import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.struts.Preparable;
import ru.andreymarkelov.atlas.plugins.prombambooexporter.manager.SecureTokenManager;

public class SecureTokenConfigAction extends GlobalAdminAction implements Preparable {
    private final SecureTokenManager secureTokenManager;

    public SecureTokenConfigAction(SecureTokenManager secureTokenManager) {
        this.secureTokenManager = secureTokenManager;
    }


    @Override
    public void prepare() throws Exception {

    }


}
