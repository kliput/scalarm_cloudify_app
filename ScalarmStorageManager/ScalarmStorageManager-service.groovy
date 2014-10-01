def queries = [
    'mongos': "Args.0.re=./mongos,Args.1.re=--bind_ip",
    'mongod_shard': "Args.0.re=./mongod,Args.1.re=--shardsvr",
    'mongod_config': "Args.0.re=./mongod,Args.1.re=--configsvr",
    'thin': "Args.0.re=thin server.*scalarm_storage_manager.sock.*",
    'nginx': "Args.0.re=nginx.*master process nginx.*nginx-storage.*"
]

def ports = [
    'mongos': 27017,
    'mongod_shard': 30000,
    'mongod_config': 28000,
    'nginx': 20001
]


def agentPrivateIP() {
    System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
}

// TODO: don't know why this does not work...
// warning: ruby must be already installed (it should be invoked after service installation)
def rubyPrivateIP() {
    def ant = new AntBuilder()
    ant.exec(executable: 'bash',
        outputproperty: 'out'
    ) {
        arg(line: '--login -c')
        arg(value: "ruby -e 'require \"socket\"; puts UDPSocket.open {|s| s.connect(\"64.233.187.99\", 1); s.addr.last}'")
    }
    
    ant.project.properties.out
}

def privateIP() {
    // todo
}

service {
    name "ScalarmStorageManager"
    type "NOSQL_DB"
    numInstances 1

    compute {
        template "SCALARM_LINUX"
    }

    lifecycle {
        install "ScalarmStorageManager_install.groovy"
        start "ScalarmStorageManager_start.groovy"
        stop "ScalarmStorageManager_stop.groovy"
        shutdown "ScalarmStorageManager_shutdown.groovy"
        startDetection {
            def privateIP = agentPrivateIP()
            println "This host is: ${privateIP}"
        
            ports.each { p ->
                println "CHECK ${p.key}-${p.value}: ${ServiceUtils.isPortOccupied(privateIP, p.value)}"
            }
        
            ports.values().every { port ->
                ServiceUtils.isPortOccupied(privateIP, port)
            } && !ServiceUtils.ProcessUtils.getPidsWithQuery(queries['thin']).isEmpty()
        }
        locator {
            queries.values().inject([]) { collectedPids, query ->
                collectedPids += ServiceUtils.ProcessUtils.getPidsWithQuery(query)
            }
        }
        stopDetection {
            def privateIP = agentPrivateIP()
            ports.values().any { port ->
               ServiceUtils.isPortFree(privateIP, port)
            } && ServiceUtils.ProcessUtils.getPidsWithQuery(queries['thin']).isEmpty()
        }
    }
        
    // TODO: check required port access types
//     network {
//         port = ports['nginx']
//         protocolDescription = "HTTPS"
//         template "APPLICATION_NET"
//         accessRules {
//             incoming ([
//                 accessRule {
//                     type "PUBLIC"
//                     portRange 22
//                     target "0.0.0.0/0"
//                 },
//                 accessRule {
//                     type "PUBLIC"
//                     portRange ports['mongos']
//                     target "0.0.0.0/0"
//                 },
//                 accessRule {
//                     type "PUBLIC"
//                     portRange ports['mongod_shard']
//                     target "0.0.0.0/0"
//                 },
//                 accessRule {
//                     type "PUBLIC"
//                     portRange ports['mongod_config']
//                     target "0.0.0.0/0"
//                 },
//                 accessRule {
//                     type "PUBLIC"
//                     portRange ports['nginx']
//                     target "0.0.0.0/0"
//                 }
//             ])
//         }
//     }
    
    /*
    network {
        template "APPLICATION_NET"
        accessRules {[
            incoming ([
                accessRule {
                    type "APPLICATION"
                    portRange 27017
                    target "0.0.0.0/0"
                },
                accessRule {
                    type "APPLICATION"
                    portRange "1-40000"
                    target "0.0.0.0/0"
                }             
            ]),
            outgoing ([
                accessRule {
                    type "PUBLIC"
                    portRange "8443"
                    target "0.0.0.0/0"
                },
                accessRule {
                    type "APPLICATION"
                    portRange "1-40000"
                    target "0.0.0.0/0"
                }
            ])
        ]}
    }*/
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
