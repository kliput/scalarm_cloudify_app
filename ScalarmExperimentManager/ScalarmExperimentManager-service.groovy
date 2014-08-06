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
def queryPuma = "Args.0.re=puma.*unix.*scalarm_experiment_manager.sock.*"
def queryNginx = "Args.0.re=nginx.*master process nginx.*"

def pumaProcessIsRunning = {
    !ServiceUtils.ProcessUtils.getPidsWithQuery("Args.0.re=puma.*unix.*scalarm_experiment_manager.sock.*").isEmpty()
}

def httpsPortIsOccupied = {
    ServiceUtils.isPortOccupied(443)
}

def isUrlAvailable = { url ->
    def output = ["sh", "-c", "curl -k -I ${url} | head -1 | cut -d' ' -f2"].execute().text
    output.isInteger() && output.toInteger() in [200, 301]
}

def scalarmLoginPageIsAvailable = {
    isUrlAvailable("https://localhost/login")
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
            println "SD - puma: " + pumaProcessIsRunning()
            println "SD - 443 port: " + httpsPortIsOccupied()
            println "SD - url avail: " + scalarmLoginPageIsAvailable()
        
            pumaProcessIsRunning() && httpsPortIsOccupied() && scalarmLoginPageIsAvailable()
        }
        stopDetection {
            println "SD - puma: " + pumaProcessIsRunning()
            println "SD - 443 port: " + httpsPortIsOccupied()
            println "SD - url avail: " + scalarmLoginPageIsAvailable()
            
            !pumaProcessIsRunning() || !httpsPortIsOccupied() || !scalarmLoginPageIsAvailable()
        }
        
        locator {
            [queryNginx, queryPuma].inject([]) { collectedPids, query ->
                collectedPids += ServiceUtils.ProcessUtils.getPidsWithQuery(query)
            }
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

