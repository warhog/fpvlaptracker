export interface LapData {
    type: string,
    lapTime: number,
    rssi: number;
}

export function isLapData(arg: any) : arg is LapData {
    return arg.type !== undefined && arg.type == "lap";
}