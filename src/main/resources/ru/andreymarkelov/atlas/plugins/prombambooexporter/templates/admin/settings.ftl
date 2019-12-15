<html>
<head>
    <meta name="decorator" content="atl.admin">
    <meta name="activeTab" content="prom-bitbucket-exporter-admin-link">
    <title>${action.getText('ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.title')}</title>
</head>
<body>
<section id="content" role="main">
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h1>${action.getText('ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.title')}</h1>
                <p>${action.getText('ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.desc')}</p>
                <p>
                    <span class="aui-icon aui-icon-small aui-iconfont-info"></span>
                    ${action.getText('ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.linkdesc')}
                    <a target="_blank" href="${baseurl}/plugins/servlet/prometheus/metrics[#if token??]?token=${token}[/#if]">
                        ${action.getText('ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.link')}
                    </a>.
                </p>
            </div>
        </div>
    </header>
    <div class="aui-page-panel">
        <div class="aui-page-panel-inner">
            <section class="aui-page-panel-content">
                [#if saved]
                <div class="aui-message closeable shadowed">
                    <p class="title">
                        <span class="aui-icon icon-success"></span>
                        <strong>${action.getText('ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.success')}</strong>
                    </p>
                </div>
                [/#if]
                <div id="base-form">
                    [@ww.form
                        action="savesettings"
                        id="saveSettingsForm"
                        submitLabelKey='ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.actions.save'
                    ]
                        [@ww.textfield
                            labelKey="ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.token"
                            name="token"
                            required="false"
                            descriptionKey="ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.token.desc"
                        /]
                    [/@ww.form]
                </div>
            </section>
        </div>
    </div>
</section>
</body>
</html>
