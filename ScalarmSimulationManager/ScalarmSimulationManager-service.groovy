def getSimulationManagerPids = {
    ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.eq=ruby,Args.1.eq=simulation_manager.rb")
}

service {
    name "ScalarmSimulationManager"
    type "APP_SERVER"
    elastic true
    numInstances 3
    maxAllowedInstances 3
    
    compute {
        template "SCALARM_LINUX"
    }

    lifecycle {
        install "ScalarmSimulationManager_install.groovy"
        start "ScalarmSimulationManager_start.groovy"
        stop "ScalarmSimulationManager_stop.groovy"
        shutdown "ScalarmSimulationManager_shutdown.groovy"

        startDetection {
            !getSimulationManagerPids().isEmpty()
        }
        locator {
            getSimulationManagerPids()
        }
        stopDetection {
            getSimulationManagerPids().isEmpty()
        }
    }
    
    // TODO: SSH open only for debug puropses
    network {
        port = 11300
        protocolDescription = "SSH"
        template "APPLICATION_NET"
        accessRules {
                incoming ([
                        accessRule {
                                type "PUBLIC"
                                portRange 22
                                target "0.0.0.0/0"
                        }
                ])
        }
    }
}

		
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
