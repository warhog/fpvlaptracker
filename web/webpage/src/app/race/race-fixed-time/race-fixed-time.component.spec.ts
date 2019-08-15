import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RaceFixedTimeComponent } from './race-fixed-time.component';

describe('RaceFixedTimeComponent', () => {
  let component: RaceFixedTimeComponent;
  let fixture: ComponentFixture<RaceFixedTimeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RaceFixedTimeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RaceFixedTimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
