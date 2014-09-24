evaluate(new File("Tools.groovy"))
def tools = new Tools()

def ant = new AntBuilder()

tools.killAllNginxes()

tools.deregisterExperimentManager()

tools.command("rake service:stop", tools.serviceDir, [
    'RAILS_ENV': 'production',
    'IS_URL': tools.isHost,
    'IS_USER': 'scalarm',
    'IS_PASS': 'scalarm'
])

tools.optionalCommand("rake db_router:stop", tools.serviceDir, [
    'RAILS_ENV': 'production',
    'IS_URL': tools.isHost,
    'IS_USER': 'scalarm',
    'IS_PASS': 'scalarm'
])