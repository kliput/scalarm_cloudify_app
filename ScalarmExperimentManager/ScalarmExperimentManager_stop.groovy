import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

builder = new AntBuilder()

builder.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "true") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/experiments/deregister")
    arg(line: "--data \"address=${enHost}:${enPort}\"")
}

builder.exec(executable: "rake", dir: serviceDir,
        outputproperty: "cmdOut",
        errorproperty: "cmdErr",
        resultproperty: "cmdExit",
        failonerror: "true") {
    arg(line: "service:stop RAILS_ENV=production")
}

println "stdout:        ${builder.project.properties.cmdOut}"
