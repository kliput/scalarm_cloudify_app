application {
    name="Scalarm"

    service {
        name = "ScalarmInformationService"
    }

    service {
        name = "ScalarmStorageManager"
        dependsOn = [ "ScalarmInformationService" ]
    }

    service {
        name = "ScalarmExperimentManager"
        dependsOn = [ "ScalarmInformationService", "ScalarmStorageManager" ]
    }
    
//     service {
//         name = "ScalarmSimulationManager"
//         dependsOn = [
//             "ScalarmInformationService", "ScalarmStorageManager",
//             "ScalarmExperimentManager"
//         ]
//     }
}
