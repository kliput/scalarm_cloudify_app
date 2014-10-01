evaluate(new File("Tools.groovy")) 
def tools = new Tools()

def ant = new AntBuilder()

tools.command("nohup ruby simulation_manager.rb >sim.log 2>&1 </dev/null & echo $!")
