export interface ScanData {
    type: string,
    frequency: number,
    rssi: number;
}

export function isScanData(arg: any) : arg is ScanData {
    return arg.type !== undefined && arg.type == "scan";
}