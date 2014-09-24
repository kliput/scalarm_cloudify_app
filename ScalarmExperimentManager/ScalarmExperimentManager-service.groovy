def queries = [
    'puma': "Args.0.re=puma.*unix.*scalarm_experiment_manager.sock.*",
    'nginx': "Args.0.re=nginx.*master process nginx.*nginx-experiment.*"
]

def agentPrivateIP() {
    System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
}

def pumaProcessIsRunning = {
    !ServiceUtils.ProcessUtils.getPidsWithQuery(queries['puma']).isEmpty()
}

def httpsPortIsOccupied = {
    ServiceUtils.isPortOccupied(agentPrivateIP(), 443)
}

def localDbRouterIsWorking = {
    ServiceUtils.isPortOccupied(agentPrivateIP(), 27017)
}

// TODO: fails sometimes - better not use
def isUrlAvailable = { url ->
    def output = ["sh", "-c", "curl -k -I ${url} | head -1 | cut -d' ' -f2"].execute().text
    output.isInteger() && output.toInteger() in [200, 301]
}

def scalarmLoginPageIsAvailable = {
    isUrlAvailable("https://${agentPrivateIP()}/login")
}

service {
	
	name "ScalarmExperimentManager"
	type "APP_SERVER"
	numInstances 1
	
	compute {
		template "SMALL_LINUX"
	}

	lifecycle {
		install "ScalarmExperimentManager_install.groovy"
		start "ScalarmExperimentManager_start.groovy"
		stop "ScalarmExperimentManager_stop.groovy"
        shutdown "ScalarmExperimentManager_shutdown.groovy"

        startDetection {
            println "Start - puma: " + pumaProcessIsRunning()
            println "Start - 443 port: " + httpsPortIsOccupied()
            println "Start - url avail: " + scalarmLoginPageIsAvailable()
            println "Start - DbR: " + localDbRouterIsWorking()
        
            pumaProcessIsRunning() && httpsPortIsOccupied() && localDbRouterIsWorking() //&& scalarmLoginPageIsAvailable()
        }
        stopDetection {
            println "Stop - puma: " + pumaProcessIsRunning()
            println "Stop - 443 port: " + httpsPortIsOccupied()
            println "Stop - url avail: " + scalarmLoginPageIsAvailable()
            println "Stop - DbR: " + localDbRouterIsWorking()
            
            !pumaProcessIsRunning() || !httpsPortIsOccupied() || !localDbRouterIsWorking() //|| !scalarmLoginPageIsAvailable()
        }
        
        locator {
            queries.values().inject([]) { collectedPids, query ->
                collectedPids += ServiceUtils.ProcessUtils.getPidsWithQuery(query)
            }
        }
        
    }
    
    network {
        port = 443
        protocolDescription = "HTTPS"
        template "APPLICATION_NET"
        accessRules {
            incoming ([
                accessRule {
                    type "PUBLIC"
                    portRange 443
                    target "0.0.0.0/0"
                },
                accessRule {
                    type "PUBLIC"
                    portRange 22
                    target "0.0.0.0/0"
                },
            ])
        }
    }
}

        
        
//		startDetection {
//            information_service_port = 11300 // context.attributes.thisInstance["port"]
//			ServiceUtils.isPortOccupied(information_service_port)
//		}
		
//		monitors {
//			try {
//				port  = context.attributes.thisInstance["port"] as int
//				mongo = new Mongo("127.0.0.1", port)
//				db = mongo.getDB("mydb")
//
//				result = db.command("serverStatus")
//				println "mongod-service.groovy: result is ${result}"
//
//				return [
//					"Current Active Connections":result.connections.current
//				]
//			}
//			finally {
//				if (null!=mongo) mongo.close()
//			}
//		}
	
//	userInterface {
//		metricGroups = ([
//			metricGroup {
//				name "MongoDB"
//				metrics([					
//					"Current Active Connections"					
//				])
//			}
//		])

//		widgetGroups = ([			
//			widgetGroup {
//				name "Current Active Connections"
//				widgets ([
//					balanceGauge{metric = "Current Active Connections"},
//					barLineChart{
//						metric "Current Active Connections"
//						axisYUnit Unit.REGULAR
//					},
//				])
//			}			
//		])
//	}
//	network {
//		port = 30001
//		protocolDescription ="HTTP"
//	}

