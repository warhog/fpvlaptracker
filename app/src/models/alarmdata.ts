export interface AlarmData {
    type: string,
    msg: string
}

export function isAlarmData(arg: any) : arg is AlarmData {
    return arg.type !== undefined && arg.type == "alarm";
}