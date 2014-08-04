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
//             ServiceUtils.isPortOccupied(3005) // puma via http
//             ServiceUtils.isPortOccupied(3001) // nginx
            // todo: przenieść do osobnego skryptu, żeby było można zmieniać ręcznie
            // todo: wykrywać, czy działa proces -> jak jest puma, to raczej działa, bo najpierw jest rake
            // później można wykrywać, czy istnieje plik socketu
        }
        locator {
//             ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=puma.*tcp.*3005")
            ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=puma.*unix.*scalarm_experiment_manager.sock.*")
        }
        stopDetection {
//             ServiceUtils.isPortFree(3005) // puma via http
//             ServiceUtils.isPortOccupied(3001) // nginx
            // todo: jak w start detection: wykrywać czy proces został zamknięty lub nie ma sock
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
	}
	
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
