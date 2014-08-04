import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
serviceConfigDir = "${serviceDir}/config"

//println "ruby -v: ${"ruby -v".execute().text}"
//println "ls: ${"ls".execute().text}"
//println "pwd: ${"pwd".execute().text}"

builder = new AntBuilder()

// download Experiment Manager's code
builder.sequential {
	mkdir(dir: installDir)
	ServiceUtils.getDownloadUtil().get(config.downloadPath, "${installDir}/master.zip", true)
}

builder.unzip(src:"${installDir}/master.zip", dest: installDir, overwrite:true)
builder.move(file:"${installDir}/${config.serviceName}-master", tofile: serviceDir)

builder.copy(file:"scalarm.yml", todir: serviceConfigDir)
builder.copy(file:"secrets.yml", todir: serviceConfigDir)
builder.copy(file:"puma.rb", todir: serviceConfigDir)

builder.exec(outputproperty:"cmdOut",
             errorproperty: "cmdErr",
             resultproperty:"cmdExit",
             failonerror: "true",
             dir: serviceDir,
             executable: "bundle"
            ) {
	arg(line: "install")
}

builder.mkdir(dir: "${serviceDir}/log")

println "bundle install: ${builder.project.properties.cmdOut}"

// TODO: compile assets disabled (assets are in package)
// builder.exec(outputproperty:"cmdOut2",
//         errorproperty: "cmdErr2",
//         resultproperty:"cmdExit2",
//         failonerror: "true",
//         dir: serviceDir,
//         executable: "rake"
// ) {
//     arg(line: "service:non_digested RAILS_ENV=production")
// }
// 
// println "service:non_digested: ${builder.project.properties.cmdOut2}"
