evaluate(new File("Tools.groovy")) 
def tools = new Tools()
 
tools.optionalCommand('rake service:stop', tools.serviceDir, [
    'RAILS_ENV': 'production',
    'IS_URL': "${tools.getIsHost()}:${tools.config.isPort}",
    'IS_USER': tools.config.isUser,
    'IS_PASS': tools.config.isPass
])

tools.deregisterStorageManager()
tools.killAllNginxes()
