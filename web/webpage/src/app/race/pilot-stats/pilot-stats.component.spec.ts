import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PilotStatsComponent } from './pilot-stats.component';

describe('PilotStatsComponent', () => {
  let component: PilotStatsComponent;
  let fixture: ComponentFixture<PilotStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PilotStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PilotStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
