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

service {
	
	name "ScalarmStorageManager"
	type "APP_SERVER"
	numInstances 1
	
	compute {
		template "SMALL_LINUX"
	}

	lifecycle {
		install "ScalarmStorageManager_install.groovy"
		start "ScalarmStorageManager_start.groovy"
		stop "ScalarmStorageManager_stop.groovy"
        shutdown "ScalarmStorageManager_shutdown.groovy"
        startDetection {
            ServiceUtils.arePortsOccupied(new ArrayList(ports.values())) &&
                !ServiceUtils.ProcessUtils.getPidsWithQuery(queries['thin']).isEmpty()
        }
        locator {
            queries.values().inject([]) { collectedPids, query ->
                collectedPids += ServiceUtils.ProcessUtils.getPidsWithQuery(query)
            }
        }
        stopDetection {
            // TODO: change port 20000 check to checking log_bank process and nginx port (maybe 20001?)
            ports.values().any { port ->
                ServiceUtils.isPortFree(port)
            } && ServiceUtils.ProcessUtils.getPidsWithQuery(queries['thin']).isEmpty()
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
