// TODO: port specified in config
service {
        
        name "ScalarmInformationService"
        type "LOAD_BALANCER"
        numInstances 1
        
        compute {
            template "SCALARM_LINUX"
        }

        lifecycle {
            install "ScalarmInformationService_install.groovy"
            start "ScalarmInformationService_start.groovy"
            startDetection {
                ServiceUtils.isPortOccupied(11300)
                // TODO: also can check with query to https://<ip>:<port>/experiments/list
                // and except []
            }
            locator {
                ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=thin server.*11300.*")
            }
            stopDetection {
                ServiceUtils.isPortFree(11300)
            }
            stop "ScalarmInformationService_stop.groovy"
            shutdown "ScalarmInformationService_shutdown.groovy"
        }
        
//         network {
//             port = 11300
//             protocolDescription = "HTTPS"
//             template "APPLICATION_NET"
//             accessRules {
//                     incoming ([
//                             accessRule {
//                                     type "PUBLIC"
//                                     portRange 11300
//                                     target "0.0.0.0/0"
//                             },
//                             accessRule {
//                                     type "PUBLIC"
//                                     portRange 22
//                                     target "0.0.0.0/0"
//                             }
//                     ])
//             }
//         }
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

//    plugins([
//            plugin {
//                name "portLiveness"
//                className "org.cloudifysource.usm.liveness.PortLivenessDetector"
//                config ([
//                        "Port" : [11300],
//                        "TimeoutInSeconds" : 10,
//                        "Host" : "127.0.0.1"
//                ])
//            }
//    ])
//
//    userInterface {
//
//        metricGroups = ([
//        ]
//        )
//
//        widgetGroups = ([]
//        )
//    }
	
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

