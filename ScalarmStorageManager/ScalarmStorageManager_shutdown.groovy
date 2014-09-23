evaluate(new File("Tools.groovy")) 
def tools = new Tools()

new AntBuilder().delete(dir: tools.installDir)
