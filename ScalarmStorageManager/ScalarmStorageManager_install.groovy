import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmStorageManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
serviceConfigDir = "${serviceDir}/config"
nginxDir = "${installDir}/nginx-storage"

println "scalarm-sm-install.groovy: starting ..."

println "ruby -v: ${"ruby -v".execute().text}"
println "ls: ${"ls".execute().text}"
println "pwd: ${"pwd".execute().text}"

builder = new AntBuilder()

if (!isNginxPresent()) installNginx()

builder.mkdir(dir: nginxDir)
builder.mkdir(dir: "${nginxDir}/logs")
builder.copy(todir: nginxDir) {
    fileset(dir: "nginx-storage")
}


// download Simulation Manager's code
builder.sequential {
	mkdir(dir:"${installDir}")
	ServiceUtils.getDownloadUtil().get("${config.downloadPath}", "${installDir}/master.zip", true)
}

builder.unzip(src:"${installDir}/master.zip", dest:"${installDir}", overwrite:true)
builder.move(file:"${installDir}/${config.serviceName}-master", tofile: serviceDir)

builder.copy(file:"scalarm.yml", todir:"${serviceConfigDir}")
builder.copy(file:"secrets.yml", todir:"${serviceConfigDir}")
builder.copy(file:"thin.yml", todir:"${serviceConfigDir}")

// download MongoDB's code
builder.sequential {
    mkdir(dir:"${installDir}")
    ServiceUtils.getDownloadUtil().get("${config.mongodbDownloadUrl}", "${installDir}/mongodb.tgz", true)
}

builder.exec(outputproperty:"cmdOut",
        errorproperty: "cmdErr",
        resultproperty:"cmdExit",
        failonerror: "true",
        dir: "${installDir}",
        executable: "tar"
) {
    arg(line: "xzvf mongodb.tgz")
}

// OpenSSL: error:14077410:SSL routines:SSL23_GET_SERVER_HELLO:sslv3 alert handshake failure

println "stdout:        ${builder.project.properties.cmdOut}"

// TODO: mongo version in directory name
builder.move(file:"${installDir}/mongodb-${config.osName}-x86_64-${config.mongodbVersion}", tofile: "${serviceDir}/mongodb")


builder.exec(outputproperty:"cmdOut2",
             errorproperty: "cmdErr2",
             resultproperty:"cmdExit2",
             failonerror: "true",
             dir: serviceDir,
             executable: "bundle"
            ) {
	arg(value: "install")
}

println "stdout2:        ${builder.project.properties.cmdOut2}"

boolean isNginxPresent() {
    def p = ['sh', '-c', 'nginx -v'].execute()
    p.waitForOrKill(1000*5)
    p.exitValue() == 0
}

void installNginx() {
    def command = [
        "add-apt-repository -y ppa:nginx/stable",
        "apt-get update",
        "apt-get install -y nginx"
    ].join("; ")
    
    def proc = ['sudo', 'sh', '-c', command].execute()
    proc.waitForOrKill(10*60*1000)
    println "nginx installation output: ${proc.text}"
}

