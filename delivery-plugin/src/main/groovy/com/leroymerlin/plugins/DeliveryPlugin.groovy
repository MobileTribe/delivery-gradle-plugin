package com.leroymerlin.plugins

import com.leroymerlin.plugins.cli.DeliveryLogger
import com.leroymerlin.plugins.core.configurators.*
import com.leroymerlin.plugins.tasks.DockerUpload
import com.leroymerlin.plugins.tasks.ListArtifacts
import com.leroymerlin.plugins.tasks.ListDockerImages
import com.leroymerlin.plugins.tasks.build.DeliveryBuild
import com.leroymerlin.plugins.tasks.build.DockerBuild
import com.leroymerlin.plugins.utils.PropertiesUtils
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler
import org.gradle.api.internal.artifacts.mvnsettings.LocalMavenRepositoryLocator
import org.gradle.api.internal.artifacts.mvnsettings.MavenSettingsProvider
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.MavenPluginConvention
import org.gradle.api.plugins.MavenRepositoryHandlerConvention
import org.gradle.api.publication.maven.internal.DefaultDeployerFactory
import org.gradle.api.publication.maven.internal.DefaultMavenRepositoryHandlerConvention
import org.gradle.api.publication.maven.internal.MavenFactory
import org.gradle.api.tasks.Upload
import org.gradle.internal.Factory
import org.gradle.internal.logging.LoggingManagerInternal

import javax.inject.Inject

class DeliveryPlugin implements Plugin<Project> {

    public static final int COMPILE_PRIORITY = 300
    public static final int RUNTIME_PRIORITY = 200
    public static final int TEST_COMPILE_PRIORITY = 150
    public static final int TEST_RUNTIME_PRIORITY = 100

    public static final String UPLOAD_TASK_PREFIX = 'upload'
    public static final String INSTALL_TASK_PREFIX = 'install'

    public static final String UPLOAD_TASK = 'uploadArtifacts'
    public static final String INSTALL_TASK = 'installArtifacts'
    public static final String BASE_INSTALL_TASK = 'install'

    static final String VERSION_ARG = 'VERSION'
    static final String VERSION_ID_ARG = 'VERSION_ID'
    static final String GROUP_ARG = 'GROUP_ID'
    static final String PROJECT_NAME_ARG = 'ARTIFACT'
    static final String TASK_GROUP = 'delivery'
    static final String DELIVERY_CONF_FILE = 'delivery.properties'

    private final DeliveryLogger deliveryLogger = new DeliveryLogger()

    def configurators = [ReactConfigurator, IonicConfigurator, FlutterConfigurator, AndroidConfigurator, JavaConfigurator, IOSConfigurator]

    Project project
    DeliveryPluginExtension deliveryExtension

    private final Factory<LoggingManagerInternal> loggingManagerFactory
    private final FileResolver fileResolver
    private final MavenSettingsProvider mavenSettingsProvider
    private final LocalMavenRepositoryLocator mavenRepositoryLocator

    MavenPluginConvention mavenPluginConvention

    @Inject
    DeliveryPlugin(Factory<LoggingManagerInternal> loggingManagerFactory,
                   FileResolver fileResolver,
                   MavenSettingsProvider mavenSettingsProvider,
                   LocalMavenRepositoryLocator mavenRepositoryLocator) {
        this.loggingManagerFactory = loggingManagerFactory
        this.fileResolver = fileResolver
        this.mavenSettingsProvider = mavenSettingsProvider
        this.mavenRepositoryLocator = mavenRepositoryLocator
    }


    void apply(Project project) {
        this.project = project
        project.plugins.apply('maven')
        this.deliveryExtension = project.extensions.create(TASK_GROUP, DeliveryPluginExtension, project, this)
        MavenFactory mavenFactory = project.getServices().get(MavenFactory.class)
        this.mavenPluginConvention = new MavenPluginConvention(project as ProjectInternal, mavenFactory)
        Convention convention = project.getConvention()
        convention.getPlugins().put("maven", mavenPluginConvention)
        DefaultDeployerFactory deployerFactory = new DefaultDeployerFactory(
                mavenFactory,
                loggingManagerFactory,
                fileResolver,
                mavenPluginConvention,
                project.getConfigurations(),
                mavenPluginConvention.getConf2ScopeMappings(),
                mavenSettingsProvider,
                mavenRepositoryLocator)

        project.getTasks().withType(Upload.class, new Action<Upload>() {
            void execute(Upload upload) {
                RepositoryHandler repositories = upload.getRepositories()
                DefaultRepositoryHandler handler = (DefaultRepositoryHandler) repositories
                DefaultMavenRepositoryHandlerConvention repositoryConvention = new DefaultMavenRepositoryHandlerConvention(handler, deployerFactory)
                new DslObject(repositories).getConvention().getPlugins().put("maven", repositoryConvention)
            }
        })

        project.ext.DeliveryBuild = DeliveryBuild
        project.ext.DockerBuild = DockerBuild

        setupProperties()

        ProjectConfigurator detectedConfigurator = configurators.find {
            configurator ->
                configurator.newInstance().handleProject(project)
        }?.newInstance() as ProjectConfigurator
        if (detectedConfigurator == null) {
            detectedConfigurator = [] as ProjectConfigurator
        } else {
            deliveryLogger.logInfo("${project.name} configured as ${detectedConfigurator.class.simpleName - "Configurator"} project")
        }
        this.deliveryExtension.configurator = detectedConfigurator

        project.task(UPLOAD_TASK, group: TASK_GROUP)
        project.task(INSTALL_TASK, group: TASK_GROUP)
        project.tasks.maybeCreate(BASE_INSTALL_TASK)

        project.subprojects {
            Project subproject ->
                subproject.afterEvaluate {
                    if (deliveryExtension.autoLinkSubModules || deliveryExtension.linkedSubModules.contains(subproject.path)) {
                        subproject.plugins.withType(DeliveryPlugin.class) {
                            project.tasks.getByName(BASE_INSTALL_TASK).dependsOn += subproject.tasks.getByName(BASE_INSTALL_TASK)
                            project.tasks.getByName(INSTALL_TASK).dependsOn += subproject.tasks.getByName(INSTALL_TASK)
                            project.tasks.getByName(UPLOAD_TASK).dependsOn += subproject.tasks.getByName(UPLOAD_TASK)
                        }
                    }
                }
        }
        project.afterEvaluate {

            if (deliveryExtension.configurator == null) {
                throw new GradleException("Configurator is null. Can't configure your project. Please set the configurator or apply the plugin after your project plugin")
            }
            deliveryExtension.configurator.configure()

            project.tasks.withType(DeliveryBuild).asMap.each {
                taskName, task ->

                    if (project.versionId == null) throwException("VersionId", project)
                    if (project.version == null) throwException("Version", project)
                    if (project.artifact == null) throwException("Artifact", project)
                    if (project.group == null) throwException("Group", project)

                    String configurationName = task.variantName + "Config"
                    if (!project.configurations.hasProperty(configurationName)) {
                        project.configurations.create(configurationName)
                        project.dependencies.add(configurationName, 'org.apache.maven.wagon:wagon-http:2.2')

                        project.task("${UPLOAD_TASK_PREFIX}${task.variantName.capitalize()}Artifacts", type: Upload, group: TASK_GROUP) {
                            configuration = project.configurations."${configurationName}"
                            repositories deliveryExtension.archiveRepositories
                        }

                        def installTask = project.task("${INSTALL_TASK_PREFIX}${task.variantName.capitalize()}Artifacts", type: Upload, group: TASK_GROUP) {
                            configuration = project.configurations."${configurationName}"
                        }

                        MavenRepositoryHandlerConvention repositories = new DslObject(installTask.getRepositories()).getConvention().getPlugin(MavenRepositoryHandlerConvention.class)
                        def mavenInstaller = repositories.mavenInstaller()
                        MavenPom pom = mavenInstaller.getPom()
                        pom.setArtifactId(task.variantName as String)
                    }
                    ((Configuration) project.configurations."${configurationName}").artifacts.addAll(task.getArtifacts() as PublishArtifact[])

            }

            def uploadArtifacts = project.tasks.findByName(UPLOAD_TASK)
            uploadArtifacts.dependsOn += project.tasks.withType(Upload).findAll { task -> task.name.startsWith(UPLOAD_TASK_PREFIX) }
            if (project.tasks.findByPath("check") != null) {
                uploadArtifacts.dependsOn += project.tasks.findByPath("check")
            }

            project.tasks.findByName(INSTALL_TASK).dependsOn += project.tasks.withType(Upload).findAll { task -> task.name.startsWith(INSTALL_TASK_PREFIX) }
            project.task("listArtifacts", type: ListArtifacts, group: TASK_GROUP)

//Docker build

            project.tasks.withType(DockerBuild).asMap.each {
                String taskName, DockerBuild task ->
                    if (project.version == null) throwException("Version", project)

                    project.task("${UPLOAD_TASK_PREFIX}${task.name.capitalize()}", type: DockerUpload, group: TASK_GROUP, dependsOn: [task]) {
                        buildTask = task
                    }
            }

            uploadArtifacts.dependsOn += project.tasks.withType(DockerUpload)
            project.tasks.findByName(INSTALL_TASK).dependsOn += project.tasks.withType(DockerBuild)
            project.task("listDockerImages", type: ListDockerImages, group: TASK_GROUP)



            project.tasks.findByPath(BASE_INSTALL_TASK).dependsOn += INSTALL_TASK
        }
    }

//create default release git flow
    void enableReleaseGitFlow(boolean enable) {
        if (enable && !project.tasks.findByPath("releaseGitFlow")) {
            deliveryExtension.flowsContainer.create(
//tag::gitReleaseFlow[]
                    'releaseGit',
                    {
                        def releaseVersion = PropertiesUtils.getSystemProperty("VERSION", (project.version as String) - '-SNAPSHOT')
                        def releaseBranch = "release/${project.versionId}-$releaseVersion"
                        def matcher
                        def newVersion
                        try {
                            matcher = releaseVersion =~ /(\d+)([^\d]*$)/
                            newVersion = PropertiesUtils.getSystemProperty("NEW_VERSION", matcher.replaceAll("${(matcher[0][1] as int) + 1}${matcher[0][2]}")) - "-SNAPSHOT" + "-SNAPSHOT"
                        } catch (Exception ignored) {
                            throw new GradleException("ReleaseGitFLow only support semantic version (Ex: 1.0.0)")
                        }
                        //Use 'false' value to skip merge to base branch
                        def baseBranch = PropertiesUtils.getSystemProperty("BASE_BRANCH", 'master')
                        def workBranch = PropertiesUtils.getSystemProperty("BRANCH", 'develop')
                        def newVersionId = Integer.parseInt(project.versionId as String) + 1

                        branch workBranch
                        step 'prepareReleaseBranch', "prepare branch $releaseBranch"
                        branch releaseBranch, true
                        step 'prepareVersion', "prepare version"
                        changeProperties releaseVersion
                        getVersionFiles(project).each {
                            add it.path
                        }
                        step 'generateVersionFiles', "generate version files"
                        step 'commitVersionFiles', "commit version files"
                        commit "chore(version): Update version to $releaseVersion", true
                        step 'build', 'build and archive'
                        build
                        if (baseBranch != 'false') {
                            step 'stepMergeToBaseBranch', 'merge to base branch'
                            branch baseBranch
                            merge releaseBranch
                            push
                        }
                        step 'tagVersion', 'tag the commit'
                        tag "$project.artifact-$project.versionId-$releaseVersion"
                        pushTag
                        step 'updateVersion', "Update version to $newVersionId - $newVersion"
                        branch releaseBranch
                        changeProperties newVersion, newVersionId
                        getVersionFiles(project).each {
                            add it.path
                        }
                        commit "chore(version): Update to new version $newVersion and versionId $newVersionId", true
                        push
                        step 'mergeDevelop', "Merge release branch to $workBranch"
                        branch workBranch
                        merge releaseBranch
                        push
                    }
//end::gitReleaseFlow[]
            )
        } else {
            deliveryLogger.logWarning("releaseGitFlow was not created or already exists")
        }
    }

    void mapToMavenConfiguration(int priority, String fromConfig, String toConfig) {
        if (project.configurations.hasProperty(fromConfig)) {
            this.mavenPluginConvention.conf2ScopeMappings.addMapping(priority, project.configurations.getByName(fromConfig), toConfig)
        }
    }

    void setupProperties() {

        def parents = new ArrayList<Project>()
        parents.add(project)
        def actualParent = project.parent
        while (actualParent != null) {
            parents.add(actualParent)
            actualParent = actualParent.parent
        }
        Collections.reverse(parents)
        Properties versionProperties = new Properties()
        Properties deliveryProperties = new Properties()
        parents.forEach {
            Properties versionProp = PropertiesUtils.readPropertiesFile(it.file("version.properties"))
            versionProp.each {
                if (it.key != null && it.value != null) versionProperties.put(it.key, it.value)
            }
            Properties deliveryProp = PropertiesUtils.readPropertiesFile(it.file(DELIVERY_CONF_FILE))
            deliveryProp.each {
                if (it.key != null && it.value != null) deliveryProperties.put(it.key, it.value)
            }
        }

        PropertiesUtils.applyPropertiesOnProject(project, deliveryProperties)
        PropertiesUtils.applyPropertiesOnProject(project, versionProperties)

        if (!project.hasProperty('versionIdKey')) {
            project.ext.versionIdKey = 'versionId'
        }
        if (!project.hasProperty('versionKey')) {
            project.ext.versionKey = 'version'
        }
        if (!project.hasProperty('artifactKey')) {
            project.ext.artifactKey = 'artifact'
        }

        if (PropertiesUtils.getSystemProperty(VERSION_ID_ARG)) {
            versionProperties.put(project.versionIdKey as String, PropertiesUtils.getSystemProperty(VERSION_ID_ARG))
        }

        if (PropertiesUtils.getSystemProperty(VERSION_ARG)) {
            versionProperties.put(project.versionKey as String, PropertiesUtils.getSystemProperty(VERSION_ARG))
        }

        if (PropertiesUtils.getSystemProperty(PROJECT_NAME_ARG)) {
            versionProperties.put(project.artifactKey as String, PropertiesUtils.getSystemProperty(PROJECT_NAME_ARG))
        }

        if (PropertiesUtils.getSystemProperty(GROUP_ARG)) {
            versionProperties.put('group', PropertiesUtils.getSystemProperty(GROUP_ARG))
        }

        PropertiesUtils.applyPropertiesOnProject(project, versionProperties)

        project.ext.versionId = versionProperties.getProperty(project.versionIdKey as String)
        project.ext.version = versionProperties.getProperty(project.versionKey as String)
        project.version = versionProperties.getProperty(project.versionKey as String)

        if (!versionProperties.getProperty(project.artifactKey as String)) {
            project.ext.artifact = project.name
        } else {
            project.ext.artifact = versionProperties.getProperty(project.artifactKey as String)
        }

        if (versionProperties.getProperty('group')) {
            project.group = versionProperties.getProperty('group')
        }

        deliveryExtension.configurator?.applyProperties()
    }

    static List<File> getVersionFiles(Project project) {
        def parents = new ArrayList<Project>()
        def versionFiles = new ArrayList<File>()
        parents.add(project)
        def actualParent = project.parent
        while (actualParent != null) {
            parents.add(actualParent)
            actualParent = actualParent.parent
        }
        Collections.reverse(parents)
        parents.forEach {
            if (it.file("version.properties").exists()) versionFiles.add(it.file("version.properties"))
        }
        return versionFiles
    }

    static void throwException(String missingElement, Project project) {
        throw new GradleException("$missingElement is not set, please add it in a version.properties in ${project.name} or one of his parent")
    }

}
