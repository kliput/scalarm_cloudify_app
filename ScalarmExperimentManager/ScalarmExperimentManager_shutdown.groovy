import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"] + "/.cloudify/${config.serviceName}" + instanceID

new AntBuilder().delete(dir: installDir)