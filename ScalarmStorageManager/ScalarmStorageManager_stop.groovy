import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmStorageManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

ant = new AntBuilder()

ant.exec(executable: "rake", dir: "${serviceDir}", failonerror: "false") {
    arg(line: "service:stop RAILS_ENV=production")
}

def isHost = "localhost"
def isPort = "11300"

def thisHost = "localhost"
def logBankPort = "20001"

// Deregister this Storage from IS (because registering the same address causes error)
// TODO to determine
ant.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "false") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/storage/deregister")
    arg(line: "--data \"address=${thisHost}:${logBankPort}\"")
}

// Kill found nginx-storage processes
ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=nginx.*master process nginx.*nginx-storage.*").each { pid ->
    "sudo kill ${pid}".execute().waitFor()
}

println "stdout:        ${ant.project.properties.cmdOut}"
