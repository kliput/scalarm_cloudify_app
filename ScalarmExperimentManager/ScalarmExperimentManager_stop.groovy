import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

builder = new AntBuilder()

isHost = "localhost"
isPort = "11300"

enHost = "localhost" // this host
enPort = "443"

// Kill found nginx-storage processes
ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=nginx.*master process nginx.*nginx-experiment.*").each { pid ->
    "sudo kill ${pid}".execute().waitFor()
}

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

builder.exec(executable: "rake", dir: serviceDir,
        outputproperty: "cmdOut",
        errorproperty: "cmdErr",
        resultproperty: "cmdExit",
        failonerror: "true") {
    arg(line: "db_router:stop RAILS_ENV=production")
}

