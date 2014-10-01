evaluate(new File("Tools.groovy")) 
def tools = new Tools()

tools.command("sudo apt-get -y install zip")

if (!tools.isRubyValid()) tools.installRvmRuby()

ant = new AntBuilder()

ant.copy(todir: serviceDir) {
    fileset(dir: "scalarm_simulation_manager")
}

def configStr = "{\"information_service_url\":\"${tools.isHost}:${tools.config.isPort}\",\"experiment_manager_user\":\"${tools.config.emUser}\",\"experiment_manager_pass\":\"${tools.config.emPassword}\"}"

new File("${tools.serviceDir}/config.json").withWriter { out ->
    out.writeLine(configStr)
}
