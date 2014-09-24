evaluate(new File("Tools.groovy"))
def tools = new Tools()

def ant = new AntBuilder()

ant.delete(dir: tools.installDir)
