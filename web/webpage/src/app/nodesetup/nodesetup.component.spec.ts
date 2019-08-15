import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NodesetupComponent } from './nodesetup.component';

describe('NodesetupComponent', () => {
  let component: NodesetupComponent;
  let fixture: ComponentFixture<NodesetupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NodesetupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodesetupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
