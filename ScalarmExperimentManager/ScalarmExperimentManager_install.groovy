import groovy.transform.Field
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
serviceConfigDir = "${serviceDir}/config"
nginxDir = "${installDir}/nginx-experiment"

//println "ruby -v: ${"ruby -v".execute().text}"
//println "ls: ${"ls".execute().text}"
//println "pwd: ${"pwd".execute().text}"

// TODO check ruby and install as in other services

builder = new AntBuilder()

if (!isRubyValid()) installRvmRuby()

if (!isNginxPresent()) installNginx()

builder.mkdir(dir: nginxDir)
builder.mkdir(dir: "${nginxDir}/logs")
builder.copy(todir: nginxDir) {
    fileset(dir: "nginx-experiment")
}

// download Experiment Manager's code
builder.sequential {
	mkdir(dir: installDir)
	ServiceUtils.getDownloadUtil().get(config.downloadPath, "${installDir}/em.zip", true)
}

// TODO: scalarm_experiment_manager-master is a directory in from ZIP
// change if GIT branch changes (e.g. to master)
builder.unzip(src:"${installDir}/em.zip", dest: installDir, overwrite:true)
builder.move(file:"${installDir}/${config.serviceName}-master", tofile: serviceDir)

builder.copy(file:"scalarm.yml", todir: serviceConfigDir)
builder.copy(file:"secrets.yml", todir: serviceConfigDir)
builder.copy(file:"puma.rb", todir: serviceConfigDir)

// TODO install GIT for bundle install

tools.

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

// TODO - it needs a is url?

// install R, sysstat
// sudo apt-get -y install r-base-core sysstat

//TODO? r-cran-class r-cran-mass r-cran-nnet r-cran-spatial

// NOTICE: takes long time
builder.exec(outputproperty:"cmdOut2",
        errorproperty: "cmdErr2",
        resultproperty:"cmdExit2",
        failonerror: "true",
        dir: serviceDir,
        executable: "rake"
) {
    env(key: 'RAILS_ENV', value: 'production')
    arg(line: "service:non_digested")
}

println "service:non_digested: ${builder.project.properties.cmdOut2}"

boolean isRubyValid() {
    def p = ['sh', '-c', 'ruby -v'].execute()
    p.waitForOrKill(1000*5)
    p.exitValue() == 0 && p.text =~ /ruby 2\.1.*/
}

void installRvmRuby() {
    def command = [
        "\\curl -sSL https://get.rvm.io | bash -s stable --ruby=2.1",
        "source /home/ubuntu/.rvm/scripts/rvm"
    ].join("; ")
    
    def proc = ['sudo', 'sh', '-c', command].execute()
    proc.waitForOrKill(10*60*1000)
    println "rvm installation output: ${proc.text}"
}

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
