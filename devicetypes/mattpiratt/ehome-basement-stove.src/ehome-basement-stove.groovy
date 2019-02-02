/**
 *  eHome Basement Stove
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
    definition (name: "eHome Basement Stove", namespace: "mattPiratt", author: "Bartosz Kubek") {
        capability "Sensor"
        capability "Refresh"
        capability "Polling"
        capability "Battery"
        capability "Temperature Measurement"

        command "setTemperature", ["number", "string"]
        command "setLevel", ["number", "string"]



    }

    simulator {
    }

    tiles(scale: 2)  {

        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
            state("battery", label:'${currentValue}', unit:"% of coal",
                    backgroundColors:[
                            [value: 10, color: "#a80320"],
                            [value: 30, color: "#ffffff"]
                    ]
            )
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
        }

        valueTile("temperature", "device.temperature", width: 2, height: 2 ){
            state("temperature", label:'${currentValue}', unit:"°C",
                    backgroundColors:[
                            [value: 20, color: "#006aff"],
                            [value: 63, color: "#ffffff"],
                            [value: 85, color: "#ffffff"],
                            [value: 95, color: "#a80320"]
                    ]
            )}

        main "battery"
        details (["thermostat"])
    }
}


// parse events into attributes
def parse(String description) {
    log.debug "Basement Stove: parsing: '${description}'"
}

def setTemperature(val, zoneName) {
    sendEvent(name: 'temperature', value: val, unit: "C")
}

def setLevel(val, zoneName) {
    sendEvent(name: 'level', value: val, unit: "%")
}