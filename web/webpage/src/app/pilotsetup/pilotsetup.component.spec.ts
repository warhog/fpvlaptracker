import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PilotsetupComponent } from './pilotsetup.component';

describe('PilotsetupComponent', () => {
  let component: PilotsetupComponent;
  let fixture: ComponentFixture<PilotsetupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PilotsetupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PilotsetupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
