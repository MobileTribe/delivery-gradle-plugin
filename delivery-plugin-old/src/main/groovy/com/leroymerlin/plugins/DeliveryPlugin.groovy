package com.leroymerlin.plugins

import com.leroymerlin.plugins.adapters.BaseScmAdapter
import com.leroymerlin.plugins.entities.Taskdroid
import com.leroymerlin.plugins.internal.SigningProperty
import com.leroymerlin.plugins.utils.BuildMethods
import com.leroymerlin.plugins.utils.ReleaseMethods
import com.leroymerlin.plugins.utils.ScmFlowMethods
import com.leroymerlin.plugins.utils.Utils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DeliveryPlugin implements Plugin<Project> {
    public Logger logger = LoggerFactory.getLogger('DeliveryPlugin')

    static final String TASK_GROUP = 'delivery'
    Project project;
    public DeliveryPluginExtension extension;


    public ReleaseMethods releaseMethods;
    public ScmFlowMethods scmFlowMethods;
    public BuildMethods buildMethods;

    def tasks = []
    def tasksInitRelease = []
    def tasksValidateRelease = []


    def versionKey = 'version'
    def versionIdKey = 'versionId'
    def projectNameKey = 'projectName'
    File versionFile

    void apply(Project project) {
        this.project = project
        this.project.apply plugin: 'com.github.dcendents.android-maven'

        def signingProperties = project.container(SigningProperty)
        this.extension = this.project.extensions.create("delivery", DeliveryPluginExtension, this.project, this, signingProperties)
        // create SCM method and apply closure on them
        extension.scmAdapters.each {
            name, adapterClass ->
                BaseScmAdapter adapter = adapterClass.newInstance(this.project);
                def config = adapter.createNewConfig()
                extension.metaClass."${name}Config" = config
                extension.metaClass."$name" << { closure ->
                    closure.setDelegate(config)
                    closure.run()
                }
        }

        this.releaseMethods = new ReleaseMethods(project, this)
        this.scmFlowMethods = new ScmFlowMethods(project, this)
        this.buildMethods = new BuildMethods(project, this)

        applyPropertiesFiles()
        checkProperties()

        if (System.getProperty("VERSION")) {
            setProjectProperty(versionKey, System.getProperty("VERSION"))
        }
        if (System.getProperty("VERSION_ID")) {
            setProjectProperty(versionIdKey, System.getProperty("VERSION_ID"))
        }

        def isAndroidApp = project.plugins.hasPlugin("com.android.application")
        def isAndroidLibrary = project.plugins.hasPlugin("com.android.library")
        if (isAndroidApp || isAndroidLibrary) {
            project.android {
                defaultConfig {
                    versionName project.version
                    versionCode Integer.parseInt(project.versionId)
                }

                buildTypes.all {
                    buildType ->
                        signingProperties.maybeCreate(buildType.name)
                }
            }
        }


        this.initTasks()
        this.applyTasksToProject()

        this.project.afterEvaluate {

            //Check the properties file once the build.gradle is read
            this.checkConfig()
            this.buildMethods.createTasksArchiveAPKs()
        }
    }

    public void applyVersionKeys() {
        if (project.hasProperty('versionFilePath')) {
            versionFile = project.file(project.versionFilePath);
        } else {
            versionFile = project.file('version.properties')
        }
        if (project.hasProperty('versionIdKey')) {
            versionIdKey = project.versionIdKey
        }
        if (project.hasProperty('versionKey')) {
            versionKey = project.versionKey
        }
        if (project.hasProperty('projectNameKey')) {
            projectNameKey = project.projectNameKey
        }
    }


    private void applyTasksToProject() {

        //Création de la suite des tâches et de leurs dépendances les unes par rapport aux autres
        def nameTasksInitRelease = []
        tasksInitRelease.each {
            taskdroid ->
                nameTasksInitRelease.push(taskdroid.createTask(project, nameTasksInitRelease.isEmpty() ? null : nameTasksInitRelease.last()))
        }

        def nameTasksValidateRelease = []
        tasksValidateRelease.each {
            taskdroid ->
                nameTasksValidateRelease.push(taskdroid.createTask(project, nameTasksValidateRelease.isEmpty() ? null : nameTasksValidateRelease.last()))
        }
    }

    private void initTasks() {
        //Initialize Scm Adapter
        project.task("prepareScmAdapter", description: "Initialize Scm Adapter", group: TASK_GROUP) << scmFlowMethods.&prepareScmAdapter
        project.task("prepareScmAdapterForValidation", description: "Initialize Scm Adapter for validation", group: TASK_GROUP) << scmFlowMethods.&prepareScmAdapter
        project.task("releaseScmAdapter", description: "Release Scm Adapter", group: TASK_GROUP) << scmFlowMethods.&releaseScmAdapter
        project.task("releaseScmAdapterForValidation", description: "Release Scm Adapter for validation", group: TASK_GROUP) << scmFlowMethods.&releaseScmAdapter

        project.task("checkSnapshotDependencies", description: "Looks for Snapshot dependencies", group: TASK_GROUP) << releaseMethods.&checkSnapshotDependencies


        this.tasksInitRelease = [
                //Création d'une branche de release avec le numéro de version
                new Taskdroid("createReleaseBranch", "Creates a release branch", TASK_GROUP, scmFlowMethods.&createReleaseBranch, ['prepareScmAdapter']),
                new Taskdroid("updateVersionsFile", "Apply the new version number and the new version code passed in params", TASK_GROUP, releaseMethods.&updateVersionsFile),
                new Taskdroid("commitBranch", "Pushes the release branch on the SCM", TASK_GROUP, scmFlowMethods.&commitReleaseBranch),
                //Une fois que tous les APKs sont créés, upload sur un gestionnaire de fichiers come le nexus
                //Les infos de connexion au gestionnaire de fichiers sont à configurer dans une closure et sont gérés ds l'extension
                new Taskdroid("delivery", "Upload the project artifacts on a file manager", TASK_GROUP, {
                    logger.warn("Release done. Don't forget to validate it with 'validateRelease'")
                }, ["runBuildTasks", "releaseScmAdapter"])
        ]

        this.tasksValidateRelease = [
                //Une fois que toutes les APKs sont build sans exception, les sources sont mergées sur master et la version est taggée
                //avec la forme "vX.X.X"
                new Taskdroid("mergeReleaseBranch", "Merge the release branch on master and tags", TASK_GROUP, scmFlowMethods.&mergeReleaseBranch, ['prepareScmAdapterForValidation']),
                //Pas sûr que ça marche, si ça fonctionne pas tant pis, on n'execute pas la prochaine task et c'est tout.
                //Penser à afficher un beau message d'erreur.
                new Taskdroid("mergeOnDevelop", "Try to merge the release branch on develop", TASK_GROUP, scmFlowMethods.&backOnDevelop),
                //CF la description de la tâche
                new Taskdroid("prepareNextVersion", "Update the version number and add -SNAPSHOT", TASK_GROUP, releaseMethods.&prepareNextVersion),
                //Supprime la branche
                //new Taskdroid("removeBranchRelease", "Delete the Release branch", TASK_GROUP, scmFlowMethods.&cleanReleaseBranch),
                new Taskdroid("validateDelivery", "Upload the project artifacts on a file manager", TASK_GROUP, {
                    logger.warn("Release validated")
                }, ["releaseScmAdapterForValidation"])

        ]
    }


    protected void applySigningProperty(SigningProperty signingProperty) {
        def hasAppPlugin = project.plugins.hasPlugin("com.android.application")
        if (hasAppPlugin) {
            def buildType = project.android.buildTypes.findByName(signingProperty.name)


            Properties properties = new Properties()

            if (buildType == null) {
                throw new IllegalStateException("Signing property can't apply on missing buildType : " + signingProperty.name)
            } else if (signingProperty.propertiesFile == null) {
                logger.warn("Signing property file not defined");
                return;
            } else if (!signingProperty.propertiesFile.exists()) {
                logger.warn("Signing property file doesn't exist : " + signingProperty.propertiesFile)
                return;
            } else {
                signingProperty.propertiesFile.withInputStream {
                    stream -> properties.load(stream)
                }
                def filePath = properties.getProperty(signingProperty.storeFileField)
                if (filePath == null) {
                    throw new IllegalStateException("KS can't be found with null filePath. Please add ${signingProperty.storeFileField} in $signingProperty.propertiesFile")
                } else if (!project.file(filePath).exists()) {
                    throw new IllegalStateException("KS not found for buildType '${signingProperty.name}' at path $filePath")
                }
            }

            def ksFile = project.file(properties.getProperty(signingProperty.storeFileField))

            project.android.signingConfigs {
                "${signingProperty.name}Signing" {
                    storeFile ksFile
                    storePassword properties.getProperty(signingProperty.storePasswordField)
                    keyAlias properties.getProperty(signingProperty.keyAliasField)
                    keyPassword properties.getProperty(signingProperty.keyAliasPasswordField)
                }
            }
            buildType.signingConfig project.android.signingConfigs."${signingProperty.name}Signing"
        }

    }


    void checkProperties() {
        checkProperty(versionKey, '0.0.1-SNAPSHOT', versionFile, true);
        checkProperty(versionIdKey, '2', versionFile, true);
        checkProperty(projectNameKey, project.name, versionFile, false);
    }


    private void checkProperty(String name, String defaultValue, File file, boolean shouldContain) {
        if (!project.hasProperty(name) || !file.exists() || (shouldContain && Utils.readProperty(file, name) == null)) {
            Utils.setPropertyInFile(file, ["${name}": defaultValue]);
            throw new GradleException("property $name not found in your project. It has been added in ${file.path} with value $defaultValue. Please try to resync !")
        }
    }

    void checkConfig() {
        //Check that properties are applied on android extension
        def isAndroidApp = project.plugins.hasPlugin("com.android.application")
        def isAndroidLibrary = project.plugins.hasPlugin("com.android.library")

        String version = project.version
        if (isAndroidApp) {
            if (!(project.android.defaultConfig.versionName == version)) {
                throw new GradleException("app versionName is ${project.android.defaultConfig.versionName} but should be $version. Please set: android.defaultConfig.versionName $versionKey")
            }
            if (!(project.android.defaultConfig.versionCode == Integer.parseInt(project.versionId))) {
                throw new GradleException("app versionCode is ${project.android.defaultConfig.versionCode} but should be ${project.versionId}. Please set: android.defaultConfig.versionCode Integer.parseInt($versionIdKey)")
            }

            if (project.android.defaultConfig.applicationId)
                project.group = project.android.defaultConfig.applicationId
        } else if (isAndroidLibrary) {
            def manifestFile = project.file("src/main/AndroidManifest.xml")
            if (manifestFile.exists()) {
                def manifest = new XmlParser(false, false).parse(manifestFile)
                project.group = manifest."@package"
            }
        }


        if (!project.group) {
            throw new GradleException("Project group is not defined. Please use a gradle properties or configure your defaultConfig.applicationId")
        }
        logger.info("group used : ${project.group}")


    }

    public void applyPropertiesFiles() {
        File deliveryProperties = project.file('delivery.properties')
        if (deliveryProperties.exists())
            applyPropertiesOnProject(deliveryProperties);

        applyVersionKeys()

        if (versionFile.exists()) {
            applyPropertiesOnProject(versionFile)
        }
    }


    public void applyPropertiesOnProject(File file) {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file))
        properties.each { prop ->
            setProjectProperty(prop.key, prop.value)
        }
    }

    public void setProjectProperty(def key, def value) {
        if (key == versionKey && key != 'version') {
            setProjectProperty('version', value)
        } else if (key == versionIdKey && key != 'versionId') {
            setProjectProperty('versionId', value)
        } else if (key == projectNameKey && key != 'projectName') {
            setProjectProperty('projectName', value)
        }

        project.ext.set(key, value);
        if (key == 'version')
            project.setProperty(key, value);
    }

    public void warnOrThrow(boolean doThrow, String message) {
        if (doThrow) {
            throw new GradleException(message)
        } else {
            logger.warn("!!WARNING!! $message")
        }
    }


}
