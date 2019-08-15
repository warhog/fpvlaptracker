import { NodeDeviceData } from '../nodedevicedata';

export interface BigPilot {
    name: string;
    node?: NodeDeviceData;
    ready: boolean;
    state: string;
    stateFinished: boolean;
    stateInvalid: boolean;
    stateLastLap: boolean;
    stateStarted: boolean;
    stateWaitingForFirstPass: boolean;
    stateWaitingForStart: boolean;
    valid: boolean;
    lapTimeList?: any | null;
}