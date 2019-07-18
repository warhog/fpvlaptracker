export interface ProfileData {
    name: string,
    frequency: number,
    minimumLapTime: number,
    triggerThreshold: number,
    triggerThresholdCalibration: number,
    calibrationOffset: number,
    filterRatio: number,
    filterRatioCalibration: number
}


export function isProfileData(arg: any) : arg is ProfileData {
    return arg.name !== undefined && 
        arg.frequency !== undefined && 
        arg.minimumLapTime !== undefined && 
        arg.triggerThreshold !== undefined && 
        arg.triggerThresholdCalibration !== undefined && 
        arg.calibrationOffset !== undefined;
}
 