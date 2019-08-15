import { Frequency } from './util.service';

export interface NodeDeviceData {
    calibrationOffset: number;
    chipId: number;
    defaultVref: number;
    filterRatio: number;
    filterRatioCalibration: number;
    frequency: number;
    inetAddress: string;
    loopTime: number;
    minimumLapTime: number;
    rssi: number;
    state: string;
    triggerThreshold: number;
    triggerThresholdCalibration: number;
    triggerValue: number;
    uptime: number;
    version: string;
    voltage: number;
    frequencyObj?: Frequency;
}
