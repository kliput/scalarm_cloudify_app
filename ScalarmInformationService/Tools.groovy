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
    
    def Tools() {
        serviceContext = ServiceContextFactory.getServiceContext()
        instanceId = serviceContext.getInstanceId()
        config = new ConfigSlurper().parse(new File("ScalarmInformationService-service.properties").toURL())
        installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceId
        serviceDir = "${installDir}/${config.serviceName}"
        serviceConfigDir = "${serviceDir}/config"
        
        new AntBuilder().mkdir(dir: installDir) // works like mkdir -p
        
        // Only for debug purposes
//         println "install dir: ${installDir}"
//         println "serviceDir: ${serviceDir}"
//         println "serviceConfigDir: ${serviceConfigDir}"
// 
//         println "ruby -v: ${"ruby -v".execute().text}"
//         println "ls: ${"ls".execute().text}"
//         println "pwd: ${"pwd".execute().text}"
    }
    
    boolean isRubyValid() {
        def p = command('ruby -v')
        
        p['exit'] == 0 && p['out'] =~ /ruby 2\.1.*/
    }

    def installRvmRuby() {
        println 'installing RVM...'
        def cmd = [
            "\\curl -sSL https://get.rvm.io | bash -s stable --ruby=2.1",
            "source ${System.properties['user.home']}/.rvm/scripts/rvm"
        ].join("; ")
        
        command(cmd)
    }

    def command(command, dir=installDir, failonerror=true) {
        execute('sh', dir, failonerror, ['-c', command])
    }

    def optionalCommand(command, dir=installDir) {
        command(command, dir, false)
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
            'exit': ant.project.properties.result
        ]
    }
    
    def main() {}
}
