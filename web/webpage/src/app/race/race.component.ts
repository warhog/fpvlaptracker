import { Component, OnInit, NgZone } from '@angular/core';
import { RaceService } from './race.service';
import { AlertService } from '../alert.service';
import { RaceData } from '../interfaces/racedata';
import { AuthService } from '../auth.service';
import { RaceState } from './race-state.enum';
import * as moment from 'moment';
import { DeviceDetectorService } from 'ngx-device-detector';
import * as NoSleep from 'nosleep.js';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Subscription, interval } from 'rxjs';
import { Message } from '@stomp/stompjs';
import * as am4core from '@amcharts/amcharts4/core';
import * as am4charts from '@amcharts/amcharts4/charts';
import am4themes_material from '@amcharts/amcharts4/themes/material';
import { PilotState } from './pilot-state.enum';

am4core.useTheme(am4themes_material);

@Component({
  selector: 'app-race',
  templateUrl: './race.component.html',
  styleUrls: ['./race.component.css']
})
export class RaceComponent implements OnInit {

  private _raceData: RaceData = {
    startTime: '',
    state: '',
    typeSpecific: null,
    raceType: 'ROUND_BASED'
  };
  private _sleepDisabled: boolean;
  private noSleep = new NoSleep();
  private isLoading: boolean = false;
  private isLoadingChart: boolean = false;
  private raceStateTopicSubscription: Subscription;
  private newLapTopicSubscription: Subscription;
  private chart: am4charts.XYChart;
  private chartData: any = [];
  private chartInitialized: boolean = false;
  private durationIntervalSubscription: Subscription;

  constructor(private zone: NgZone, private rxStompService: RxStompService, private authService: AuthService, private raceService: RaceService, private alertService: AlertService, private deviceService: DeviceDetectorService) { }

  ngOnInit() {
    this.loadRaceData();

    this.raceStateTopicSubscription = this.rxStompService.watch('/topic/race/state').subscribe((message: Message) => {
      console.log('got new race state message', message);
      this.raceData.state = JSON.parse(message.body);
    });

    this.newLapTopicSubscription = this.rxStompService.watch('/topic/lap').subscribe((message: Message) => {
      console.log('got new lap message', message);
      this.loadRaceData();
    });

    const source = interval(100);
    this.durationIntervalSubscription = source.subscribe(val => {
      if (this.raceData.state == RaceState.Running && this.raceData.raceType == 'FIXED_TIME') {
        let keys: string[] = Object.keys(this.raceData.pilotDurations);
        for (let i = 0; i < keys.length; i++) {
          this.raceData.pilotDurations[keys[i]] -= 100;
        }
      }
    });

  }

  ngOnDestroy() {
    this.raceStateTopicSubscription.unsubscribe();
    this.newLapTopicSubscription.unsubscribe();
    this.durationIntervalSubscription.unsubscribe();
    this.zone.runOutsideAngular(() => {
      if (this.chart) {
        this.chart.dispose();
      }
    });
  }

  isRunning(): boolean {
    return this.raceData.state == RaceState.Running || this.raceData.state == RaceState.GetReady || this.raceData.state == RaceState.Prepare;
  }

  isMobile(): boolean {
    return !this.deviceService.isDesktop();
  }

  sleepDisable() {
    this.noSleep.disable();
  }

  sleepEnable() {
    this.noSleep.enable();
  }

  onReloadEvent($event: any) {
    this.loadRaceData();
  }

  getPilotDuration(pilotName: string) {
    if (this.raceData.pilotDurations[pilotName] != undefined) {
      this.raceData.pilots.forEach((pilot) => {
        if (pilot.name == pilotName) {
          if (pilot.state == PilotState.Started) {
            return this.raceData.pilotDurations[pilotName];
          }
        }
      });
    }
    return "PT0S";
  }

  loadRaceData() {
    if (this.isLoading) {
      return;
    }
    this.isLoading = true;
    this.raceService.loadRaceData((raceData) => {
      this.raceData = raceData;
      this.isLoading = false;
      this.loadRaceChartData();
    }, (message) => {
      this.alertService.error('failed to load race data: ' + message, 'load error');
      this.isLoading = false;
    });
  }

  loadRaceChartData() {
    if (this.isLoadingChart) {
      return;
    }
    this.isLoadingChart = true;
    this.raceService.loadRaceChartData((chartData) => {
      this.chartData = chartData;
      console.log('chartdata loaded');
      if (this.chartInitialized) {
        this.chart.data = this.chartData.lapTimes;
        console.log('chartdata updated');
      } else {
        this.createChart();
      }
      //$('body').hide().show();
      this.isLoadingChart = false;
    }, (message) => {
      this.alertService.error('failed to load race chart data: ' + message, 'load error');
      this.isLoadingChart = false;
    });
  }

  createChart() {
    if (!this.chartInitialized) {
      this.zone.runOutsideAngular(() => {
        this.chartInitialized = true;
        let chart = am4core.create('chartdiv', am4charts.XYChart);

        chart.data = this.chartData.lapTimes;

        let categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
        categoryAxis.title.text = 'Lap';
        categoryAxis.dataFields.category = 'lap';

        let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
        valueAxis.title.text = 'Duration [s]';

        valueAxis.numberFormatter = new am4core.NumberFormatter();
        valueAxis.numberFormatter.numberFormat = "#.000";

        chart.cursor = new am4charts.XYCursor();
        //chart.numberFormatter.numberFormat = '#.00';

        if (this.chartData.pilots) {
          this.chartData.pilots.forEach((name: string) => {
            let series = chart.series.push(new am4charts.LineSeries());
            series.dataFields.categoryX = 'lap';
            series.dataFields.valueY = name;
            series.tooltipText = '{valueY.value}';
          });
        } else {
          console.log('no pilots for chart series creation');
        }

        this.chart = chart;
        console.log('chart created', this.chart);
      });
    } else {
      console.log('chart already initialized');
    }
  }


  nonEmptyToplist() {
    if (this.raceData.toplist !== undefined) {
      return Object.keys(this.raceData.toplist).length > 0;
    }
    return false;
  }

  hasValidPilots(): boolean {
    if (this.raceData.pilots) {
      let filteredValid = this.raceData.pilots.filter((pilot) => {
        return pilot.valid && pilot.node != null;
      });
      return filteredValid.length > 0;
    }
    return false;
  }

  startRace() {
    this.raceService.startRace(() => {
      this.loadRaceData();
    }, (message) => {
      this.alertService.error('failed to start race: ' + message, 'error');
    });
  }

  stopRace() {
    this.raceService.stopRace(() => {
      this.loadRaceData();
    }, (message) => {
      this.alertService.error('failed to stop race: ' + message, 'error');
    });
  }

  switchToType(type: string) {
    this.raceService.setRaceType(type, () => {
      this.loadRaceData();
    }, (message) => {
      this.alertService.error('failed to switch race type: ' + message, 'switch state error');
    });
  }

  convertTime(time) {
    return moment(time).format('HH:mm:ss, DD.MM.');
  };

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  public get raceData(): RaceData {
    return this._raceData;
  }
  public set raceData(value: RaceData) {
    this._raceData = value;
  }
  public get sleepDisabled(): boolean {
    return this._sleepDisabled;
  }
  public set sleepDisabled(value: boolean) {
    this._sleepDisabled = value;
  }

}
