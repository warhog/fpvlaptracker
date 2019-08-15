import { Pilot } from './pilot';
import { NodeDeviceData } from '../nodedevicedata';
import { BigPilot } from './bigpilot';

export interface RaceData {
    state: string;
    startTime?: string | null;
    raceType: string;
    toplist?: (null)[] | null;
    typeSpecific: TypeSpecific;
    pilots?: (PilotsEntity)[] | null;
    pilotDurations?: any;
}

export interface PilotsEntity {
    name: string;
    valid: boolean;
    node?: NodeDeviceData | null;
    lapTimeList: LapTimeList;
    state: string;
}

export interface TypeSpecific {

}

export interface TypeSpecificRoundBased extends TypeSpecific {
    numberOfLaps: string;
    preparationDuration: string;
}

export interface TypeSpecificFixedTime extends TypeSpecific {
    startInterval: string;
    raceDuration: string;
    overtimeDuration: string;
    preparationDuration: string;
}

export interface LapDataEntity {
    pilot: BigPilot;
    lapTimeList: LapTimeList;
}

export interface LapTimeList {
    laps?: (LapsEntity)[] | null;
    currentLap: number;
    lastRssi: number;
    totalLaps: number;
    totalDuration: string;
    averageLapDuration: string;
    fastestLapDuration: string;
    fastestLap: number;
}

export interface LapsEntity {
    lap: number;
    duration: string;
    invalid: boolean;
}

