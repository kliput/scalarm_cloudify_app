import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

// -----------

ant = new AntBuilder()

isHost = "localhost"
isPort = "11300"

enHost = "localhost" // this host
enPort = "3005"

// TODO to determine
ant.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "true") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/experiments/deregister")
    arg(line: "--data \"address=${enHost}:${enPort}\"")
}

ant.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "true") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/experiments/register")
    arg(line: "--data \"address=${enHost}:${enPort}\"")
}

ant.exec(executable: "rake", dir: serviceDir,
        outputproperty: "cmdOut",
        errorproperty: "cmdErr",
        resultproperty: "cmdExit",
        failonerror: "true") {
    arg(line: "service:start RAILS_ENV=production")
}


// TODO: fail if not {"status":"ok"...}
// assert ant.project.properties.cmdOut1 ==~ /.*"status":"ok".*/

println "stdout:        ${ant.project.properties.cmdOut}"
