import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmInformationService-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
serviceConfigDir = "${serviceDir}/config"

builder = new AntBuilder()

builder.exec(executable: "rake", dir: "${serviceDir}",
        outputproperty: "cmdOut",
        errorproperty: "cmdErr",
        resultproperty: "cmdExit",
        failonerror: "true") {
    arg(line: "service:start RAILS_ENV=production")
}

println "stdout:        ${builder.project.properties.cmdOut}"
