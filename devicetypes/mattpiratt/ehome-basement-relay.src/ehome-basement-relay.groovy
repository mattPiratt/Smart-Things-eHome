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
    definition (name: "eHome Basement Relay", namespace: "mattPiratt", author: "Bartosz Kubek") {
        capability "Switch"
        command "changeSwitchState", ["string"]
    }

    simulator {
    }

    tiles(scale: 2)  {

        standardTile("switch", "device.switch", width: 6, height: 4,canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
            state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
        }

        main "switch"
        details (["switch"])
    }
}

// parse events into attributes
def parse(String description) {
    log.debug "Virtual siwtch parsing '${description}'"
}

def on() {
    log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on");
}

def off() {
    log.debug "Executing 'off'"
    sendEvent(name: "switch", value: "off");
}

def changeSwitchState(newState) {
    log.trace "Received update that this switch is now $newState"
    switch(newState) {
        case 1:
            on();
            break;
        case 0:
            off();
            break;
    }
}