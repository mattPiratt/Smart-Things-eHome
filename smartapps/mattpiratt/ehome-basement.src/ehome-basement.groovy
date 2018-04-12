/**
 *  eHome pyServer Manager
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
definition(
        name: "eHome Basement",
        namespace: "mattPiratt",
        author: "Bartosz Kubek",
        description: "Smart App for eHome basement controller.",
        category: "Safety & Security",
        iconUrl: "http://download.easyicon.net/png/1161404/64/",
        iconX2Url: "http://download.easyicon.net/png/1161404/128/",
        iconX3Url: "http://download.easyicon.net/png/1161404§   /128/")


preferences {

    section("eHome pyServer Setup"){
        input "piIP", "text", "title": "pyServer address", multiple: false, required: true
        input "piPort", "text", "title": "pyServer Port", multiple: false, required: true
        input "theHub", "hub", title: "On which hub?", multiple: false, required: true
    }
}

def getRelaysConfig() {
    return [
            "runningWaterPump": [name: "Hot water pump", defaultState: "off"],
            "floorHeatingPump": [name: "Floor heating pump", defaultState: "on"],
            "radiatorsPump": [name: "Radiators pump", defaultState: "on"],
    ]
};


def installed() {
    log.debug "installed(): with settings: ${settings}"

    initialize()
}

def initialize() {
    log.debug "initialize()"

    subscribe(location, null, response, [filterEvents:false])

    relaysConfig.each { deviceCodeName, deviceConfig ->
        setupVirtualRelay(deviceConfig['name'], "switch", deviceCodeName, deviceConfig['defaultState']);
    }

//    setupVirtualRelay("Hot water pump", "switch", "runningWaterPump", "off");
//    setupVirtualRelay("Floor heating pump", "switch", "floorHeatingPump", "on");
//    setupVirtualRelay("Radiators pump", "switch", "radiatorsPump", "on");
}

def updated() {
    log.debug "Updated(): with settings: ${settings}"

    updateRelayState();
    unsubscribe();

    relaysConfig.each { deviceCodeName, deviceConfig ->
        updateVirtualRelay(deviceConfig['name'], "switch", deviceCodeName, deviceConfig['defaultState']);
    }

//    updateVirtualRelay("Hot water pump", "switch", "runningWaterPump", "off");
//    updateVirtualRelay("Floor heating pump", "switch", "floorHeatingPump", "on");
//    updateVirtualRelay("Radiators pump", "switch", "radiatorsPump", "on");

    subscribe(location, null, response, [filterEvents:false])
}

def updateVirtualRelay(deviceName, deviceType, deviceCodeName, defaultState) {
    log.debug "updateVirtualRelay(): deviceName: " + deviceName
    log.debug "updateVirtualRelay(): deviceType: "+deviceType
    log.debug "updateVirtualRelay(): deviceCodeName: "+deviceCodeName

    // If user didn't fill this device out, skip it
    if(!deviceName) return;

    def theDeviceNetworkId = "";
    switch(deviceType) {
        case "switch":
            theDeviceNetworkId = getRelayID(deviceCodeName);
            break;

        case "temperatureSensor":
            theDeviceNetworkId = getTemperatureID(deviceCodeName);
            break;
    }

    log.trace "updateVirtualRelay(): Searching for: $theDeviceNetworkId";

    def theDevice = getChildDevices().find{ d -> d.deviceNetworkId.startsWith(theDeviceNetworkId) }

    if(theDevice){ // The switch already exists
        log.debug "updateVirtualRelay(): Found existing device which we will now update: label: ${theDevice.label}, name: ${theDevice.name}"
        //theDevice.deviceNetworkId = theDeviceNetworkId + "." + deviceCodeName // orginal one
        theDevice.label = deviceName
        theDevice.name = deviceName
        log.debug "updateVirtualRelay(): After updating: label: ${theDevice.label}, name: ${theDevice.name}"

        if(deviceType == "switch") { // Actions specific for the relay device type
            subscribe(theDevice, "switch", switchChangeOrRefresh)
            log.debug "updateVirtualRelay(): Setting initial state of $deviceName to ${defaultState}"
            setDeviceStateOnPyServer(deviceCodeName, defaultState);
            if( defaultState == "on") {
                theDevice.on();
            } else {
                theDevice.off();
            }
        } else {
            updateTempratureSensor();
        }

    } else { // The switch does not exist
        log.debug "updateVirtualRelay(): This device does not exist, creating a new one now"
        setupVirtualRelay(deviceName, deviceType, deviceCodeName, defaultState);
    }

}
def setupVirtualRelay(deviceName, deviceType, deviceCodeName, defaultState) {

    log.debug "setupVirtualRelay()"

    if(deviceName){
        log.debug "setupVirtualRelay(): deviceName: ${deviceName}"
        log.debug "setupVirtualRelay(): deviceType: ${deviceType}"
        log.debug "setupVirtualRelay(): deviceCodeName: ${deviceCodeName}"

        switch(deviceType) {
            case "switch":
                log.trace "setupVirtualRelay(): Setting up a eHome Basement Relay called $deviceName with Device ID #$deviceCodeName"
                def theDevice = addChildDevice("mattPiratt", "eHome Basement Relay", getRelayID(deviceCodeName), theHub.id, [label:deviceName, name:deviceName])
                subscribe(theDevice, "switch", switchChangeOrRefresh)

                log.debug "setupVirtualRelay(): Setting initial state of ${deviceName} at pyServer into OFF"
                setDeviceStateOnPyServer(deviceCodeName, defaultState);
                if( defaultState == "on") {
                    theDevice.on();
                } else {
                    theDevice.off();
                }
                break;

            case "temperatureSensor":
                log.trace "setupVirtualRelay(): Found a temperature sensor called $deviceName on $deviceCodeName"
                def theDevice = addChildDevice("mattPiratt", "eHome Temperature Sensor", getTemperatureID(deviceCodeName), theHub.id, [label:deviceName, name:deviceName])
                state.temperatureZone = deviceCodeName
                updateTempratureSensor();
                break;
        }
    }
}

def String getRelayID(deviceCodeName) {

    return "pyServerRelay." + deviceCodeName
}
def String getTemperatureID(deviceCodeName){

    return  "pyServerTempSensor." + deviceCodeName
}

def uninstalled() {
    log.debug "uninstalled()"
    unsubscribe()
    def delete = getChildDevices()
    delete.each {
        unsubscribe(it)
        log.trace "uninstalled(): about to delete device ${it.deviceNetworkId}"
        deleteChildDevice(it.deviceNetworkId)
    }
}

def response(evt){
    log.debug "response()"
    def msg = parseLanMessage(evt.description);
//    log.debug "response(): msg:"+msg;
    if(msg && msg.body){
//        log.debug "response(): body: ${msg.body}"
        log.debug "response(): json runningWaterPump: ${msg.json.runningWaterPump}"
        log.debug "response(): json floorHeatingPump: ${msg.json.floorHeatingPump}"
        log.debug "response(): json radiatorsPump: ${msg.json.radiatorsPump}"
        log.debug "response(): json intTemp1: ${msg.json.intTemp1}"
        log.debug "response(): json extTemp: ${msg.json.extTemp}"
        log.debug "response(): json waterTemp: ${msg.json.waterTemp}"
        log.debug "response(): json stoveTemp: ${msg.json.stoveTemp}"

        if(msg.json) {
//            def flagsOnPyServerVsID = [
//                    "runningWaterPump": 1,
//                    "floorHeatingPump": 2,
//                    "radiatorsPump": 3,
//            ];
            def children = getChildDevices(false)
            msg.json.each { item ->
                log.debug "response(): each() item.key: ${item.key}"
                log.debug "response(): each() item.value: ${item.value}"
                log.debug "response(): each() children: ${children}"

                if( relaysConfig[item.key]) {
                    updateRelayDevice(item.key, item.value, children);
                }
            }

            log.debug "response(): Finished seting Relay virtual switches"
        }

//        def tempContent = msg.body.tokenize('.')
//        log.debug "response(): tempContent"+tempContent
//        if(tempContent.size() == 2 && tempContent[0].isNumber() && tempContent[1].isNumber() ) {
//
//            // Got temperature response
//            def networkId = getTemperatureID(state.temperatureZone);
//            def theDevice = getChildDevices().find{ d -> d.deviceNetworkId.startsWith(networkId) }
//            log.debug "response(): networkId"+networkId
//            log.debug "response(): theDevice"+theDevice
//
//            if(theDevice) {
//                theDevice.setTemperature(msg.body, state.temperatureZone);
//                log.trace "$theDevice set to $msg.body"
//            }
//        }
    }
}

def updateRelayDevice(deviceCodeName, state, childDevices) {
    log.debug "updateRelayDevice()"

    def theSwitch = childDevices.find{ d -> d.deviceNetworkId.endsWith(".$deviceCodeName") }
    if(theSwitch) {
        log.debug "updateRelayDevice(): Updating switch $theSwitch for Device ID $deviceCodeName with value $state"
        theSwitch.changeSwitchState(state)
    }
}

def updateTempratureSensor() {

    log.trace "updateTempratureSensor(): Updating temperature for $state.temperatureZone"

//    executeRequestToPyServer("/devices/" + state.temperatureZone  + "/sensor/temperature/c", "GET", null);

    runIn(60*60, updateTempratureSensor);
}

def updateRelayState() {

    log.trace "updateRelayState(): Updating Relay map or IDK. Maybe read temp sensors by poolig?"

//    executeRequestToPyServer("/*", "GET", null);

    runIn(60*60, updateRelayState);
}

def switchChangeOrRefresh(evt){

    log.debug "switchChangeOrRefresh(): evt: ${evt}"
    log.debug "switchChangeOrRefresh(): evt.value: ${evt.value}"
    if(evt.value == "on" || evt.value == "off") return;


    def parts = evt.value.tokenize('.');
    def deviceCodeName = parts[1];
    def state = parts.last();

    log.debug "switchChangeOrRefresh(): state:"+ state;
    log.debug "switchChangeOrRefresh(): parts:"+ parts;
    log.debug "switchChangeOrRefresh(): deviceCodeName: "+ deviceCodeName;

    switch(state){
        case "refresh":
            // Refresh this switches button
            log.debug "switchChangeOrRefresh(): Refreshing the state of All/This one (?) relay switch"
//            executeRequestToPyServer("/*", "GET", null)
            return;
    }

    setDeviceStateOnPyServer(deviceCodeName, state);

    return;
}


def setDeviceStateOnPyServer(deviceCodeName, state) {
    log.debug "setDeviceStateOnPyServer(): deviceCodeName: "+deviceCodeName+"; state:"+state

    // Determine the path to post which will set the switch to the desired state
    def Path = "/setFlag";
    def val = (state == "on") ? "1" : "0";

    executeRequestToPyServer(Path, "POST", ["flag":deviceCodeName,"value":val]);
}

def executeRequestToPyServer(Path, method, params) {

    log.debug "executeRequestToPyServer(): Path:" + Path + "; method: "+method+"; params: " +params

    def headers = [:]
    headers.put("HOST", "$settings.piIP:$settings.piPort")
    log.debug "executeRequestToPyServer(): HOST: $settings.piIP:$settings.piPort"

    try {
        def actualAction = new physicalgraph.device.HubAction(
                method: method,
                path: "/api/L7fedk8tBkfbe7JG9Iu11"+Path,
                headers: headers,
                body: params
        )

        sendHubCommand(actualAction)
        log.debug "executeRequestToPyServer(): sendHubCommand done!"
    }
    catch (Exception e) {
        log.debug "executeRequestToPyServer(): Hit Exception $e on $hubAction"
    }
}

/* Helper functions to get the network device ID */
private String NetworkDeviceId(){
    def iphex = convertIPtoHex(settings.piIP).toUpperCase()
    def porthex = convertPortToHex(settings.piPort)
    return "$iphex:$porthex"
}

private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}
