evaluate(new File("Tools.groovy")) 
def tools = new Tools()

tools.command('RAILS_ENV=production rake service:start', tools.serviceDir)
