import { Component, OnInit, Input } from '@angular/core';
import * as moment from 'moment';
import { TypeSpecificFixedTime } from 'src/app/interfaces/racedata';

@Component({
  selector: 'app-race-fixed-time',
  templateUrl: './race-fixed-time.component.html',
  styleUrls: ['./race-fixed-time.component.css']
})
export class RaceFixedTimeComponent implements OnInit {

  private _typeSpecificFixedTime: TypeSpecificFixedTime = {
    overtimeDuration: '',
    preparationDuration: '',
    raceDuration: '',
    startInterval: ''
  };

  constructor() { }

  ngOnInit() {
  }

  public get typeSpecificFixedTime(): TypeSpecificFixedTime {
    return this._typeSpecificFixedTime;
  }
  @Input('typeSpecific')
  public set typeSpecificFixedTime(value: TypeSpecificFixedTime) {
    if (value == null) {
      return;
    }
    this._typeSpecificFixedTime = value;
  }
}
