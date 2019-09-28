import { Component, OnInit, NgZone } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { UtilService } from '../util.service';
import { AlertService } from '../alert.service';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Subscription } from 'rxjs';
import { Message } from '@stomp/stompjs';
import { NodeService } from '../node.service';
import { NodeDeviceData } from '../nodedevicedata';
import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts";
import am4themes_material from "@amcharts/amcharts4/themes/material";

am4core.useTheme(am4themes_material);

@Component({
  selector: 'app-scan',
  templateUrl: './scan.component.html',
  styleUrls: ['./scan.component.css']
})
export class ScanComponent implements OnInit {

  private _nodeDeviceData: NodeDeviceData = {
    calibrationOffset: 0,
    chipId: 0,
    defaultVref: 0,
    filterRatio: 0,
    filterRatioCalibration: 0,
    frequency: 0,
    inetAddress: '',
    loopTime: 0,
    minimumLapTime: 0,
    rssi: 0,
    state: '',
    triggerThreshold: 0,
    triggerThresholdCalibration: 0,
    triggerValue: 0,
    uptime: 0,
    version: '',
    voltage: 0,
    frequencyObj: {
      frequency: 0,
      name: '',
      shortName: ''
    }
  };
  public get nodeDeviceData(): NodeDeviceData {
    return this._nodeDeviceData;
  }
  public set nodeDeviceData(value: NodeDeviceData) {
    this._nodeDeviceData = value;
  }
  private chipid: number = 0;
  private chartData = [];
  private _maxFreq: string = '-';
  private _maxRssi: number = 0;
  private scanTopicSubscription: Subscription;
  private _lastFrequency: string = '-';
  private chart: am4charts.XYChart;

  constructor(private nodeService: NodeService, private route: ActivatedRoute, private httpClient: HttpClient, private utilService: UtilService, private alertService: AlertService, private router: Router, private rxStompService: RxStompService, private zone: NgZone) { }

  ngOnInit() {
    let me = this;

    this.route.paramMap.subscribe(params => {
      this.chipid = Number(params.get('id'));
      this.loadNode();
    });

    this.scanTopicSubscription = this.rxStompService.watch('/topic/scan').subscribe((message: Message) => {
      console.log("got new scan message", message);
      let scandata = JSON.parse(message.body);
      if (scandata.chipid == me.chipid) {
        me.lastFrequency = scandata.frequency + ' MHz';
        me.chartData.forEach(function (entry) {
          if (entry.frequency === scandata.frequency) {
            entry.rssi = scandata.rssi;
            if (scandata.rssi > me.maxRssi) {
              me.maxRssi = scandata.rssi;
              me.maxFreq = scandata.frequency + ' MHz';
              if (me.utilService.frequencies.indexOf(scandata.frequency) !== -1) {
                me.maxFreq = me.utilService.bands[me.utilService.frequencies.indexOf(scandata.frequency)];
              }
            }
          }
        });
        me.chart.data = me.chartData;
      }
    });
  }

  ngAfterViewInit() {
    this.zone.runOutsideAngular(() => {
      let chart = am4core.create("chartdiv", am4charts.XYChart);

      chart.data = [];

      let categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
      categoryAxis.title.text = 'Frequency [MHz]';
      categoryAxis.dataFields.category = 'frequency';

      let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
      valueAxis.min = 0;
      valueAxis.max = 1000;
      valueAxis.title.text = 'RSSI';

      let series = chart.series.push(new am4charts.ColumnSeries());
      series.dataFields.categoryX = 'frequency';
      series.dataFields.valueY = 'rssi';
      series.tooltipText = "{valueY.value}";

      chart.cursor = new am4charts.XYCursor();

      this.chart = chart;
    });
  }

  ngOnDestroy() {
    this.zone.runOutsideAngular(() => {
      if (this.chart) {
        this.chart.dispose();
      }
    });

    this.scanTopicSubscription.unsubscribe();
    if (this.nodeDeviceData.state == 'Scan' || this.nodeDeviceData.state == 'Deep scan') {
      this.restoreOldState();
    }
  }

  loadNode() {
    this.nodeService.loadNode(this.chipid, (nodeDeviceData: NodeDeviceData) => {
      this.nodeDeviceData = nodeDeviceData;
    }, (message) => {
      console.log('cannot load node: ', message);
      this.alertService.error('cannot load node: ' + message, 'load error');
    });
  }

  scan() {
    this.chartData = [];
    for (let i = 0; i <= this.utilService.frequencies.length; i++) {
      let freq = this.utilService.frequencies[i];
      let text = freq + " MHz";
      if (this.utilService.frequencies.indexOf(i) !== -1) {
        text = this.utilService.bands[this.utilService.frequencies.indexOf(i)];
      }
      this.chartData.push({
        frequency: freq,
        frequencyText: text,
        rssi: 0
      });
    }

    this.maxFreq = this.utilService.bands[0];
    this.maxRssi = 0;
    this.nodeService.setState(this.chipid, 'SCAN', () => {
      this.loadNode();
    }, (message) => {
      this.alertService.error('cannot set scan state: ' + message);
    });
  }

  deepscan() {
    this.chartData = [];
    for (let i = 5362; i <= 5945; i++) {
      let text = i + " MHz";
      if (this.utilService.frequencies.indexOf(i) !== -1) {
        text = this.utilService.bands[this.utilService.frequencies.indexOf(i)];
      }
      this.chartData.push({
        frequency: i,
        frequencyText: text,
        rssi: 0
      });
    }

    this.nodeService.setState(this.chipid, 'DEEPSCAN', () => {
      this.loadNode();
    }, (message) => {
      this.alertService.error('cannot set deepscan state: ' + message);
    });
  }

  restoreOldState() {
    this.lastFrequency = '-';
    this.nodeService.setState(this.chipid, 'RESTORE_STATE', () => {
      this.loadNode();
    }, (message) => {
      this.alertService.error('cannot restore state: ' + message);
    });
  }

  public get maxFreq(): string {
    return this._maxFreq;
  }
  public set maxFreq(value: string) {
    this._maxFreq = value;
  }
  public get lastFrequency(): string {
    return this._lastFrequency;
  }
  public set lastFrequency(value: string) {
    this._lastFrequency = value;
  }
  public get maxRssi(): number {
    return this._maxRssi;
  }
  public set maxRssi(value: number) {
    this._maxRssi = value;
  }
}
