import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmSimulationManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

builder = new AntBuilder()

def getSimulationManagerPids = {
    ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.eq=ruby,Args.1.eq=simulation_manager.rb")
}

getSimulationManagerPids().collect { pid ->
    "kill -9 ${pid}".execute().waitFor()
}

builder.exec(executable: "sh",
        dir: serviceDir,
        outputproperty: "cmdOutSim",
        errorproperty: "cmdErrSim",
        failonerror: "true",
) {
    arg(value: "-c")
    arg(value: "nohup ruby simulation_manager.rb >sim.log 2>&1 </dev/null & echo $!")
}

def pid = builder.project.properties.cmdOutSim
println "PID: ${pid}"
