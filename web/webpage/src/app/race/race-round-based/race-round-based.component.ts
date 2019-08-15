import { Component, OnInit, Input } from '@angular/core';
import * as moment from 'moment';
import { TypeSpecificRoundBased } from 'src/app/interfaces/racedata';

@Component({
  selector: 'app-race-round-based',
  templateUrl: './race-round-based.component.html',
  styleUrls: ['./race-round-based.component.css']
})
export class RaceRoundBasedComponent implements OnInit {

  private _typeSpecificRoundBased: TypeSpecificRoundBased = {
    numberOfLaps: '',
    preparationDuration: ''
  };

  constructor() { }

  ngOnInit() {
  }

  public get typeSpecificRoundBased(): TypeSpecificRoundBased {
    return this._typeSpecificRoundBased;
  }
  @Input('typeSpecific')
  public set typeSpecificRoundBased(value: TypeSpecificRoundBased) {
    if (value == null) {
      return;
    }
    this._typeSpecificRoundBased = value;
  }
}
