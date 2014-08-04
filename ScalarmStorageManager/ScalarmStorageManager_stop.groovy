import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmStorageManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"

builder = new AntBuilder()

builder.exec(executable: "rake", dir: "${serviceDir}", failonerror: "false") {
    arg(line: "service:stop RAILS_ENV=production")
}

println "stdout:        ${builder.project.properties.cmdOut}"
