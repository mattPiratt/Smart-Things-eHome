/**
 *  eHome Basement Relay
 *
 *  Copyright 2018 Bartosz Kubek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
    definition (name: "eHome Basement Thermometer", namespace: "mattPiratt", author: "Bartosz Kubek") {
        capability "Temperature Measurement"

        command "setTemperature", ["number", "string"]
    }

    simulator {
    }

//    tiles {
//
//        valueTile("temperature", "device.temperature") {
//            state "temperature", label:'${currentValue}°', icon:"st.Weather.weather2",backgroundColors:[
//                            [value: 31, color: "#153591"],
//                            [value: 44, color: "#1e9cbb"],
//                            [value: 59, color: "#90d2a7"],
//                            [value: 74, color: "#44b621"],
//                            [value: 84, color: "#f1d801"],
//                            [value: 95, color: "#d04e00"],
//                            [value: 96, color: "#bc2323"]
//                    ]
//        }
//
//        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
//            state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
//        }
//
//        main "temperature"
//        details (["temperature"])
//    }

    tiles(scale: 2)  {

        multiAttributeTile(name: "thermostat", width: 6, height: 2, type:"thermostat") {
            tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeBackground: true){
                attributeState "default", label: '${currentValue}°', unit:"C", backgroundColors: [
                        // Celsius Color Range
                        [value: 0, color: "#50b5dd"],
                        [value: 18, color: "#43a575"],
                        [value: 20, color: "#c5d11b"],
                        [value: 24, color: "#f4961a"],
                        [value: 27, color: "#e75928"],
                        [value: 30, color: "#d9372b"],
                        [value: 32, color: "#b9203b"]
                ]}
            tileAttribute ("zoneName", key: "SECONDARY_CONTROL") {
                attributeState "zoneName", label:'${currentValue}'
            }
        }

        main "thermostat"
        details (["thermostat"])
    }
}

//// parse events into attributes
//def parse(String description) {
//    log.debug "Virtual Thermometer parsing '${description}'"
//}
//
//
//def changeThermometerState(newState) {
//    log.trace "Received update that this Thermometer is now $newState"
////    switch(newState) {
////        case 1:
////            sendEvent(name: "switch", value: "on")
////            break;
////        case 0:
////            sendEvent(name: "switch", value: "off")
////            break;
////    }
//}

// parse events into attributes
def parse(String description) {
    log.debug "Virtual temperature parsing '${description}'"
}

def setTemperature(val, zoneName) {
    sendEvent(name: 'temperature', value: val, unit: "C")
    sendEvent(name: 'zoneName', value: zoneName)
}