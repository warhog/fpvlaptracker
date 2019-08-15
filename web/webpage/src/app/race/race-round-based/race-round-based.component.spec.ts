import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RaceRoundBasedComponent } from './race-round-based.component';

describe('RaceRoundBasedComponent', () => {
  let component: RaceRoundBasedComponent;
  let fixture: ComponentFixture<RaceRoundBasedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RaceRoundBasedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RaceRoundBasedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
