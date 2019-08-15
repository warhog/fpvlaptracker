import { Component, OnInit, Input, Output, ComponentFactoryResolver } from '@angular/core';
import { BigPilot } from 'src/app/interfaces/bigpilot';
import * as moment from 'moment';
import { PilotsService } from 'src/app/pilots.service';
import { AlertService } from 'src/app/alert.service';
import { EventEmitter } from '@angular/core';
import { LapDataEntity, LapsEntity } from 'src/app/interfaces/racedata';
import { RaceService } from '../race.service';
import { AuthService } from 'src/app/auth.service';

@Component({
  selector: 'app-pilot-stats',
  templateUrl: './pilot-stats.component.html',
  styleUrls: ['./pilot-stats.component.css']
})
export class PilotStatsComponent implements OnInit {

  private _pilot: BigPilot;
  @Output()
  private reloadEvent: EventEmitter<void> = new EventEmitter<void>();
  private _pilotDuration: string = null;

  constructor(private authService: AuthService, private raceService: RaceService, private pilotService: PilotsService, private alertService: AlertService) { }

  ngOnInit() {
  }

  isPilotValid(pilot: BigPilot): boolean { 
    return pilot.valid;
  }

  isPilotValidClass(pilot: BigPilot): string {
    if (!this.isPilotValid(pilot)) {
      return 'strikethrough';
    }
  }

  setLapValid(pilot: BigPilot, lap: LapsEntity, valid: boolean = true) {
    this.raceService.setLapValid(pilot.name, lap.lap, valid, () => {
      this.reloadEvent.emit();
    }, (message) => {
      this.alertService.error('cannot set lap validity: ' + message, 'error');
    });
  }

  isInvalidLapClass(lap: LapsEntity) {
    return lap.invalid ? 'strikethrough' : '';
  }

  setPilotValid(pilot: BigPilot, valid: boolean = true) {
    this.pilotService.setPilotValid(pilot, valid, () => {
      this.reloadEvent.emit();
    }, (message) => {
      this.alertService.error('cannot set pilot validity: ' + message, 'error');
    });
  }

  public isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  public get pilot(): BigPilot {
    return this._pilot;
  }
  @Input()
  public set pilot(value: BigPilot) {
    this._pilot = value;
  }
  public get pilotDuration(): string {
    return this._pilotDuration;
  }
  @Input()
  public set pilotDuration(value: string) {
    this._pilotDuration = value;
  }

}
