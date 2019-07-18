export interface DeviceData {
    type: string,
    ssid: string,
    password: string,
    frequency: number,
    minimumLapTime: number,
    triggerThreshold: number,
    triggerThresholdCalibration: number,
    calibrationOffset: number,
    state: string,
    triggerValue: number,
    voltage: number,
    uptime: number,
    defaultVref: number,
    wifiState: boolean,
    rssi: number,
    loopTime: number,
    filterRatio: number,
    filterRatioCalibration: number
}


export function isDeviceData(arg: any) : arg is DeviceData {
    return arg.type !== undefined && arg.type == "device";
}