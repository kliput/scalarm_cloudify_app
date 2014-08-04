import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmInformationService-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"] + "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
serviceConfigDir = "${serviceDir}/config"

println "Install dir: ${installDir}"
println "serviceDir: ${serviceDir}"
println "serviceConfigDir: ${serviceConfigDir}"

println "scalarm-is-install.groovy: Writing mongos port to this instance(${instanceID}) attributes..."

println "ruby -v: ${"ruby -v".execute().text}"
println "ls: ${"ls".execute().text}"
println "pwd: ${"pwd".execute().text}"

builder = new AntBuilder()

builder.sequential {
    mkdir(dir: "${installDir}")
    ServiceUtils.getDownloadUtil().get("${config.downloadPath}", "${installDir}/master.zip", true)
    unzip(src: "${installDir}/master.zip", dest: "${installDir}", overwrite: true)
    move(file: "${installDir}/${config.serviceName}-master", tofile: "${serviceDir}")
    
    copy(file: "secrets.yml", todir: "${serviceConfigDir}")
    copy(file: "scalarm-cert.pem", todir: "${serviceConfigDir}")
    copy(file: "scalarm-cert-key.pem", todir: "${serviceConfigDir}")
    copy(file: "thin.yml", todir: "${serviceConfigDir}")
    
    exec(outputproperty: "installOut",
        errorproperty: "installErr",
        resultproperty: "installExit",
        failonerror: "true",
        dir: "${serviceDir}",
        executable: "bundle"
    ) {
        arg(value: "install")
    }
    
    println "bundle install stdout:        ${builder.project.properties.installOut}"
    
    exec(outputproperty: "rakeMigrateProdOut",
        errorproperty: "rakeMigrateProdErr",
        resultproperty: "rakeMigrateProdExit",
        failonerror: "true",
        dir: "${serviceDir}",
        executable: "rake"
    ) {
        arg(line: "db:migrate RAILS_ENV=production")
    }
    
    println "stdout:        ${builder.project.properties.rakeMigrateProdOut}"
    
    exec(outputproperty: "rakeMigrateOut",
        errorproperty: "rakeMigrateErr",
        resultproperty: "rakeMigrateExit",
        failonerror: "true",
        dir: "${serviceDir}",
        executable: "rake"
    ) {
        arg(line: "db:migrate")
    }
    
    println "stdout:        ${builder.project.properties.rakeMigrateOut}"
}

