export interface StateData {
    type: string,
    state?: string,
    rssi?: string,
    scan?: string,
    calibration?: string
}

export function isStateData(arg: any) : arg is StateData {
    return arg.type !== undefined && arg.type == "state" && arg.state !== undefined;
}

export function isStateScanData(arg: any) : arg is StateData {
    return arg.type !== undefined && arg.type == "state" && arg.scan !== undefined;
}

export function isStateRssiData(arg: any) : arg is StateData {
    return arg.type !== undefined && arg.type == "state" && arg.rssi !== undefined;
}

export function isStateCalibrationData(arg: any) : arg is StateData {
    return arg.type !== undefined && arg.type == "state" && arg.calibration !== undefined;
}
