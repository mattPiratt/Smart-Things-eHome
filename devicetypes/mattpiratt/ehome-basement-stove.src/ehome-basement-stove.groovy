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
        capability "Battery"
        capability "Temperature Measurement"
        capability "Switch"
        command "changeSwitchState", ["string"]
        command "setTemperature", ["number"]
        command "setLevel", ["number"]
    }

    // simulator metadata
    simulator {
    }

    tiles(scale: 2)  {

        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 6, height: 4,canChangeIcon: true) {
            state("battery", label:'${currentValue}% \n coal', unit:"%", icon:"st.Seasonal Winter.seasonal-winter-009",
                    backgroundColors:[
                            [value: 10, color: "#a80320"],
                            [value: 30, color: "#50C878"]
                    ]
            )
        }

        standardTile("temperature", "device.temperature", decoration: "flat", width: 2, height: 2 ){
            state("temperature", label:'${currentValue}', unit:"°C", icon:"st.Weather.weather2",
                    backgroundColors:[
                            [value: 20, color: "#006aff"],
                            [value: 63, color: "#ffffff"],
                            [value: 85, color: "#ffffff"],
                            [value: 95, color: "#a80320"]
                    ]
            )}

        standardTile("coalLvlReset2", "device.switch", width: 2, height: 2,canChangeIcon: true) {
            state "on", label: 'Reset coal level', action: "switch.off",
                    icon:"st.secondary.refresh-icon", backgroundColor: "#ffffff", defaultState: true
            state "off", label: 'Reset coal level', action: "switch.on",
                    icon:"st.secondary.refresh-icon", backgroundColor: "#ffffff", defaultState: true

        }

        main "battery"
        details (["battery","temperature","coalLvlReset", "coalLvlReset2"])
    }
}

// parse events into attributes
def parse(String description) {
    log.debug "Basement Stove: parsing: '${description}'"
}

def setTemperature(val) {
    log.debug "Basement Stove: setTemperature: '${val}'"
    sendEvent(name: 'temperature', value: val, unit: "C")
}

def setLevel(val) {
    log.debug "Basement Stove: setLevel: '${val}'"
    sendEvent(name: 'battery', value: val, unit: "%", descriptionText: "Coal level is ${val}%.")

}


def on() {
    log.debug "Executing 'on', but in real: Basement Stove: reset."
    sendEvent(name: "switch", value: "on");
}

def off() {
    log.debug "Executing 'off', but in real: Basement Stove: reset."
    sendEvent(name: "switch", value: "off");
}

def changeSwitchState(newState) {
    log.trace "Received update that this sotve should have coal level being reset (fake state: ${newState})"
    switch(newState) {
        case 1:
            on();
            break;
        case 0:
            off();
            break;
    }
}


