/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
// TODO: port specified in config
service {
	
	name "ScalarmInformationService"
	type "APP_SERVER"
	numInstances 1
	
	compute {
		template "SMALL_LINUX"
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
	}

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
}
