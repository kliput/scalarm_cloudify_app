import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("ScalarmExperimentManager-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
serviceDir = "${installDir}/${config.serviceName}"
nginxConfigDir = "${installDir}/nginx-experiment"

// -----------

ant = new AntBuilder()

// TODO
isHost = "localhost"
isPort = "11300"

// TODO
enHost = "localhost" // this host
enPort = "443"

// Deregister this EM from IS (because registering the same address causes error)
// TODO to determine
ant.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "true") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/experiments/deregister")
    arg(line: "--data \"address=${enHost}:${enPort}\"")
}

// Destroy if EM is already running
ant.exec(executable: "rake", dir: serviceDir,
        outputproperty: "cmdOut",
        errorproperty: "cmdErr",
        resultproperty: "cmdExit",
        failonerror: "false")
{
    env(key: 'RAILS_ENV', value: 'production')
    env(key: 'IS_URL', value: isHost)
    env(key: 'IS_USER', value: 'scalarm')
    env(key: 'IS_PASS', value: 'scalarm') // TODO: is_user and password to config
    arg(line: "service:stop")
}


// test for local development purposes - mongodb router could be already launched
if (!ServiceUtils.isPortOccupied(27017)) {

    ant.chmod(
        file: "${serviceDir}/bin/mongos",
        perm: "a+x"
    )
    
    try {
        // Start local db_router
        ant.exec(executable: "rake", dir: serviceDir,
                outputproperty: "dbrOut",
                errorproperty: "dbrErr",
                resultproperty: "dbrExit",
                failonerror: "false")
        {
            env(key: 'RAILS_ENV', value: 'production')
            arg(line: "db_router:start RAILS_ENV=production")
            // TODO: use IS env variables
        }
    } catch (Exception e) {
        throw e;
    } finally {
        println "rake db_router:start out: ${ant.project.properties.dbrOut}"
        println "rake db_router:start err: ${ant.project.properties.dbrErr}"
        println "rake db_router:start exit: ${ant.project.properties.dbrExit}"
    }    
}

// Start EM
// TODO: błąd, jeśli puma jest już uruchomiona, to rake service:start próbuje się uruchomić i pada z exitcode = 1
// proces rake zawisa (nie wiadomo dlaczego)
ant.exec(executable: "rake", dir: serviceDir,
        outputproperty: "cmdOut",
        errorproperty: "cmdErr",
        resultproperty: "cmdExit",
        failonerror: "true")
{
    env(key: 'RAILS_ENV', value: 'production')
    arg(line: "service:start")
    // TODO: use IS env variables
}

// TODO create single user in Scalarm

// TODO ---- zmiany w samym Scalarmie
// - tryb cloudify - single user login (pomijanie login screen i logowanie od razu na użytkownika podanego w konfiguracji, ew. podanie hasła), use only anonymous
// single user mode -> całkowite pominięcie uwierzytelniania (login screen)  
// przenieść do zewnętrznego pliku konfiguracyjnego ustawianie, które infrastrutury mają być dostępne?
// TODO patch scalarm to support single-user installation

println "rake service:start: ${ant.project.properties.cmdOut}"
println "puma real PID: " + ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=puma.*unix.*scalarm_experiment_manager.sock.*")


// Kill found nginx-storage processes
ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=nginx.*master process nginx.*nginx-experiment.*").each { pid ->
    "sudo kill ${pid}".execute().waitFor()
}

// Launch nginx
println "Launching nginx..."
ant.exec(executable: "sh", dir: installDir,
        outputproperty: "nginxOut",
        errorproperty: "nginxErr",
        resultproperty: "nginxExit",
        failonerror: "true") {
    arg(value: "-c")
    arg(value: "sudo nginx -c ${nginxConfigDir}/nginx.conf -p ${nginxConfigDir}")
}

println "nginx exit:          ${ant.project.properties.nginxExit}"
println "nginx stdout:        ${ant.project.properties.nginxOut}"
println "nginx stderr:        ${ant.project.properties.nginxErr}"



try {
    println "nginx PID from file: " + new File("${nginxConfigDir}/nginx.pid").text
} catch (Exception e) {
    println "caught exception on ngnix pid read: " + e.toString()
}


println "nginx real PID: " + ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=nginx.*master process nginx.*")

// TODO: fail if not {"status":"ok"...}
// assert ant.project.properties.cmdOut1 ==~ /.*"status":"ok".*/




// Register EM in IS
ant.exec(executable: "curl",
        outputproperty: "cmdOut1",
        failonerror: "true") {
    arg(line: "--user scalarm:scalarm")
    arg(line: "-k -X POST https://${isHost}:${isPort}/experiments/register")
    arg(line: "--data \"address=${enHost}:${enPort}\"")
}


println "[OK] Scalarm EM and nginx are launched as daemons"

