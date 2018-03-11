[@ui.bambooSection titleKey="ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.title"]
    [@ww.form
        action="savesettings"
        id="saveSettingsForm"
        submitLabelKey='ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.actions.save'
    ]
        [@ww.textfield
            labelKey="ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.token"
            name="prometheus.settings.token"
            required="false"
            descriptionKey="ru.andreymarkelov.atlas.plugins.prombambooexporter.admin.settings.token.desc"
        /]
    [/@ww.form]
[/@ui.bambooSection]
