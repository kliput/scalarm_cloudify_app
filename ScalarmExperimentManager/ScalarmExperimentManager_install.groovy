import groovy.transform.Field
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
serviceConfigDir = "${serviceDir}/config"
nginxDir = "${installDir}/nginx"

//println "ruby -v: ${"ruby -v".execute().text}"
//println "ls: ${"ls".execute().text}"
//println "pwd: ${"pwd".execute().text}"

builder = new AntBuilder()

if (!isNginxPresent()) installNginx()

builder.mkdir(dir: nginxDir)
builder.mkdir(dir: "${nginxDir}/logs")
builder.copy(todir: nginxDir) {
    fileset(dir: "nginx")
}

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
// TODO: please uncomment if using fresh package from git
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

def executeCommand(command) {
    def ant = AntBuilder()

    ant.exec(outputproperty:"cmdOut",
            errorproperty: "cmdErr",
            resultproperty:"cmdExit",
            executable: "sh"
    ) {
        arg(value: "-c")
        arg(value: command)
    }

    [
            "stdout": ant.project.properties.cmdOut,
            "stderr": ant.project.properties.cmdErr,
            "exitcode": ant.project.properties.cmdExit
    ]
}
