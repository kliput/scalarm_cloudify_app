import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmSimulationManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

def getSimulationManagerPids = {
    ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.eq=ruby,Args.1.eq=simulation_manager.rb")
}

getSimulationManagerPids().collect { pid ->
    "kill -9 ${pid}".execute().waitFor()
}
