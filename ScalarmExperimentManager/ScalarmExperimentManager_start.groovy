import org.cloudifysource.dsl.utils.ServiceUtils;

evaluate(new File("Tools.groovy"))
def tools = new Tools()

def nginxDir = "${tools.installDir}/nginx-experiment"

def ant = new AntBuilder()

tools.optionalCommand("rake service:stop", tools.serviceDir, [
    'RAILS_ENV': 'production',
    'IS_URL': tools.getIsHost(),
    'IS_USER': 'scalarm',
    'IS_PASS': 'scalarm'
])


// test for local development purposes - mongodb router could be already launched
if (!ServiceUtils.isPortOccupied(27017)) {

    ant.chmod(
        file: "${tools.serviceDir}/bin/mongos",
        perm: "a+x"
    )
    
    tools.command("rake db_router:start", tools.serviceDir, [
        'RAILS_ENV': 'production',
        'IS_URL': tools.getIsHost(),
        'IS_USER': 'scalarm',
        'IS_PASS': 'scalarm'
    ])
}

// Start EM
// TODO: błąd, jeśli puma jest już uruchomiona, to rake service:start próbuje się uruchomić i pada z exitcode = 1
// proces rake zawisa (nie wiadomo dlaczego)
tools.command("rake service:start", tools.serviceDir, [
    'RAILS_ENV': 'production',
    'IS_URL': tools.getIsHost(),
    'IS_USER': 'scalarm',
    'IS_PASS': 'scalarm'
])

// TODO create single user in Scalarm

// TODO ---- zmiany w samym Scalarmie
// - tryb cloudify - single user login (pomijanie login screen i logowanie od razu na użytkownika podanego w konfiguracji, ew. podanie hasła), use only anonymous
// single user mode -> całkowite pominięcie uwierzytelniania (login screen)  
// przenieść do zewnętrznego pliku konfiguracyjnego ustawianie, które infrastrutury mają być dostępne?
// TODO patch scalarm to support single-user installation


tools.killAllNginxes()
tools.command("sudo nginx -c nginx.conf -p ${nginxConfigDir}")

// TODO: fail if not {"status":"ok"...}
// assert ant.project.properties.cmdOut1 ==~ /.*"status":"ok".*/

// Deregister this EM from IS (because registering the same address causes error)
tools.deregisterExperimentManager()
tools.registerExperimentManager()

println "[OK] Scalarm EM and nginx are launched as daemons"
