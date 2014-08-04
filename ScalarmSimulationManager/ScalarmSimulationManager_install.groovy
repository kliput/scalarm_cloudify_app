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

ant = new AntBuilder()

ant.copy(todir: serviceDir) {
    fileset(dir: "scalarm_simulation_manager")
}



// println builder.project.properties.cmdOutSim