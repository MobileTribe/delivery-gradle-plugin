package com.leroymerlin.plugins

import com.leroymerlin.plugins.cli.Executor
import com.leroymerlin.plugins.core.configurators.*
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
import com.leroymerlin.plugins.utils.PropertiesUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.internal.artifacts.Module
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.MavenRepositoryHandlerConvention
import org.gradle.api.tasks.Upload
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeliveryPlugin implements Plugin<Project> {


    Logger logger = LoggerFactory.getLogger('DeliveryPlugin')

    public static final String UPLOAD_TASK_PREFIX = 'upload'
    public static final String INSTALL_TASK_PREFIX = 'install'

    public static final String TASK_UPLOAD = 'uploadArtifacts'
    public static final String TASK_INSTALL = 'installArtifacts'

    static final String VERSION_ARG = 'VERSION'
    static final String VERSION_ID_ARG = 'VERSION_ID'
    static final String GROUP_ARG = 'GROUP'
    static final String PROJECT_NAME_ARG = 'PROJECT_NAME'
    static final String TASK_GROUP = 'delivery'
    static final String DELIVERY_CONF_FILE = 'delivery.properties'

    def configurators = [IonicConfigurator, AndroidConfigurator, JavaConfigurator, IOSConfigurator]

    Project project
    DeliveryPluginExtension deliveryExtension


    void apply(Project project) {
        this.project = project
        this.deliveryExtension = project.extensions.create(TASK_GROUP, DeliveryPluginExtension, project, this)
        project.plugins.apply('maven')
        Executor.logger = logger
        project.ext.DeliveryBuild = DeliveryBuild


        setupProperties()

        ProjectConfigurator detectedConfigurator = configurators.find {
            configurator ->
                configurator.newInstance().handleProject(project)
        }?.newInstance()
        if (detectedConfigurator == null) {
            detectedConfigurator = [] as ProjectConfigurator
        } else {
            logger.warn("${project.name} configured as ${detectedConfigurator.class.simpleName - "Configurator"} project")
        }
        this.deliveryExtension.configurator = detectedConfigurator



        project.task(TASK_UPLOAD, group: TASK_GROUP)
        project.task(TASK_INSTALL, group: TASK_GROUP)

        project.afterEvaluate {
            if (deliveryExtension.configurator == null) {
                throw new GradleException("Configurator is null. Can't configure your project. Please set the configurator or apply the plugin after your project plugin")
            }
            deliveryExtension.configurator.configure()

            def buildTasks = []
            buildTasks.addAll(project.tasks.withType(DeliveryBuild))

            buildTasks.each {
                task ->
                    def configurationName = task.variantName + "Config"
                    if (!project.configurations.hasProperty(configurationName)) {
                        ConfigurationInternal config = project.configurations.create(configurationName)
                        project.dependencies.add(configurationName, 'org.apache.maven.wagon:wagon-http:2.2')




                        project.task("${UPLOAD_TASK_PREFIX}${task.variantName.capitalize()}Artifacts", type: Upload, group: TASK_GROUP) {
                            configuration = project.configurations."${configurationName}"
                            repositories deliveryExtension.archiveRepositories
                        }


                        def installTask = project.task("${INSTALL_TASK_PREFIX}${task.variantName.capitalize()}Artifacts", type: Upload, group: TASK_GROUP) {
                            configuration = project.configurations."${configurationName}"
                        }

                        MavenRepositoryHandlerConvention repositories = new DslObject(installTask.getRepositories()).getConvention().getPlugin(MavenRepositoryHandlerConvention.class);
                        def mavenInstaller = repositories.mavenInstaller();
                        MavenPom pom = mavenInstaller.getPom()
                        pom.setArtifactId(task.variantName)

                        //installTask.getConvention().getPlugins().get("maven").mavenInstaller();
                    }
                    ((Configuration) project.configurations."${configurationName}").artifacts.addAll(task.getArtifacts())
            }

            def uploadArtifacts = project.tasks.findByName(TASK_UPLOAD)
            uploadArtifacts.dependsOn += project.tasks.withType(Upload).findAll { task -> task.name.startsWith(UPLOAD_TASK_PREFIX) }
            if (project.tasks.findByPath("check") != null) {
                uploadArtifacts.dependsOn += project.tasks.findByPath("check")
            }

            project.tasks.findByName(TASK_INSTALL).dependsOn += project.tasks.withType(Upload).findAll { task -> task.name.startsWith(INSTALL_TASK_PREFIX) }
            if (project.tasks.findByPath("install") == null) {
                project.task("install", dependsOn: [TASK_INSTALL])
            }

            //create default release git flow

            if (!deliveryExtension.flowsContainer.hasProperty("releaseGit")) {
                deliveryExtension.flowsContainer.create(
//tag::gitReleaseFlow[]
'releaseGit',
{
    def releaseVersion = PropertiesUtils.getSystemProperty("VERSION", project.version - '-SNAPSHOT')
    def releaseBranch = "release/${project.versionId}-$releaseVersion"
    def matcher = releaseVersion =~ /(\d+)([^\d]*$)/
    def newVersion = PropertiesUtils.getSystemProperty("NEW_VERSION", matcher.replaceAll("${(matcher[0][1] as int) + 1}${matcher[0][2]}")) - "-SNAPSHOT" + "-SNAPSHOT"
    //Use 'false' value to skip merge to base branch
    def baseBranch = PropertiesUtils.getSystemProperty("BASE_BRANCH", 'master')
    def workBranch = PropertiesUtils.getSystemProperty("BRANCH", 'develop')
    def newVersionId = Integer.parseInt(project.versionId) + 1
    def propertyFile = getVersionFile()

    branch workBranch
    step 'prepareReleaseBranch', "prepare branch $releaseBranch"
    branch releaseBranch, true
    step 'prepareVersion', "prepare version"
    changeProperties releaseVersion
    add propertyFile.path
    step 'generateVersionFiles', "generate version files"
    step 'commitVersionFiles', "commit version files"
    commit "chore (version) : Update version to $releaseVersion"
    step 'build', 'build and archive'
    build
    step 'tagVersion', 'tag the commit'
    tag "$project.projectName-$project.versionId-$releaseVersion"
    if (baseBranch != 'false') {
        step 'stepMergeToBaseBranch', 'merge to base branch'
        branch baseBranch
        merge releaseBranch
        push
    }
    step 'updateVersion', "Update version to $newVersionId - $newVersion"
    branch releaseBranch
    changeProperties newVersion, newVersionId
    add propertyFile.path
    commit "chore (version) : Update to new version $releaseVersion and versionId $newVersionId"
    push
    step 'mergeDevelop', "Merge release branch to $workBranch"
    branch workBranch
    merge releaseBranch
    push
}
//end::gitReleaseFlow[]
                )
            }
        }
    }


    void setupProperties() {
        //Read and apply Delivery.properties file to override default version.properties path and version, versionId, projectName keys
        PropertiesUtils.readAndApplyPropertiesFile(project, project.file(DELIVERY_CONF_FILE))

        //Apply default value if needed
        File versionFile = getVersionFile()
        if (!project.hasProperty('versionIdKey')) {
            project.ext.versionIdKey = 'versionId'
        }
        PropertiesUtils.setDefaultProperty(versionFile, project.ext.versionIdKey, "2")

        if (!project.hasProperty('versionKey')) {
            project.ext.versionKey = 'version'
        }
        PropertiesUtils.setDefaultProperty(versionFile, project.ext.versionKey, "1.0.0-SNAPSHOT")

        if (!project.hasProperty('projectNameKey')) {
            project.ext.projectNameKey = 'projectName'
        }
        PropertiesUtils.setDefaultProperty(versionFile, project.ext.projectNameKey, project.name)

        if (PropertiesUtils.getSystemProperty(VERSION_ID_ARG)) {
            PropertiesUtils.setProperty(versionFile, project.ext.versionIdKey, PropertiesUtils.getSystemProperty(VERSION_ID_ARG))
        }
        if (PropertiesUtils.getSystemProperty(VERSION_ARG)) {
            PropertiesUtils.setProperty(versionFile, project.ext.versionKey, PropertiesUtils.getSystemProperty(VERSION_ARG))
        }
        if (PropertiesUtils.getSystemProperty(GROUP_ARG)) {
            PropertiesUtils.setProperty(versionFile, 'group', PropertiesUtils.getSystemProperty(GROUP_ARG))
        }
        if (PropertiesUtils.getSystemProperty(PROJECT_NAME_ARG)) {
            PropertiesUtils.setProperty(versionFile, project.ext.projectNameKey, PropertiesUtils.getSystemProperty(PROJECT_NAME_ARG))
        }
        applyDeliveryProperties(versionFile)
    }

    File getVersionFile() {
        if (project.hasProperty('versionFilePath')) {
            return project.file(project.property('versionFilePath'))
        } else {
            return project.file('version.properties')
        }
    }

    void applyDeliveryProperties(File versionFile) {
        PropertiesUtils.readAndApplyPropertiesFile(project, versionFile)
        project.ext.versionId = project.ext."${project.ext.versionIdKey}"
        project.ext.version = project.ext."${project.ext.versionKey}"
        project.version = project.ext."${project.ext.versionKey}"
        if (project.extensions.getExtraProperties().has("group")) {
            project.group = project.ext.group
        }
        project.ext.projectName = project.ext."${project.ext.projectNameKey}"
        deliveryExtension.configurator?.applyProperties()
    }
}
