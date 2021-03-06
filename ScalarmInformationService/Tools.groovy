import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

public class Tools
{
    def serviceContext
    def instanceId
    def config
    def installDir
    def serviceDir
    def serviceConfigDir
    def thisHost
    
    def Tools() {
        serviceContext = ServiceContextFactory.getServiceContext()
        instanceId = serviceContext.getInstanceId()
        config = new ConfigSlurper().parse(new File("ScalarmInformationService-service.properties").toURL())
        installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceId
        serviceDir = "${installDir}/${config.serviceName}"
        serviceConfigDir = "${serviceDir}/config"
        
        new AntBuilder().mkdir(dir: installDir) // works like mkdir -p
        
        thisHost = serviceContext.isLocalCloud() ?
            InetAddress.getLocalHost().getHostAddress() : System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
        
        println "this host is: ${thisHost}"
        serviceContext.attributes.thisInstance["thisHost"] = thisHost.toString()
    }
    
    boolean isRubyValid() {
        def p = optionalCommand('ruby -v')
        
        p['exit'] == 0 && p['out'] =~ /ruby 2\.1.*/
    }

    def installCurl() {
        command("sudo apt-get -y install curl")
    }
    
    def installRvmRuby() {
        println 'installing RVM...'
        def installCmd = "\\curl -sSL https://get.rvm.io | bash -s stable --ruby=2.1"
        command(installCmd)
    }

    def command(command, dir=installDir, envs=[], failonerror=true) {
        execute('bash', dir, failonerror, ['--login', '-c', command], envs)
    }

    def optionalCommand(cmd, dir=installDir, envs=[]) {
        command(cmd, dir, envs, false)
    }

    def execute(executable, dir, failonerror, args=[], envs=[]) {
        def cmd = "${executable} ${args.join(' ')}"
        println "executing: '${cmd}' in ${dir}"
        
        def ant = new AntBuilder()
        try {
            ant.exec(
                executable: executable,
                dir: dir,
                failonerror: failonerror,
                
                outputproperty: 'out',
                errorproperty: 'err',
                resultproperty: 'result'
            ) {
                args.each() { arg(value: it) }
                envs.each() { k, v -> env(key: k, value: v) }
            }
        } finally {
            println "finished: '${cmd}' with exit code ${ant.project.properties.result}"
            println "- stdout: ${ant.project.properties.out}"
            println "- stderr: ${ant.project.properties.err}"
        }
        return [
            'out': ant.project.properties.out,
            'err': ant.project.properties.err, 
            'exit': Integer.parseInt(ant.project.properties.result)
        ]
    }
    
    def main() {}
}
