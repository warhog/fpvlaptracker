export interface RssiData {
    type: string,
    rssi: number;
}

export function isRssiData(arg: any) : arg is RssiData {
    return arg.type !== undefined && arg.type == "rssi";
}