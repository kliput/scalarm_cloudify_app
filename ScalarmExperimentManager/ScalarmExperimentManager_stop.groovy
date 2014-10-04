evaluate(new File("Tools.groovy"))
def tools = new Tools()

def ant = new AntBuilder()

tools.killAllNginxes()

tools.deregisterExperimentManager()

tools.commandProduction("rake service:stop")

tools.optionalCommandProduction("rake db_router:stop")