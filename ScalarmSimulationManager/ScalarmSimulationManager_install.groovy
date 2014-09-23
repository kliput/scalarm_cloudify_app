import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmSimulationManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"] + "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

//println "ruby -v: ${"ruby -v".execute().text}"
//println "ls: ${"ls".execute().text}"
//println "pwd: ${"pwd".execute().text}"

// TODO check ruby and install as in other services

// TODO install unzip

ant = new AntBuilder()

ant.copy(todir: serviceDir) {
    fileset(dir: "scalarm_simulation_manager")
}

// TODO: tworzenie pliku config.json
// z wypełnionym service_url
// {"information_service_url":"0.0.0.0:11300","experiment_manager_user":"anonymous","experiment_manager_pass":"pass123"}


// println builder.project.properties.cmdOutSim