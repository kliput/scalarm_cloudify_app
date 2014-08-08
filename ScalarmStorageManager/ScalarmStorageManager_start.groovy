import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmStorageManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
nginxConfigDir = "${installDir}/nginx-storage"

builder = new AntBuilder()

def isHost = "localhost"
def isPort = "11300"

def thisHost = "localhost"
def logBankPort = "20001"

// in case if one of service parts is still running after some failure
builder.exec(executable: "rake", dir: "${serviceDir}", failonerror: "false") {
    arg(line: "service:stop RAILS_ENV=production")
}

builder.exec(executable: "rake", dir: "${serviceDir}",
        outputproperty: "cmdOut",
        errorproperty: "cmdErr",
        resultproperty: "cmdExit",
        failonerror: "false") {
    arg(line: "service:start RAILS_ENV=production")
}


// Kill found nginx-storage processes
ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=nginx.*master process nginx.*nginx-storage.*").each { pid ->
    "sudo kill ${pid}".execute().waitFor()
}

// Launch nginx
println "Launching nginx..."
builder.exec(executable: "sh", dir: installDir,
        outputproperty: "nginxOut",
        errorproperty: "nginxErr",
        resultproperty: "nginxExit",
        failonerror: "true") {
    arg(value: "-c")
    arg(value: "sudo nginx -c ${nginxConfigDir}/nginx.conf -p ${nginxConfigDir}")
}

println "nginx exit:          ${builder.project.properties.nginxExit}"
println "nginx stdout:        ${builder.project.properties.nginxOut}"
println "nginx stderr:        ${builder.project.properties.nginxErr}"


// Deregister this Storage from IS (because registering the same address causes error)
// TODO to determine
builder.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "false") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/storage/deregister")
    arg(line: "--data \"address=${thisHost}:${logBankPort}\"")
}

// Register Storage in IS
builder.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "true") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/storage/register")
    arg(line: "--data \"address=${thisHost}:${logBankPort}\"")
}


println "stdout:        ${builder.project.properties.cmdOut}"
