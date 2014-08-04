import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmSimulationManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

builder = new AntBuilder()

def simulation_manager_command = "nohup ruby simulation_manager.rb >sim.log 2>&1 </dev/null & echo $!"

builder.exec(outputproperty: "cmdOutSim",
        errorproperty: "cmdErrSim",
        dir: serviceDir,
        executable: "bash",
        failonerror: "true",
        inputstring: simulation_manager_command
)

def pid = builder.project.properties.cmdOutSim

println "PID: ${pid}"

new File("${serviceDir}/sim.pid").write(pid)

// todo: check if process with given PID is running
